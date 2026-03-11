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

    private SuscripcionDao suscripcionDao;
    private LiveData<List<SuscripcionModel>> todasLasSuscripciones;

    // Usamos un ExecutorService para mandar las operaciones de escritura a un hilo secundario
    // (Android prohíbe escribir en la base de datos en el hilo principal)
    private ExecutorService executorService;

    public SuscripcionRepository(Application application) {
        // Conectamos con nuestro "Gerente General" de la base de datos
        AppDatabase database = AppDatabase.getInstance(application);
        suscripcionDao = database.suscripcionDao();

        // Cargamos la lista reactiva desde el DAO
        todasLasSuscripciones = suscripcionDao.getAllSuscripciones();

        // Preparamos 2 hilos trabajadores en el fondo
        executorService = Executors.newFixedThreadPool(2);
    }

    // ─── LECTURA (Van en el hilo principal porque LiveData ya es asíncrono) ───

    public LiveData<List<SuscripcionModel>> getTodasLasSuscripciones() {
        return todasLasSuscripciones;
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesActivas() {
        return suscripcionDao.getSuscripcionesActivas();
    }

    public LiveData<List<SuscripcionModel>> getSuscripcionesProximasAVencer(String startDate, String endDate) {
        return suscripcionDao.getSuscripcionesProximasAVencer(startDate, endDate);
    }

    // ─── ESCRITURA (Obligatorio mandarlas al hilo secundario) ───

    public void insertar(SuscripcionModel suscripcion) {
        executorService.execute(() -> suscripcionDao.insertarSuscripcion(suscripcion));
    }


}