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
















public class RemoteSyncRepository {

    private static final String TAG = "RemoteSyncRepo";
    private static final String SERVER_CONNECTION_ERROR =
        "No se pudo establecer conexión con el servidor. Intenta más tarde.";
    private static final String ACCOUNT_NOT_REGISTERED_ERROR =
        "Esta cuenta no está registrada correctamente en el servidor. Vuelve a iniciar sesión o regístrate nuevamente.";


    public enum SyncStatus {
        SUCCESS,
        NO_NETWORK,
        NOT_PREMIUM,
        ERROR,
    }


    public interface SyncCallback {
        void onResult(SyncStatus status, String message);
    }

    private static class RemoteUserResolution {
        final long userId;
        final SyncStatus status;
        final String message;

        RemoteUserResolution(long userId, SyncStatus status, String message) {
            this.userId = userId;
            this.status = status;
            this.message = message;
        }

        boolean isSuccess() {
            return userId != -1L;
        }
    }

    private final Context context;
    private final AppDatabase db;
    private final SupabaseApi api;
    private final SessionManager session;
    private final Handler mainHandler;


    public static final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    public static final MutableLiveData<String> syncResult = new MutableLiveData<>(null);






    private static final ExecutorService executor = Executors.newSingleThreadExecutor();





    private static final AtomicBoolean isSyncRunning = new AtomicBoolean(false);

    public RemoteSyncRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(this.context);
        this.api = SupabaseClient.getApi();
        this.session = new SessionManager(this.context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }











    private RemoteUserResolution resolveRemoteUserId() {
        long currentUserId = session.getRemoteUserId();
        if (currentUserId != -1L) {
            return new RemoteUserResolution(currentUserId, SyncStatus.SUCCESS, "");
        }

        String email = session.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return new RemoteUserResolution(-1L, SyncStatus.ERROR, ACCOUNT_NOT_REGISTERED_ERROR);
        }

