package com.example.submanager.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.ui.adapter.SuscripcionAdapter;
import com.example.submanager.data.model.SuscripcionModel;

import java.util.ArrayList;
import java.util.List;

public class SuscripcionesFragment extends Fragment {

    private RecyclerView rvSuscripciones;
    private SuscripcionAdapter adaptador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suscripciones, container, false);

        rvSuscripciones = view.findViewById(R.id.rvSuscripciones);
        rvSuscripciones.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SuscripcionModel> misSuscripciones = new ArrayList<>();
        misSuscripciones.add(new SuscripcionModel("Netflix Premium", "Entretenimiento", 199.00, "15 Oct", "Pagado", R.drawable.ic_app_netflix));
        misSuscripciones.add(new SuscripcionModel("Spotify Duo", "Música", 129.00, "20 Oct", "Pendiente", R.drawable.ic_app_spotify));
        misSuscripciones.add(new SuscripcionModel("Copilot", "Entretenimiento", 199.00, "15 Oct", "Pagado", R.drawable.ic_app_copilot));
        adaptador = new SuscripcionAdapter(misSuscripciones);
        rvSuscripciones.setAdapter(adaptador);

        return view;
    }
}