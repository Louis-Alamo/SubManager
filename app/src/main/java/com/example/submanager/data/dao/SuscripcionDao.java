package com.example.submanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.submanager.data.model.SuscripcionModel;

import java.util.List;

@Dao
public interface SuscripcionDao {

    @Query("SELECT * FROM suscripciones")
    LiveData<List<SuscripcionModel>> getAllSuscripciones();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1 ORDER BY fecha_proximo_cobro ASC")
    LiveData<List<SuscripcionModel>> getSuscripcionesActivasOrdenadas();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1 AND fecha_proximo_cobro IS NOT NULL AND fecha_proximo_cobro <> '' ORDER BY fecha_proximo_cobro ASC")
    LiveData<List<SuscripcionModel>> getSuscripcionesProximas();

    @Query("SELECT COALESCE(SUM(monto), 0) FROM suscripciones WHERE esta_activa = 1")
    LiveData<Double> getMontoTotalActivas();

    @Insert
    void insertarSuscripcion(SuscripcionModel suscripcion);

}
