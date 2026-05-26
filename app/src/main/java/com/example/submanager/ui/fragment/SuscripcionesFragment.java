package com.example.submanager.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.submanager.ui.activity.NuevaSuscripcionActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.SearchView;

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
import com.example.submanager.utils.CategoryManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SuscripcionesFragment extends Fragment {

    private RecyclerView rvSuscripciones;
    private SuscripcionAdapter adaptador;
    private ChipGroup cgCategorias;
    private SearchView svBuscar;
    private String currentQuery = "";

    // El cerebro de nuestra pantalla
    private SuscripcionViewModel viewModel;

    // Aquí guardaremos la copia fresca que nos mande la base de datos
    private List<SuscripcionModel> listaMaestra = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suscripciones, container, false);

        rvSuscripciones = view.findViewById(R.id.rvSuscripciones);
        rvSuscripciones.setLayoutManager(new LinearLayoutManager(getContext()));
        cgCategorias = view.findViewById(R.id.cgCategorias);
        CategoryManager.setupCategoryChips(cgCategorias, getContext(), true, "Todas");

        adaptador = new SuscripcionAdapter(new ArrayList<>());
        rvSuscripciones.setAdapter(adaptador);

        // Botón flotante → abrir pantalla de nueva suscripción
        FloatingActionButton fabAgregar = view.findViewById(R.id.fabAgregar);
        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevaSuscripcionActivity.class);
            startActivity(intent);
        });

        // Configurar el buscador
        svBuscar = view.findViewById(R.id.svBuscar);
        svBuscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                svBuscar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText.trim();
                aplicarFiltroActual();
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Instanciamos el ViewModel
        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        // 2. Observamos la base de datos de Room en tiempo real
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
        if (listaMaestra.isEmpty())
            return;

        List<Integer> checkedIds = cgCategorias.getCheckedChipIds();
        if (checkedIds.isEmpty())
            return;

        int idSeleccionado = checkedIds.get(0);
        Chip chipSeleccionado = cgCategorias.findViewById(idSeleccionado);
        if (chipSeleccionado == null) return;
        String categoriaSeleccionada = chipSeleccionado.getText().toString();

        List<SuscripcionModel> listaFiltrada = new ArrayList<>();

        if ("Todas".equals(categoriaSeleccionada)) {
            for (SuscripcionModel sub : listaMaestra) {
                if (currentQuery.isEmpty() || sub.getNombre().toLowerCase().contains(currentQuery.toLowerCase())) {
                    listaFiltrada.add(sub);
                }
            }
        } else {
            for (SuscripcionModel sub : listaMaestra) {
                if (sub.getCategoria().equals(categoriaSeleccionada)) {
                    if (currentQuery.isEmpty() || sub.getNombre().toLowerCase().contains(currentQuery.toLowerCase())) {
                        listaFiltrada.add(sub);
                    }
                }
            }
        }

        // Finalmente, mandamos la lista filtrada al RecyclerView
        adaptador.actualizarLista(listaFiltrada);
    }
}
