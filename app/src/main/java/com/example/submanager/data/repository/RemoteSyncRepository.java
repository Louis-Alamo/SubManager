package com.example.submanager.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.model.ConfiguracionAppModel;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.ServicioFisicoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.model.TercerosCompartidosModel;
import com.example.submanager.data.remote.SupabaseClient;
import com.example.submanager.data.remote.api.SupabaseApi;
import com.example.submanager.data.remote.dto.ConfiguracionAppDto;
import com.example.submanager.data.remote.dto.RegistroPagoDto;
import com.example.submanager.data.remote.dto.ServicioFisicoDto;
import com.example.submanager.data.remote.dto.SuscripcionDto;
import com.example.submanager.data.remote.dto.TerceroCompartidoDto;
import com.example.submanager.data.remote.dto.UsuarioDto;
import com.example.submanager.utils.NetworkUtils;
import com.example.submanager.utils.SessionManager;
import androidx.lifecycle.MutableLiveData;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Repositorio central de sincronización remota.
 *
 * Estrategia: "Replace-All" offline-first
 *  1. Lee todos los datos de Room (fuente de verdad local)
 *  2. Borra los datos remotos en Supabase
 *  3. Sube los datos locales a Supabase
 *  4. Actualiza ultima_sincronizacion en Room y Supabase
 *
 * Para el Pull (Restaurar):
 *  1. Descarga todos los datos de Supabase
 *  2. Borra los datos locales en Room
 *  3. Inserta los datos remotos en Room
 */
public class RemoteSyncRepository {

    private static final String TAG = "RemoteSyncRepo";

    /** Resultado de una operación de sincronización */
    public enum SyncStatus {
        SUCCESS,
        NO_NETWORK,
        NOT_PREMIUM,
        ERROR,
    }

    /** Callback para notificar el resultado en el hilo principal */
    public interface SyncCallback {
        void onResult(SyncStatus status, String message);
    }

    private final Context context;
    private final AppDatabase db;
    private final SupabaseApi api;
    private final SessionManager session;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // Estado global de sincronización para observar desde la UI
    public static final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    public static final MutableLiveData<String> syncResult = new MutableLiveData<>(null);

