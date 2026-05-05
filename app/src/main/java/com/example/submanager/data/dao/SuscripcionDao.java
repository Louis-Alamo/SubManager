package com.example.submanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.submanager.data.model.ConfiguracionAppModel;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.ServicioFisicoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.model.TercerosCompartidosModel;

import java.util.List;

@Dao
public interface SuscripcionDao {

    // ─────────────────────────────────────────────────────────────────────────
    // SUSCRIPCIONES — Reactivo (LiveData)
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM suscripciones")
    LiveData<List<SuscripcionModel>> getAllSuscripciones();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1 ORDER BY fecha_proximo_cobro ASC")
    LiveData<List<SuscripcionModel>> getSuscripcionesActivasOrdenadas();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1 AND fecha_proximo_cobro IS NOT NULL AND fecha_proximo_cobro <> '' ORDER BY fecha_proximo_cobro ASC")
    LiveData<List<SuscripcionModel>> getSuscripcionesProximas();

    @Query("SELECT COALESCE(SUM(monto), 0) FROM suscripciones WHERE esta_activa = 1")
    LiveData<Double> getMontoTotalActivas();

    @Query("SELECT * FROM suscripciones WHERE id = :id LIMIT 1")
    LiveData<SuscripcionModel> getSuscripcionById(int id);

    // ─────────────────────────────────────────────────────────────────────────
    // SUSCRIPCIONES — Síncrono (para sincronización remota)
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM suscripciones")
    List<SuscripcionModel> getAllSuscripcionesSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarSuscripcion(SuscripcionModel suscripcion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSuscripciones(List<SuscripcionModel> suscripciones);

    @Update
    void updateSuscripcion(SuscripcionModel suscripcion);

    @Query("DELETE FROM suscripciones")
    void deleteAllSuscripciones();

    @Query("DELETE FROM suscripciones WHERE id = :id")
    void deleteSuscripcionById(int id);

    // ─────────────────────────────────────────────────────────────────────────
    // SERVICIOS FÍSICOS — Síncrono
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM servicios_fisicos")
    List<ServicioFisicoModel> getAllServiciosFisicosSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllServiciosFisicos(List<ServicioFisicoModel> servicios);

    @Query("DELETE FROM servicios_fisicos")
    void deleteAllServiciosFisicos();

    // ─────────────────────────────────────────────────────────────────────────
    // TERCEROS COMPARTIDOS — Síncrono
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM terceros_compartidos")
    List<TercerosCompartidosModel> getAllTercerosSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTerceros(List<TercerosCompartidosModel> terceros);

    @Query("DELETE FROM terceros_compartidos")
    void deleteAllTerceros();

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTROS DE PAGO — Síncrono y Reactivo
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM registros_pago")
    List<RegistrosPagoModel> getAllRegistrosPagoSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRegistrosPago(List<RegistrosPagoModel> registros);

    @Query("DELETE FROM registros_pago")
    void deleteAllRegistrosPago();

    @Query("SELECT * FROM registros_pago ORDER BY id DESC")
    LiveData<List<RegistrosPagoModel>> getAllRegistrosPagoLiveData();

    @Query("SELECT * FROM registros_pago WHERE suscripcion_id = :suscripcionId ORDER BY id DESC")
    LiveData<List<RegistrosPagoModel>> getPagosBySuscripcion(int suscripcionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRegistroPago(RegistrosPagoModel registro);

    // ─────────────────────────────────────────────────────────────────────────
    // CONFIGURACIÓN APP — Síncrono
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM configuracion_app WHERE id = 1 LIMIT 1")
    ConfiguracionAppModel getConfiguracionSync();

    @Upsert
    void upsertConfiguracion(ConfiguracionAppModel config);

    @Query("UPDATE configuracion_app SET ultima_sincronizacion = :timestamp WHERE id = 1")
    void updateUltimaSincronizacion(String timestamp);
}
