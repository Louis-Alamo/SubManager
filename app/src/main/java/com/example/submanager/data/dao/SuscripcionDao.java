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


    @Insert
    void insertarSuscripcion(SuscripcionModel suscripcion);



}