    public RemoteSyncRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(this.context);
        this.api = SupabaseClient.getApi();
        this.session = new SessionManager(this.context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUSH — Local → Supabase (Respaldar)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sincroniza todos los datos locales con Supabase.
     * Solo funciona si el usuario es Premium y hay conexión.
     *
     * @param callback Resultado en el hilo principal.
     */
    public void syncAll(SyncCallback callback) {
        if (!session.isPremium()) {
            notifyCallback(
                callback,
                SyncStatus.NOT_PREMIUM,
                "Se requiere cuenta Premium para sincronizar."
            );
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            notifyCallback(
                callback,
                SyncStatus.NO_NETWORK,
                "Sin conexión a internet. Intenta más tarde."
            );
            return;
        }

        executor.execute(() -> {
            mainHandler.post(() -> {
                isSyncing.setValue(true);
                syncResult.setValue(null);
            });
            try {
                long userId = session.getRemoteUserId();
                if (userId == -1) {
                    try {
                        Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                            .getUsuarioPorCorreo("eq." + session.getEmail())
                            .execute();
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            userId = resp.body().get(0).id;
                            session.saveRemoteUserId(userId);
                        }
                    } catch (Exception ignored) {}
                }

                if (userId == -1) {
                    notifyCallback(callback, SyncStatus.ERROR, "Tu cuenta ('" + session.getEmail() + "') solo existe en este teléfono y nunca se registró en Supabase (o fue borrada en la nube). \n\nSugerencia: Desinstala la app, vuelve a instalarla y REGÍSTRATE para sincronizarla correctamente.");
                    return;
                }
                String userFilter = "eq." + userId;
                int total = 0;

                // ── 1. Suscripciones ──────────────────────────────────────────
                List<SuscripcionModel> suscripciones = db
                    .suscripcionDao()
                    .getAllSuscripcionesSync();
                Log.d(TAG, "Suscripciones en Room: " + suscripciones.size());

                if (!suscripciones.isEmpty()) {
                    // Borrar remotas
                    Response<Void> delResp = api
                        .deleteAllSuscripciones(userFilter)
                        .execute();
                    Log.d(TAG, "DELETE suscripciones → HTTP " + delResp.code());

                    // Subir locales
                    List<SuscripcionDto> dtos = new ArrayList<>();
                    for (SuscripcionModel m : suscripciones) dtos.add(toDto(m));

                    Response<Void> insResp = api
                        .insertSuscripciones(dtos)
                        .execute();
                    Log.d(TAG, "INSERT suscripciones → HTTP " + insResp.code());

                    if (!insResp.isSuccessful()) {
                        String errorBody =
                            insResp.errorBody() != null
                                ? insResp.errorBody().string()
                                : "sin detalle";
                        String msg = errorBody;

                        // Mensajes de error amigables según código HTTP
                        if (insResp.code() == 401 || insResp.code() == 403) {
                            msg =
                                "Acceso denegado (HTTP " +
                                insResp.code() +
                                ").\n\n" +
                                "Necesitas configurar las políticas RLS en Supabase.\n" +
                                "Ve a: Supabase → Authentication → Policies → suscripciones → Nueva política para rol 'anon'.";
                        } else if (insResp.code() == 422) {
                            msg =
                                "Error de esquema (HTTP 422). Campos incompatibles con la tabla de Supabase:\n" +
                                errorBody;
                        } else if (insResp.code() == 0) {
                            msg =
                                "Sin respuesta del servidor. Verifica tu conexión.";
                        } else {
                            msg =
                                "Error HTTP " +
                                insResp.code() +
                                ": " +
                                errorBody;
                        }

                        notifyCallback(callback, SyncStatus.ERROR, msg);
                        return; // Detener aquí para mostrar el error real
                    }
                    total += dtos.size();
                }

                // ── 2. Servicios físicos ──────────────────────────────────────
                List<ServicioFisicoModel> servicios = db
                    .suscripcionDao()
                    .getAllServiciosFisicosSync();
                Log.d(TAG, "Servicios físicos en Room: " + servicios.size());
                if (!servicios.isEmpty()) {
                    api.deleteAllServiciosFisicos(userFilter).execute();
                    List<ServicioFisicoDto> dtos = new ArrayList<>();
                    for (ServicioFisicoModel m : servicios) dtos.add(toDto(m));
                    Response<Void> insResp = api
                        .insertServiciosFisicos(dtos)
                        .execute();
                    Log.d(TAG, "INSERT servicios → HTTP " + insResp.code());
                    if (insResp.isSuccessful()) total += dtos.size();
                }

                // ── 3. Terceros compartidos ───────────────────────────────────
                List<TercerosCompartidosModel> terceros = db
                    .suscripcionDao()
                    .getAllTercerosSync();
                if (!terceros.isEmpty()) {
                    api.deleteAllTerceros(userFilter).execute();
                    List<TerceroCompartidoDto> dtos = new ArrayList<>();
                    for (TercerosCompartidosModel m : terceros)
                        dtos.add(toDto(m));
                    Response<Void> insResp = api
                        .insertTercerosCompartidos(dtos)
                        .execute();
                    if (insResp.isSuccessful()) total += dtos.size();
                }

                // ── 4. Registros de pago ──────────────────────────────────────
                List<RegistrosPagoModel> registros = db
                    .suscripcionDao()
                    .getAllRegistrosPagoSync();
                if (!registros.isEmpty()) {
                    api.deleteAllRegistrosPago(userFilter).execute();
                    List<RegistroPagoDto> dtos = new ArrayList<>();
                    for (RegistrosPagoModel m : registros) dtos.add(toDto(m));
                    Response<Void> insResp = api
                        .insertRegistrosPago(dtos)
                        .execute();
                    if (insResp.isSuccessful()) total += dtos.size();
                }

                // ── 5. Actualizar ultima_sincronizacion ───────────────────────
                String ahora = Instant.now().toString();
                actualizarUltimaSincronizacion(ahora);

                mainHandler.post(() -> {
                    isSyncing.setValue(false);
                    syncResult.setValue("SUCCESS");
                });

                final int totalFinal = total;
                if (totalFinal == 0) {
                    notifyCallback(
                        callback,
                        SyncStatus.SUCCESS,
                        "Sincronización completada. No había datos nuevos que subir (Room vacío o sin suscripciones)."
                    );
                } else {
                    notifyCallback(
                        callback,
                        SyncStatus.SUCCESS,
                        totalFinal + " registros sincronizados correctamente."
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "syncAll error", e);
                mainHandler.post(() -> {
                    isSyncing.setValue(false);
                    syncResult.setValue("ERROR");
                });
                notifyCallback(
                    callback,
                    SyncStatus.ERROR,
                    "Error inesperado: " + e.getMessage()
                );
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PULL — Supabase → Local (Restaurar)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Restaura todos los datos desde Supabase a Room.
     * Solo funciona si el usuario es Premium y hay conexión.
     *
     * @param callback Resultado en el hilo principal.
     */
    public void pullAll(SyncCallback callback) {
        if (!session.isPremium()) {
            notifyCallback(
                callback,
                SyncStatus.NOT_PREMIUM,
                "Se requiere cuenta Premium."
            );
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            notifyCallback(
                callback,
                SyncStatus.NO_NETWORK,
                "Sin conexión a internet."
            );
            return;
        }

        executor.execute(() -> {
            try {
                long userId = session.getRemoteUserId();
                if (userId == -1) {
                    try {
                        Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                            .getUsuarioPorCorreo("eq." + session.getEmail())
                            .execute();
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            userId = resp.body().get(0).id;
                            session.saveRemoteUserId(userId);
                        }
                    } catch (Exception ignored) {}
                }

                if (userId == -1) {
                    notifyCallback(callback, SyncStatus.ERROR, "Tu cuenta ('" + session.getEmail() + "') solo existe en este teléfono y nunca se registró en Supabase (o fue borrada en la nube). \n\nSugerencia: Desinstala la app, vuelve a instalarla y REGÍSTRATE para sincronizarla correctamente.");
                    return;
                }
                String userFilter = "eq." + userId;

                // ── 1. Suscripciones ──────────────────────────────────────────
                Response<List<SuscripcionDto>> susResp = api
                    .getSuscripciones(userFilter)
                    .execute();
                if (susResp.isSuccessful() && susResp.body() != null) {
                    db.suscripcionDao().deleteAllSuscripciones();
                    List<SuscripcionModel> models = new ArrayList<>();
                    for (SuscripcionDto dto : susResp.body()) {
                        models.add(toModel(dto));
                    }
                    db.suscripcionDao().insertAllSuscripciones(models);
                }

                // ── 2. Servicios físicos ──────────────────────────────────────
                Response<List<ServicioFisicoDto>> srvResp = api
                    .getServiciosFisicos(userFilter)
                    .execute();
                if (srvResp.isSuccessful() && srvResp.body() != null) {
                    db.suscripcionDao().deleteAllServiciosFisicos();
                    List<ServicioFisicoModel> models = new ArrayList<>();
                    for (ServicioFisicoDto dto : srvResp.body()) {
                        models.add(toModel(dto));
                    }
                    db.suscripcionDao().insertAllServiciosFisicos(models);
                }

                // ── 3. Registros de pago ──────────────────────────────────────
                Response<List<RegistroPagoDto>> regResp = api
                    .getRegistrosPago(userFilter)
                    .execute();
                if (regResp.isSuccessful() && regResp.body() != null) {
                    db.suscripcionDao().deleteAllRegistrosPago();
                    List<RegistrosPagoModel> models = new ArrayList<>();
                    for (RegistroPagoDto dto : regResp.body()) {
                        models.add(toModel(dto));
                    }
                    db.suscripcionDao().insertAllRegistrosPago(models);
                }

                // ── 4. Actualizar timestamp ───────────────────────────────────
                actualizarUltimaSincronizacion(Instant.now().toString());

                mainHandler.post(() -> {
                    isSyncing.setValue(false);
                    syncResult.setValue("SUCCESS");
                });

                notifyCallback(
                    callback,
                    SyncStatus.SUCCESS,
                    "Datos restaurados correctamente desde la nube."
                );
            } catch (Exception e) {
                Log.e(TAG, "pullAll error", e);
                notifyCallback(
                    callback,
                    SyncStatus.ERROR,
                    "Error al restaurar: " + e.getMessage()
                );
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers internos
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarUltimaSincronizacion(String timestamp) {
        // Room local
        ConfiguracionAppModel config = db
            .suscripcionDao()
            .getConfiguracionSync();
        if (config == null) {
            config = new ConfiguracionAppModel();
        }
        config.setUltimaSincronizacion(timestamp);
        db.suscripcionDao().upsertConfiguracion(config);

        // Remoto Supabase
        try {
            long userId = session.getRemoteUserId();
            String userFilter = "eq." + userId;
            
            ConfiguracionAppDto dto = new ConfiguracionAppDto();
            if (userId != -1) dto.usuarioId = userId;
            dto.ultimaSincronizacion = timestamp;
            
            // Intentar PATCH primero, si falla hacer POST
            Response<Void> patchResp = api
                .updateConfiguracion(userFilter, dto)
                .execute();
            if (!patchResp.isSuccessful()) {
                api.insertConfiguracion(dto).execute();
            }
        } catch (Exception e) {
            Log.w(
                TAG,
                "No se pudo actualizar ultima_sincronizacion remota: " +
                    e.getMessage()
            );
        }
    }

    private void notifyCallback(
        SyncCallback callback,
        SyncStatus status,
        String message
    ) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onResult(status, message));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Convertidores Model → DTO
    // ─────────────────────────────────────────────────────────────────────────

    private SuscripcionDto toDto(SuscripcionModel m) {
        SuscripcionDto dto = new SuscripcionDto();
        dto.id = (long) m.getId();
        long userId = session.getRemoteUserId();
        if (userId != -1) {
            dto.usuarioId = userId;
        }
        dto.nombre = m.getNombre();
        dto.monto = m.getMonto();
        dto.cicloFacturacion = m.getCicloFacturacion();
        dto.color = m.getColor();
        dto.categoria = m.getCategoria();
        dto.metodoPago = m.getMetodoPago();
        dto.fechaPrimerCobro = m.getFechaPrimerCobro();
        dto.fechaProximoCobro = m.getFechaProximoCobro();
        dto.fechaLimiteCancelacion = m.getFechaLimiteCancelacion();
        dto.recordatorioHabilitado = m.isRecordatorioHabilitado();
        dto.diasAnticipacion = m.getDiasAnticipacion();
        dto.notificacionSilenciada = m.isNotificacionSilenciada();
        dto.estaActiva = m.isEstaActiva();
        dto.iconoNombre = m.getNombreIcono();
        dto.creadoEn = m.getCreadoEn();
        dto.actualizadoEn = m.getActualizadoEn();
        return dto;
    }

    private ServicioFisicoDto toDto(ServicioFisicoModel m) {
        ServicioFisicoDto dto = new ServicioFisicoDto();
        dto.id = (long) m.getId();
        long userId = session.getRemoteUserId();
        if (userId != -1) dto.usuarioId = userId;
        dto.nombre = m.getNombre();
        dto.montoEstimado = m.getMontoEstimado();
        dto.montoVariable = m.isMontoVariable();
        dto.cicloFacturacion = m.getCicloFacturacion();
        dto.fechaProximoCobro = m.getFechaProximoCobro();
        dto.esCompartido = m.isEsCompartido();
        dto.montoTotalRecibo = m.getMontoTotalRecibo();
        dto.montoParteUsuario = m.getMontoParteUsuario();
        dto.recordatorioHabilitado = m.isRecordatorioHabilitado();
        dto.diasAnticipacion = m.getDiasAnticipacion();
        dto.notificacionSilenciada = m.isNotificacionSilenciada();
        dto.rutaImagenComprobante = m.getRutaImagenComprobante();
        dto.estaActivo = m.isEstaActivo();
        dto.creadoEn = m.getCreadoEn();
        dto.actualizadoEn = m.getActualizadoEn();
        return dto;
    }

    private TerceroCompartidoDto toDto(TercerosCompartidosModel m) {
        TerceroCompartidoDto dto = new TerceroCompartidoDto();
        dto.id = (long) m.getId();
        long userId = session.getRemoteUserId();
        if (userId != -1) dto.usuarioId = userId;
        dto.servicioId = (long) m.getServicioId();
        dto.nombreTercero = m.getNombreTercero();
        dto.montoAportacion = m.getMontoAportacion();
        dto.creadoEn = m.getCreadoEn();
        return dto;
    }

    private RegistroPagoDto toDto(RegistrosPagoModel m) {
        RegistroPagoDto dto = new RegistroPagoDto();
        dto.id = (long) m.getId();
        long userId = session.getRemoteUserId();
        if (userId != -1) dto.usuarioId = userId;
        dto.suscripcionId =
            m.getSuscripcionId() != null
                ? m.getSuscripcionId().longValue()
                : null;
        dto.servicioId =
            m.getServicioId() != null ? m.getServicioId().longValue() : null;
        dto.nombreOrigen = m.getNombreOrigen();
        dto.colorOrigen = m.getColorOrigen();
        dto.categoria = m.getCategoria();
        dto.monto = m.getMonto();
        dto.estado = m.getEstado();
        dto.fechaVencimiento = m.getFechaVencimiento();
        dto.fechaPago = m.getFechaPago();
        dto.mesFacturacion = m.getMesFacturacion();
        dto.anioFacturacion = m.getAnioFacturacion();
        dto.creadoEn = m.getCreadoEn();
        dto.actualizadoEn = m.getActualizadoEn();
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Convertidores DTO → Model (para Pull)
    // ─────────────────────────────────────────────────────────────────────────

    private SuscripcionModel toModel(SuscripcionDto dto) {
        SuscripcionModel m = new SuscripcionModel();
        if (dto.id != null) m.setId(dto.id.intValue());
        m.setNombre(dto.nombre != null ? dto.nombre : "");
        m.setMonto(dto.monto != null ? dto.monto : 0.0);
        m.setCicloFacturacion(
            dto.cicloFacturacion != null ? dto.cicloFacturacion : "MENSUAL"
        );
        m.setColor(dto.color != null ? dto.color : "#2563EB");
        m.setCategoria(dto.categoria != null ? dto.categoria : "OTRO");
        m.setMetodoPago(dto.metodoPago != null ? dto.metodoPago : "TARJETA");
        m.setFechaPrimerCobro(
            dto.fechaPrimerCobro != null ? dto.fechaPrimerCobro : ""
        );
        m.setFechaProximoCobro(
            dto.fechaProximoCobro != null ? dto.fechaProximoCobro : ""
        );
        m.setFechaLimiteCancelacion(dto.fechaLimiteCancelacion);
        m.setRecordatorioHabilitado(
            dto.recordatorioHabilitado != null
                ? dto.recordatorioHabilitado
                : true
        );
        m.setDiasAnticipacion(
            dto.diasAnticipacion != null ? dto.diasAnticipacion : 3
        );
        m.setNotificacionSilenciada(
            dto.notificacionSilenciada != null
                ? dto.notificacionSilenciada
                : false
        );
        m.setEstaActiva(dto.estaActiva != null ? dto.estaActiva : true);
        m.setNombreIcono(dto.iconoNombre != null ? dto.iconoNombre : "");
        m.setCreadoEn(dto.creadoEn != null ? dto.creadoEn : "");
        m.setActualizadoEn(dto.actualizadoEn != null ? dto.actualizadoEn : "");
        return m;
    }

    private ServicioFisicoModel toModel(ServicioFisicoDto dto) {
        ServicioFisicoModel m = new ServicioFisicoModel();
        if (dto.id != null) m.setId(dto.id.intValue());
        m.setNombre(dto.nombre != null ? dto.nombre : "");
        m.setMontoEstimado(dto.montoEstimado != null ? dto.montoEstimado : 0.0);
        m.setMontoVariable(
            dto.montoVariable != null ? dto.montoVariable : false
        );
        m.setCicloFacturacion(
            dto.cicloFacturacion != null ? dto.cicloFacturacion : "MENSUAL"
        );
        m.setFechaProximoCobro(
            dto.fechaProximoCobro != null ? dto.fechaProximoCobro : ""
        );
        m.setEsCompartido(dto.esCompartido != null ? dto.esCompartido : false);
        m.setMontoTotalRecibo(dto.montoTotalRecibo);
        m.setMontoParteUsuario(dto.montoParteUsuario);
        m.setRecordatorioHabilitado(
            dto.recordatorioHabilitado != null
                ? dto.recordatorioHabilitado
                : true
        );
        m.setDiasAnticipacion(
            dto.diasAnticipacion != null ? dto.diasAnticipacion : 3
        );
        m.setNotificacionSilenciada(
            dto.notificacionSilenciada != null
                ? dto.notificacionSilenciada
                : false
        );
        m.setRutaImagenComprobante(dto.rutaImagenComprobante);
        m.setEstaActivo(dto.estaActivo != null ? dto.estaActivo : true);
        m.setCreadoEn(dto.creadoEn != null ? dto.creadoEn : "");
        m.setActualizadoEn(dto.actualizadoEn != null ? dto.actualizadoEn : "");
        return m;
    }

    private RegistrosPagoModel toModel(RegistroPagoDto dto) {
        RegistrosPagoModel m = new RegistrosPagoModel();
        if (dto.id != null) m.setId(dto.id.intValue());
        m.setSuscripcionId(
            dto.suscripcionId != null ? dto.suscripcionId.intValue() : null
        );
        m.setServicioId(
            dto.servicioId != null ? dto.servicioId.intValue() : null
        );
        m.setNombreOrigen(dto.nombreOrigen != null ? dto.nombreOrigen : "");
        m.setColorOrigen(dto.colorOrigen != null ? dto.colorOrigen : "#000000");
        m.setCategoria(dto.categoria != null ? dto.categoria : "OTRO");
        m.setMonto(dto.monto != null ? dto.monto : 0.0);
        m.setEstado(dto.estado != null ? dto.estado : "PENDIENTE");
        m.setFechaVencimiento(
            dto.fechaVencimiento != null ? dto.fechaVencimiento : ""
        );
        m.setFechaPago(dto.fechaPago);
        m.setMesFacturacion(
            dto.mesFacturacion != null ? dto.mesFacturacion : 1
        );
        m.setAnioFacturacion(
            dto.anioFacturacion != null ? dto.anioFacturacion : 2025
        );
        m.setCreadoEn(dto.creadoEn != null ? dto.creadoEn : "");
        m.setActualizadoEn(dto.actualizadoEn != null ? dto.actualizadoEn : "");
        return m;
    }
}
