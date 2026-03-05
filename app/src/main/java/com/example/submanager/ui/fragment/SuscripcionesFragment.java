package com.example.submanager.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.adapter.SuscripcionAdapter;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SuscripcionesFragment extends Fragment {

    private RecyclerView rvSuscripciones;
    private SuscripcionAdapter adaptador;
    private ChipGroup cgCategorias;

    // El cerebro de nuestra pantalla
    private SuscripcionViewModel viewModel;

    // Aquí guardaremos la copia fresca que nos mande la base de datos
    private List<SuscripcionModel> listaMaestra = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Solo inflamos la vista y conectamos los IDs XML
        View view = inflater.inflate(R.layout.fragment_suscripciones, container, false);

        rvSuscripciones = view.findViewById(R.id.rvSuscripciones);
        rvSuscripciones.setLayoutManager(new LinearLayoutManager(getContext()));
        cgCategorias = view.findViewById(R.id.cgCategorias);

        // Iniciamos el adaptador vacío por ahora
        adaptador = new SuscripcionAdapter(new ArrayList<>());
        rvSuscripciones.setAdapter(adaptador);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Instanciamos el ViewModel
        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        // 2. Observamos la base de datos de Room en tiempo real 🪄
        viewModel.getTodasLasSuscripciones().observe(getViewLifecycleOwner(), suscripciones -> {
            if (suscripciones != null) {
                // Actualizamos nuestra lista maestra con los datos reales de SQLite
                listaMaestra.clear();
                listaMaestra.addAll(suscripciones);

                // Aplicamos el filtro dependiendo de qué Chip esté seleccionado en este momento
                aplicarFiltroActual();
            }
        });

        // 3. Escuchamos los clics en los Chips
        cgCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                aplicarFiltroActual();
            }
        });
    }

    // Método auxiliar con tu lógica exacta de filtrado
    private void aplicarFiltroActual() {
        if (listaMaestra.isEmpty()) return;

        List<Integer> checkedIds = cgCategorias.getCheckedChipIds();
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
        // ¡Aquí puedes agregar más else-if para Videojuegos, Software, etc.!

        // Finalmente, mandamos la lista filtrada al RecyclerView
        adaptador.actualizarLista(listaFiltrada);
    }
}