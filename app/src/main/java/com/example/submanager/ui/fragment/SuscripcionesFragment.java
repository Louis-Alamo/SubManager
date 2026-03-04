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
import com.google.android.material.chip.ChipGroup; // ✨ No olvides este import

import java.util.ArrayList;
import java.util.List;

public class SuscripcionesFragment extends Fragment {

    private RecyclerView rvSuscripciones;
    private SuscripcionAdapter adaptador;
    private List<SuscripcionModel> listaMaestra;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suscripciones, container, false);

        rvSuscripciones = view.findViewById(R.id.rvSuscripciones);
        rvSuscripciones.setLayoutManager(new LinearLayoutManager(getContext()));


        listaMaestra = new ArrayList<>();
// Netflix Premium
        listaMaestra.add(new SuscripcionModel(
                "Netflix Premium",
                199.00,
                "Mensual",
                "#E50914",           // color rojo Netflix
                "Entretenimiento",
                "Tarjeta de crédito",
                "15 Oct 2025",       // fechaPrimerCobro
                "15 Mar 2026",       // fechaProximoCobro
                "12 Mar 2026",       // fechaLimiteCancelacion
                true,                // recordatorioHabilitado
                3,                   // diasAnticipacion
                false,               // notificacionSilenciada
                true,                // estaActiva
                R.drawable.ic_app_netflix, // icono (ejemplo)
                "2025-10-15T10:00:00", // creadoEn
                "2026-02-15T10:00:00"  // actualizadoEn
        ));

// Spotify Duo
        listaMaestra.add(new SuscripcionModel(
                "Spotify Duo",
                129.00,
                "Mensual",
                "#1DB954",
                "Música",
                "Tarjeta de débito",
                "20 Oct 2025",
                "20 Mar 2026",
                "17 Mar 2026",
                true,
                3,
                false,
                true,
                R.drawable.ic_app_spotify,   // icono
                "2025-10-20T10:00:00",
                "2026-02-20T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "GitHub Copilot",
                200.00,
                "Mensual",
                "#24292F",
                "Software",
                "Tarjeta de crédito",
                "15 Oct 2025",
                "15 Mar 2026",
                "12 Mar 2026",
                true,
                3,
                false,
                true,
                R.drawable.ic_app_copilot, // icono
                "2025-10-15T10:00:00",
                "2026-02-15T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "Amazon Prime",
                99.00,
                "Mensual",
                "#00A8E0",
                "Entretenimiento",
                "Tarjeta de crédito",
                "02 Nov 2025",
                "02 Apr 2026",
                "30 Mar 2026",
                true,
                3,
                false,
                true,
                R.drawable.ic_app_prime_video, // icono
                "2025-11-02T10:00:00",
                "2026-02-02T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "YouTube Premium",
                139.00,
                "Mensual",
                "#FF0000",
                "Entretenimiento",
                "PayPal",
                "05 Nov 2025",
                "05 Apr 2026",
                "02 Apr 2026",
                true,
                5,
                false,
                true,
                R.drawable.ic_app_youtube, // icono
                "2025-11-05T10:00:00",
                "2026-02-05T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "Disney+",
                179.00,
                "Mensual",
                "#0C3483",
                "Entretenimiento",
                "Tarjeta de crédito",
                "10 Nov 2025",
                "10 Apr 2026",
                "07 Apr 2026",
                true,
                3,
                false,
                true,
                R.drawable.ic_app_disneyplus, // icono
                "2025-11-10T10:00:00",
                "2026-02-10T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "Apple Music",
                129.00,
                "Mensual",
                "#FC3C44",
                "Música",
                "Tarjeta de débito",
                "18 Nov 2025",
                "18 Apr 2026",
                "15 Apr 2026",
                true,
                3,
                false,
                true,
                R.drawable.ic_app_apple_music, // icono
                "2025-11-18T10:00:00",
                "2026-02-18T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "Xbox Game Pass",
                249.00,
                "Mensual",
                "#107C10",
                "Videojuegos",
                "Tarjeta de crédito",
                "21 Nov 2025",
                "21 Apr 2026",
                "18 Apr 2026",
                true,
                7,
                false,
                true,
                R.drawable.ic_app_xbox, // icono
                "2025-11-21T10:00:00",
                "2026-02-21T10:00:00"
        ));

        listaMaestra.add(new SuscripcionModel(
                "Google One",
                39.00,
                "Mensual",
                "#4285F4",
                "Productividad",
                "Tarjeta de débito",
                "25 Nov 2025",
                "25 Apr 2026",
                "22 Apr 2026",
                false,
                3,
                true,
                true,
                R.drawable.ic_app_google, // icono
                "2025-11-25T10:00:00",
                "2026-02-25T10:00:00"
        ));


        adaptador = new SuscripcionAdapter(listaMaestra);
        rvSuscripciones.setAdapter(adaptador);

        ChipGroup cgCategorias = view.findViewById(R.id.cgCategorias);

        cgCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int idSeleccionado = checkedIds.get(0);
            List<SuscripcionModel> listaFiltrada = new ArrayList<>();

            if (idSeleccionado == R.id.chipTodas) {
                listaFiltrada.addAll(listaMaestra);

            } else if (idSeleccionado == R.id.chipEntretenimiento) {
                for (SuscripcionModel sub : listaMaestra) {
                    if (sub.getCategoria().equals("Entretenimiento")) {
                        listaFiltrada.add(sub);
                    }
                }

            } else if (idSeleccionado == R.id.chipMusica) {
                for (SuscripcionModel sub : listaMaestra) {
                    if (sub.getCategoria().equals("Música")) {
                        listaFiltrada.add(sub);
                    }
                }
            }

            adaptador.actualizarLista(listaFiltrada);
        });

        return view;
    }
}