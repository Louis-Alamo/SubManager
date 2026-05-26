package com.example.submanager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.dao.SuscripcionDao;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.utils.NetworkUtils;
import com.example.submanager.utils.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;








public class SuscripcionRepository {

    private final SuscripcionDao suscripcionDao;
    private final LiveData<List<SuscripcionModel>> todasLasSuscripciones;
    private final LiveData<List<SuscripcionModel>> suscripcionesActivasOrdenadas;
    private final LiveData<List<SuscripcionModel>> suscripcionesProximas;
    private final LiveData<Double> montoTotalActivas;
    private final ExecutorService executorService;


    private final RemoteSyncRepository remoteSyncRepository;
    private final SessionManager sessionManager;
    private final Application application;

    public SuscripcionRepository(Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getInstance(application);
        suscripcionDao = database.suscripcionDao();

        todasLasSuscripciones          = suscripcionDao.getAllSuscripciones();
        suscripcionesActivasOrdenadas   = suscripcionDao.getSuscripcionesActivasOrdenadas();
        suscripcionesProximas           = suscripcionDao.getSuscripcionesProximas();
        montoTotalActivas               = suscripcionDao.getMontoTotalActivas();




        executorService         = Executors.newSingleThreadExecutor();
        remoteSyncRepository    = new RemoteSyncRepository(application);
        sessionManager          = new SessionManager(application);
    }



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

    public LiveData<SuscripcionModel> getSuscripcionById(int id) {
        return suscripcionDao.getSuscripcionById(id);
    }







    public void insertar(SuscripcionModel suscripcion) {
        executorService.execute(() -> {
            suscripcionDao.insertarSuscripcion(suscripcion);
            triggerSyncIfNeeded();
        });
    }




    public void actualizar(SuscripcionModel suscripcion) {
        executorService.execute(() -> {
            suscripcionDao.updateSuscripcion(suscripcion);
            triggerSyncIfNeeded();
        });
    }




    public void eliminar(int id) {
        executorService.execute(() -> {
            suscripcionDao.deleteSuscripcionById(id);
            triggerSyncIfNeeded();
        });
    }



    public LiveData<List<RegistrosPagoModel>> getPagosBySuscripcion(int suscripcionId) {
        return suscripcionDao.getPagosBySuscripcion(suscripcionId);
    }

    public LiveData<List<RegistrosPagoModel>> getAllRegistrosPagoLiveData() {
        return suscripcionDao.getAllRegistrosPagoLiveData();
    }

    public void insertRegistroPago(RegistrosPagoModel pago) {
        executorService.execute(() -> {
            suscripcionDao.insertRegistroPago(pago);
            triggerSyncIfNeeded();
        });
    }











    private void triggerSyncIfNeeded() {
        if (sessionManager.isPremium() && NetworkUtils.isNetworkAvailable(application)) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                remoteSyncRepository.syncAll(null);
            }, 300);
        }
    }
}