        try {
            Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                .getUsuarioPorCorreo("eq." + email)
                .execute();

            if (!resp.isSuccessful() || resp.body() == null) {
                return new RemoteUserResolution(-1L, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
            }

            if (resp.body().isEmpty() || resp.body().get(0).id == null || resp.body().get(0).id <= 0) {
                session.clearRemoteUserId();
                return new RemoteUserResolution(-1L, SyncStatus.ERROR, ACCOUNT_NOT_REGISTERED_ERROR);
            }

            long resolvedUserId = resp.body().get(0).id;
            session.saveRemoteUserId(resolvedUserId);
            return new RemoteUserResolution(resolvedUserId, SyncStatus.SUCCESS, "");
        } catch (Exception e) {
            Log.e(TAG, "No se pudo resolver remoteUserId", e);
            return new RemoteUserResolution(-1L, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
        }
    }

    private RemoteUserResolution verifyRemotePremium() {
        String email = session.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return new RemoteUserResolution(-1L, SyncStatus.ERROR, ACCOUNT_NOT_REGISTERED_ERROR);
        }

        try {
            Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                .getUsuarioPorCorreo("eq." + email)
                .execute();

            if (!resp.isSuccessful() || resp.body() == null) {
                return new RemoteUserResolution(-1L, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
            }

            if (resp.body().isEmpty() || resp.body().get(0).id == null || resp.body().get(0).id <= 0) {
                session.clearRemoteUserId();
                session.clearPremium();
                return new RemoteUserResolution(-1L, SyncStatus.ERROR, ACCOUNT_NOT_REGISTERED_ERROR);
            }

            UsuarioDto usuario = resp.body().get(0);
            session.saveRemoteUserId(usuario.id);
            if (isPremiumPlanActive(usuario)) {
                session.savePremium(
                    usuario.tipoPlan,
                    usuario.fechaRenovacion != null ? usuario.fechaRenovacion : ""
                );
                return new RemoteUserResolution(usuario.id, SyncStatus.SUCCESS, "");
            }

            session.clearPremium();
            return new RemoteUserResolution(
                -1L,
                SyncStatus.NOT_PREMIUM,
                "Se requiere cuenta Premium para sincronizar."
            );
        } catch (Exception e) {
            Log.e(TAG, "No se pudo validar Premium remoto", e);
            return new RemoteUserResolution(-1L, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
        }
    }

    private boolean isPremiumPlanActive(UsuarioDto usuario) {
        return usuario.tipoPlan != null &&
            !"GRATIS".equalsIgnoreCase(usuario.tipoPlan) &&
            usuario.estaActivo != null &&
            usuario.estaActivo;
    }





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

            if (!isSyncRunning.compareAndSet(false, true)) {
                notifyCallback(callback, SyncStatus.SUCCESS, "Sincronización ya en progreso.");
                return;
            }
            mainHandler.post(() -> {
                isSyncing.setValue(true);
                syncResult.setValue(null);
            });
            try {
                RemoteUserResolution resolution = resolveRemoteUserId();
                if (!resolution.isSuccess()) {
                    notifyCallback(callback, resolution.status, resolution.message);
                    return;
                }
                RemoteUserResolution premiumResolution = verifyRemotePremium();
                if (!premiumResolution.isSuccess()) {
                    notifyCallback(callback, premiumResolution.status, premiumResolution.message);
                    return;
                }
                long userId = premiumResolution.userId;
                String userFilter = "eq." + userId;
                int total = 0;






                List<SuscripcionModel> suscripciones = db.suscripcionDao().getAllSuscripcionesSync();
                if (!suscripciones.isEmpty()) {
                    List<SuscripcionDto> dtos = new ArrayList<>();
                    for (SuscripcionModel m : suscripciones) dtos.add(toDto(m));
                    Response<Void> insResp = api.insertSuscripciones(dtos).execute();
                    if (!insResp.isSuccessful()) {
                        notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                        return;
                    }
                    total += dtos.size();
                }


                List<ServicioFisicoModel> servicios = db.suscripcionDao().getAllServiciosFisicosSync();
                if (!servicios.isEmpty()) {
                    List<ServicioFisicoDto> dtos = new ArrayList<>();
                    for (ServicioFisicoModel m : servicios) dtos.add(toDto(m));
                    Response<Void> insResp = api.insertServiciosFisicos(dtos).execute();
                    if (!insResp.isSuccessful()) {
                        notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                        return;
                    }
                    total += dtos.size();
                }


                List<TercerosCompartidosModel> terceros = db.suscripcionDao().getAllTercerosSync();
                if (!terceros.isEmpty()) {
                    List<TerceroCompartidoDto> dtos = new ArrayList<>();
                    for (TercerosCompartidosModel m : terceros) dtos.add(toDto(m));
                    Response<Void> insResp = api.insertTercerosCompartidos(dtos).execute();
                    if (!insResp.isSuccessful()) {
                        notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                        return;
                    }
                    total += dtos.size();
                }


                List<RegistrosPagoModel> registros = db.suscripcionDao().getAllRegistrosPagoSyncValidos();
                if (!registros.isEmpty()) {
                    List<RegistroPagoDto> dtos = new ArrayList<>();
                    for (RegistrosPagoModel m : registros) dtos.add(toDto(m));
                    Response<Void> insResp = api.insertRegistrosPago(dtos).execute();
                    if (!insResp.isSuccessful()) {
                        notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                        return;
                    }
                    total += dtos.size();
                }


                ConfiguracionAppModel configuracion = db
                    .suscripcionDao()
                    .getConfiguracionSync();
                if (configuracion != null) {
                    ConfiguracionAppDto confDto = toDto(configuracion);
                    Response<Void> confResp = api
                        .updateConfiguracion(userFilter, confDto)
                        .execute();
                    if (!confResp.isSuccessful() && (confResp.code() == 404 || confResp.code() == 400 || confResp.code() == 406)) {
                        confResp = api.insertConfiguracion(confDto).execute();
                    }
                }


                String ahora = Instant.now().toString();
                actualizarUltimaSincronizacion(ahora);


                final boolean isSuccess = true;

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
                notifyCallback(
                    callback,
                    SyncStatus.ERROR,
                    SERVER_CONNECTION_ERROR
                );
            } finally {

                isSyncRunning.set(false);
                mainHandler.post(() -> {
                    if (Boolean.TRUE.equals(isSyncing.getValue())) {
                        isSyncing.setValue(false);


                    }
                });
            }
        });
    }











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
            mainHandler.post(() -> {
                isSyncing.setValue(true);
                syncResult.setValue(null);
            });
            try {
                RemoteUserResolution resolution = resolveRemoteUserId();
                if (!resolution.isSuccess()) {
                    notifyCallback(callback, resolution.status, resolution.message);
                    return;
                }
                RemoteUserResolution premiumResolution = verifyRemotePremium();
                if (!premiumResolution.isSuccess()) {
                    notifyCallback(callback, premiumResolution.status, premiumResolution.message);
                    return;
                }
                long userId = premiumResolution.userId;
                String userFilter = "eq." + userId;


                Response<List<SuscripcionDto>> susResp = api
                    .getSuscripciones(userFilter)
                    .execute();
                if (!susResp.isSuccessful() || susResp.body() == null) {
                    notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                    return;
                }


                Response<List<ServicioFisicoDto>> srvResp = api
                    .getServiciosFisicos(userFilter)
                    .execute();
                if (!srvResp.isSuccessful() || srvResp.body() == null) {
                    notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                    return;
                }

                Response<List<TerceroCompartidoDto>> terResp = api
                    .getTercerosCompartidos(userFilter)
                    .execute();
                if (!terResp.isSuccessful() || terResp.body() == null) {
                    notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                    return;
                }

                Response<List<RegistroPagoDto>> regResp = api
                    .getRegistrosPago(userFilter)
                    .execute();
                if (!regResp.isSuccessful() || regResp.body() == null) {
                    notifyCallback(callback, SyncStatus.ERROR, SERVER_CONNECTION_ERROR);
                    return;
                }

                List<SuscripcionModel> suscripciones = new ArrayList<>();
                for (SuscripcionDto dto : susResp.body()) {
                    suscripciones.add(toModel(dto));
                }

                List<ServicioFisicoModel> servicios = new ArrayList<>();
                for (ServicioFisicoDto dto : srvResp.body()) {
                    servicios.add(toModel(dto));
                }

                List<TercerosCompartidosModel> terceros = new ArrayList<>();
                for (TerceroCompartidoDto dto : terResp.body()) {
                    terceros.add(toModel(dto));
                }

                List<RegistrosPagoModel> registros = new ArrayList<>();
                for (RegistroPagoDto dto : regResp.body()) {
                    registros.add(toModel(dto));
                }

                db.suscripcionDao().deleteAllRegistrosPago();
                db.suscripcionDao().deleteAllTerceros();
                db.suscripcionDao().deleteAllServiciosFisicos();
                db.suscripcionDao().deleteAllSuscripciones();

                db.suscripcionDao().insertAllSuscripciones(suscripciones);
                db.suscripcionDao().insertAllServiciosFisicos(servicios);
                db.suscripcionDao().insertAllTerceros(terceros);
                db.suscripcionDao().insertAllRegistrosPago(registros);

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
                    SERVER_CONNECTION_ERROR
                );
            } finally {
                mainHandler.post(() -> {
                    if (Boolean.TRUE.equals(isSyncing.getValue())) {
                        isSyncing.setValue(false);
                    }
                });
            }
        });
    }





    private void actualizarUltimaSincronizacion(String timestamp) {

        ConfiguracionAppModel config = db
            .suscripcionDao()
            .getConfiguracionSync();
        if (config == null) {
            config = new ConfiguracionAppModel();
        }
        config.setUltimaSincronizacion(timestamp);
        db.suscripcionDao().upsertConfiguracion(config);


        try {
            long userId = session.getRemoteUserId();
            String userFilter = "eq." + userId;

            ConfiguracionAppDto dto = new ConfiguracionAppDto();
            if (userId != -1) dto.usuarioId = userId;
            dto.ultimaSincronizacion = timestamp;


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

        mainHandler.post(() -> {
            if (status == SyncStatus.SUCCESS) {
                syncResult.setValue("SUCCESS");
            } else {
                syncResult.setValue("ERROR");
            }
        });


        if (callback == null) return;
        mainHandler.post(() -> callback.onResult(status, message));
    }





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

    private ConfiguracionAppDto toDto(ConfiguracionAppModel m) {
        ConfiguracionAppDto dto = new ConfiguracionAppDto();
        dto.id = (long) m.getId();
        long userId = session.getRemoteUserId();
        if (userId != -1) dto.usuarioId = userId;
        dto.notificacionesHabilitadas = m.isNotificacionesHabilitadas();
        dto.horaNotificacion = m.getHoraNotificacion();
        dto.minutoNotificacion = m.getMinutoNotificacion();
        dto.tonoNotificacion = m.getTonoNotificacion();
        dto.ultimaSincronizacion = m.getUltimaSincronizacion();
        return dto;
    }





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

    private TercerosCompartidosModel toModel(TerceroCompartidoDto dto) {
        TercerosCompartidosModel m = new TercerosCompartidosModel();
        if (dto.id != null) m.setId(dto.id.intValue());
        m.setServicioId(dto.servicioId != null ? dto.servicioId.intValue() : 0);
        m.setNombreTercero(dto.nombreTercero != null ? dto.nombreTercero : "");
        m.setMontoAportacion(dto.montoAportacion != null ? dto.montoAportacion : 0.0);
        m.setCreadoEn(dto.creadoEn != null ? dto.creadoEn : "");
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
