package com.example.submanager.ui.viewmodel;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.repository.SuscripcionRepository;

import java.util.List;

public class SuscripcionViewModel extends AndroidViewModel {

    private SuscripcionRepository repository;

    private LiveData<List<SuscripcionModel>> todasLasSuscripciones;

    public SuscripcionViewModel(@NonNull Application application) {
        super(application);
        repository = new SuscripcionRepository(application);

        todasLasSuscripciones = repository.getTodasLasSuscripciones();
    }

    public LiveData<List<SuscripcionModel>> getTodasLasSuscripciones() {
        return todasLasSuscripciones;
    }

    public void insertar(SuscripcionModel suscripcion) {
        repository.insertar(suscripcion);
    }


}