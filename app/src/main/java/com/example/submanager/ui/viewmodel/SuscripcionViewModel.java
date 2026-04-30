package com.example.submanager.ui.viewmodel;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.repository.SuscripcionRepository;

import java.util.List;

public class SuscripcionViewModel extends AndroidViewModel {

    private final SuscripcionRepository repository;

    private final LiveData<List<SuscripcionModel>> todasLasSuscripciones;
    private final LiveData<List<SuscripcionModel>> suscripcionesActivasOrdenadas;
    private final LiveData<List<SuscripcionModel>> suscripcionesProximas;
    private final LiveData<Double> montoTotalActivas;

    public SuscripcionViewModel(@NonNull Application application) {
        super(application);
        repository = new SuscripcionRepository(application);

        todasLasSuscripciones = repository.getTodasLasSuscripciones();
        suscripcionesActivasOrdenadas = repository.getSuscripcionesActivasOrdenadas();
        suscripcionesProximas = repository.getSuscripcionesProximas();
        montoTotalActivas = repository.getMontoTotalActivas();
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
        return repository.getSuscripcionById(id);
    }

    public void insertar(SuscripcionModel suscripcion) {
        repository.insertar(suscripcion);
    }

    public void eliminar(int id) {
        repository.eliminar(id);
    }

}