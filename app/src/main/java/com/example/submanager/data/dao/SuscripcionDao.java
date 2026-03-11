package com.example.submanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.submanager.data.model.SuscripcionModel;

import java.util.List;

@Dao
public interface SuscripcionDao {

    @Query("SELECT * FROM suscripciones")
    LiveData<List<SuscripcionModel>> getAllSuscripciones();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1")
    LiveData<List<SuscripcionModel>> getSuscripcionesActivas();

    @Query("SELECT * FROM suscripciones WHERE esta_activa = 1 AND fecha_proximo_cobro BETWEEN :startDate AND :endDate ORDER BY fecha_proximo_cobro ASC")
    LiveData<List<SuscripcionModel>> getSuscripcionesProximasAVencer(String startDate, String endDate);

    @Insert
    void insertarSuscripcion(SuscripcionModel suscripcion);

}
