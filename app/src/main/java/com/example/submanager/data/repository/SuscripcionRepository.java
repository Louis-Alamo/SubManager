package com.example.submanager.data.repository;


import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.dao.SuscripcionDao;
import com.example.submanager.data.model.SuscripcionModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuscripcionRepository {

    private final SuscripcionDao suscripcionDao;
    private final LiveData<List<SuscripcionModel>> todasLasSuscripciones;
    private final LiveData<List<SuscripcionModel>> suscripcionesActivasOrdenadas;
    private final LiveData<List<SuscripcionModel>> suscripcionesProximas;
    private final LiveData<Double> montoTotalActivas;

    // Usamos un ExecutorService para mandar las operaciones de escritura a un hilo secundario
    // (Android prohíbe escribir en la base de datos en el hilo principal)
    private final ExecutorService executorService;

    public SuscripcionRepository(Application application) {
        // Conectamos con nuestro "Gerente General" de la base de datos
        AppDatabase database = AppDatabase.getInstance(application);
        suscripcionDao = database.suscripcionDao();

        // Cargamos las listas reactivas desde el DAO
        todasLasSuscripciones = suscripcionDao.getAllSuscripciones();
        suscripcionesActivasOrdenadas = suscripcionDao.getSuscripcionesActivasOrdenadas();
        suscripcionesProximas = suscripcionDao.getSuscripcionesProximas();
        montoTotalActivas = suscripcionDao.getMontoTotalActivas();

        // Preparamos 2 hilos trabajadores en el fondo
        executorService = Executors.newFixedThreadPool(2);
    }

    // ─── LECTURA (Van en el hilo principal porque LiveData ya es asíncrono) ───

    public LiveData<List<SuscripcionModel>> getTodasLasSuscripciones() {
        return todasLasSuscripciones;
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesActivasOrdenadas() {
        return suscripcionesActivasOrdenadas;
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesProximas() {
        return suscripcionesProximas;
    }

    public LiveData<Double> getMontoTotalActivas() {
        return montoTotalActivas;
    }

    // ─── ESCRITURA (Obligatorio mandarlas al hilo secundario) ───

    public void insertar(SuscripcionModel suscripcion) {
        executorService.execute(() -> suscripcionDao.insertarSuscripcion(suscripcion));
    }


}