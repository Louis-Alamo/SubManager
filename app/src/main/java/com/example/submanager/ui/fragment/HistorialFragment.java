package com.example.submanager.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {

    // ─── Mock model ─────────────────────────────────────────────────────────
    static class PagoMock {
        String nombre, fechaCategoria, monto, colorHex, categoria;
        int iconRes;
        // categoria: "digital" | "hogar"
        PagoMock(String nombre, String fechaCategoria, String monto,
                 String colorHex, int iconRes, String categoria) {
            this.nombre        = nombre;
            this.fechaCategoria = fechaCategoria;
            this.monto         = monto;
            this.colorHex      = colorHex;
            this.iconRes       = iconRes;
            this.categoria     = categoria;
        }
    }

    // ─── Vistas ─────────────────────────────────────────────────────────────
    private PieChart pieChart;
    private TextView tvMesSeleccionado;
    private RecyclerView rvPagosMes;
    private View emptyState;
    private PagoAdapter adapter;
    private List<PagoMock> allPagos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupMenu();
        setupDonutChart();
        setupRecyclerView();
        setupChips(view);
        setupMesNavegacion();
    }

    private void bindViews(@NonNull View root) {
        pieChart           = root.findViewById(R.id.pieChart);
        tvMesSeleccionado  = root.findViewById(R.id.tvMesSeleccionado);
        rvPagosMes         = root.findViewById(R.id.rvPagosMes);
        emptyState         = root.findViewById(R.id.emptyStateHistorial);
    }

    // ─── Menú ⋮ (Exportar CSV / PDF) ────────────────────────────────────────
    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                menu.add(Menu.NONE, 1, Menu.NONE, R.string.history_export_csv)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.add(Menu.NONE, 2, Menu.NONE, R.string.history_export_pdf)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == 1) {
                    Snackbar.make(requireView(), R.string.history_export_success, Snackbar.LENGTH_SHORT).show();
                } else if (item.getItemId() == 2) {
                    Snackbar.make(requireView(), R.string.history_export_success, Snackbar.LENGTH_SHORT).show();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    // ─── Gráfica de dona ────────────────────────────────────────────────────
    private void setupDonutChart() {
        List<PieEntry> entradas = new ArrayList<>();
        entradas.add(new PieEntry(65f, getString(R.string.history_chart_subscriptions)));
        entradas.add(new PieEntry(25f, getString(R.string.history_chart_services)));
        entradas.add(new PieEntry(10f, getString(R.string.history_chart_others)));

        PieDataSet dataSet = new PieDataSet(entradas, "");
        dataSet.setColors(
                Color.parseColor("#2563EB"),   // azul primario — Suscripciones
                Color.parseColor("#60A5FA"),   // azul claro    — Servicios
                Color.parseColor("#BFDBFE")    // azul muy claro — Otros
        );
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(62f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Marzo\n2026");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#0F172A"));
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // ─── RecyclerView con datos mock ────────────────────────────────────────
    private void setupRecyclerView() {
        allPagos = getMockPagos();
        adapter = new PagoAdapter(new ArrayList<>(allPagos));
        rvPagosMes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPagosMes.setAdapter(adapter);
        rvPagosMes.setNestedScrollingEnabled(false);
        actualizarVista(allPagos);
    }

    private void actualizarVista(List<PagoMock> filtrados) {
        adapter.updateItems(filtrados);
        boolean sinDatos = filtrados.isEmpty();
        rvPagosMes.setVisibility(sinDatos ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(sinDatos ? View.VISIBLE : View.GONE);
    }

    // ─── Chips de filtro ────────────────────────────────────────────────────
    private void setupChips(@NonNull View root) {
        ChipGroup chipGroup = root.findViewById(R.id.cgFiltros);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || allPagos == null) return;
            int id = checkedIds.get(0);
            List<PagoMock> filtrados;
            if (id == R.id.chipDigital) {
                filtrados = filtrarPor("digital");
            } else if (id == R.id.chipHogar) {
                filtrados = filtrarPor("hogar");
            } else {
                filtrados = new ArrayList<>(allPagos);
            }
            actualizarVista(filtrados);
        });
    }

    private List<PagoMock> filtrarPor(String categoria) {
        List<PagoMock> resultado = new ArrayList<>();
        for (PagoMock p : allPagos) {
            if (categoria.equals(p.categoria)) resultado.add(p);
        }
        return resultado;
    }

    // ─── Navegación de mes con flechas ──────────────────────────────────────
    private void setupMesNavegacion() {
        View btnAnterior  = requireView().findViewById(R.id.btnMesAnterior);
        View btnSiguiente = requireView().findViewById(R.id.btnMesSiguiente);

        btnAnterior.setOnClickListener(v ->
                Snackbar.make(requireView(), "Mes anterior", Snackbar.LENGTH_SHORT).show());
        btnSiguiente.setOnClickListener(v ->
                Snackbar.make(requireView(), "Mes siguiente", Snackbar.LENGTH_SHORT).show());
    }

    // ─── Datos mock ─────────────────────────────────────────────────────────
    private List<PagoMock> getMockPagos() {
        List<PagoMock> lista = new ArrayList<>();
        // ── Digital ─────────────────────────────────────────────────────────
        lista.add(new PagoMock("Netflix",         "1 Mar · Streaming",        "-$199.00", "#EF4444", R.drawable.ic_app_netflix,         "digital"));
        lista.add(new PagoMock("Spotify",          "2 Mar · Música",           "-$129.00", "#22C55E", R.drawable.ic_app_spotify,          "digital"));
        lista.add(new PagoMock("Xbox Game Pass",   "3 Mar · Gaming",           "-$249.00", "#107C10", R.drawable.ic_app_xbox,             "digital"));
        lista.add(new PagoMock("Disney+",          "5 Mar · Streaming",        "-$159.00", "#0064FF", R.drawable.ic_app_disneyplus,       "digital"));
        lista.add(new PagoMock("Prime Video",      "6 Mar · Streaming",        "-$99.00",  "#00A8E0", R.drawable.ic_app_prime_video,      "digital"));
        lista.add(new PagoMock("YouTube Premium",  "8 Mar · Entretenimiento",  "-$139.00", "#FF0000", R.drawable.ic_app_youtube,          "digital"));
        lista.add(new PagoMock("HBO Max",          "10 Mar · Streaming",       "-$179.00", "#00B4F5", R.drawable.ic_app_hbomax,           "digital"));
        lista.add(new PagoMock("Twitch",           "12 Mar · Gaming",          "-$59.00",  "#9146FF", R.drawable.ic_app_twitch,           "digital"));
        lista.add(new PagoMock("Duolingo Plus",    "14 Mar · Educación",       "-$89.00",  "#58CC02", R.drawable.ic_app_duolingo,         "digital"));
        lista.add(new PagoMock("Mercado Libre +",  "16 Mar · Compras",         "-$99.00",  "#FFE600", R.drawable.ic_app_mercadolibre,     "digital"));
        lista.add(new PagoMock("Microsoft Copilot","18 Mar · Software",        "-$200.00", "#0078D4", R.drawable.ic_app_copilot,          "digital"));
        lista.add(new PagoMock("Google One",       "20 Mar · Almacenamiento",  "-$35.00",  "#4285F4", R.drawable.ic_app_google,           "digital"));
        // ── Hogar ────────────────────────────────────────────────────────────
        lista.add(new PagoMock("Luz Eléctrica",   "4 Mar · Hogar",            "-$640.00", "#F59E0B", R.drawable.ic_service_electricity,  "hogar"));
        lista.add(new PagoMock("Internet",         "7 Mar · Hogar",            "-$450.00", "#3B82F6", R.drawable.ic_service_internet,     "hogar"));
        lista.add(new PagoMock("Agua",             "9 Mar · Hogar",            "-$180.00", "#0EA5E9", R.drawable.ic_service_water,        "hogar"));
        lista.add(new PagoMock("Gas",              "13 Mar · Hogar",           "-$320.00", "#F97316", R.drawable.ic_service_gas,          "hogar"));
        lista.add(new PagoMock("Teléfono Celular", "17 Mar · Hogar",           "-$199.00", "#22C55E", R.drawable.ic_service_phone,        "hogar"));
        return lista;
    }

    // ─── Adapter interno ────────────────────────────────────────────────────
    private static class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.VH> {

        private final List<PagoMock> items;
        PagoAdapter(List<PagoMock> items) { this.items = items; }

        void updateItems(List<PagoMock> nuevos) {
            items.clear();
            items.addAll(nuevos);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_historial_pago, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PagoMock p = items.get(pos);
            h.tvNombre.setText(p.nombre);
            h.tvFechaCategoria.setText(p.fechaCategoria);
            h.tvMonto.setText(p.monto);
            if (p.iconRes != 0) {
                // Icono propio: sin fondo, se muestra tal cual
                h.ivAppIcon.setBackground(null);
                h.ivAppIcon.setImageResource(p.iconRes);
                h.ivAppIcon.setPadding(0, 0, 0, 0);
            } else {
                // Sin icono: fondo circular de color sólido
                android.graphics.drawable.GradientDrawable circle =
                        new android.graphics.drawable.GradientDrawable();
                circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                try { circle.setColor(Color.parseColor(p.colorHex)); }
                catch (Exception ignore) { circle.setColor(Color.LTGRAY); }
                h.ivAppIcon.setBackground(circle);
                h.ivAppIcon.setImageDrawable(null);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            android.widget.ImageView ivAppIcon;
            TextView tvNombre, tvFechaCategoria, tvMonto;
            VH(@NonNull View v) {
                super(v);
                ivAppIcon        = v.findViewById(R.id.ivAppIcon);
                tvNombre         = v.findViewById(R.id.tvNombre);
                tvFechaCategoria = v.findViewById(R.id.tvFechaCategoria);
                tvMonto          = v.findViewById(R.id.tvMonto);
            }
        }
    }
}
