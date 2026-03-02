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
    private List<SuscripcionModel> listaMaestra; // ✨ Nuestra fuente de verdad inmutable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suscripciones, container, false);

        rvSuscripciones = view.findViewById(R.id.rvSuscripciones);
        rvSuscripciones.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Llenamos la Lista Maestra (Esto después vendrá de Room/SQL)
        listaMaestra = new ArrayList<>();
        listaMaestra.add(new SuscripcionModel("Netflix Premium", "Entretenimiento", 199.00, "15 Oct", "Pagado", R.drawable.ic_app_netflix));
        listaMaestra.add(new SuscripcionModel("Spotify Duo", "Música", 129.00, "20 Oct", "Pendiente", R.drawable.ic_app_spotify));
        listaMaestra.add(new SuscripcionModel("Copilot", "Entretenimiento", 199.00, "15 Oct", "Pagado", R.drawable.ic_app_copilot));


        // Entretenimiento
        listaMaestra.add(new SuscripcionModel("Amazon Prime", "Entretenimiento", 99.00, "02 Nov", "Pagado", R.drawable.ic_app_prime_video));
        listaMaestra.add(new SuscripcionModel("YouTube Premium", "Entretenimiento", 139.00, "05 Nov", "Pendiente", R.drawable.ic_app_youtube));
        listaMaestra.add(new SuscripcionModel("Disney+", "Entretenimiento", 179.00, "10 Nov", "Pagado", R.drawable.ic_app_disneyplus));

        // Música
        listaMaestra.add(new SuscripcionModel("Apple Music", "Música", 129.00, "18 Nov", "Pagado", R.drawable.ic_app_apple_music));

        // Videojuegos (¡Para el rato libre!)
        listaMaestra.add(new SuscripcionModel("Xbox Game Pass", "Videojuegos", 249.00, "21 Nov", "Pendiente", R.drawable.ic_app_xbox));

        // Software y Productividad
        listaMaestra.add(new SuscripcionModel("GitHub Copilot", "Software", 200.00, "15 Oct", "Pagado", R.drawable.ic_app_copilot));
        listaMaestra.add(new SuscripcionModel("Google One", "Productividad", 39.00, "25 Nov", "Pagado", R.drawable.ic_app_google));

        // Educación


        // 2. Arrancamos el adaptador pasándole todas las suscripciones al inicio
        adaptador = new SuscripcionAdapter(listaMaestra);
        rvSuscripciones.setAdapter(adaptador);

        // 3. ¡La lógica de los Chips!
        ChipGroup cgCategorias = view.findViewById(R.id.cgCategorias); // Asegúrate de ponerle este ID en tu XML

        cgCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Si el usuario desmarca todo por accidente, no hacemos nada o mostramos todas
            if (checkedIds.isEmpty()) return;

            int idSeleccionado = checkedIds.get(0);
            List<SuscripcionModel> listaFiltrada = new ArrayList<>();

            if (idSeleccionado == R.id.chipTodas) {
                // Copiamos la lista maestra completa
                listaFiltrada.addAll(listaMaestra);

            } else if (idSeleccionado == R.id.chipEntretenimiento) {
                // Buscamos solo las de entretenimiento
                for (SuscripcionModel sub : listaMaestra) {
                    if (sub.getCategoria().equals("Entretenimiento")) {
                        listaFiltrada.add(sub);
                    }
                }

            } else if (idSeleccionado == R.id.chipMusica) {
                // Buscamos solo las de música
                for (SuscripcionModel sub : listaMaestra) {
                    if (sub.getCategoria().equals("Música")) {
                        listaFiltrada.add(sub);
                    }
                }
            }

            // 4. Le mandamos la orden al pintor de actualizar la pantalla
            adaptador.actualizarLista(listaFiltrada);
        });

        return view;
    }
}