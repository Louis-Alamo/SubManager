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
        String nombre, fechaCategoria, monto, colorHex;
        int iconRes; // 0 = sin ícono, usa solo color de fondo
        PagoMock(String nombre, String fechaCategoria, String monto, String colorHex, int iconRes) {
            this.nombre = nombre;
            this.fechaCategoria = fechaCategoria;
            this.monto = monto;
            this.colorHex = colorHex;
            this.iconRes = iconRes;
        }
    }

    // ─── Vistas ─────────────────────────────────────────────────────────────
    private PieChart pieChart;
    private TextView tvMesSeleccionado;
    private RecyclerView rvPagosMes;
    private View emptyState;

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
        List<PagoMock> pagos = getMockPagos();
        rvPagosMes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPagosMes.setAdapter(new PagoAdapter(pagos));
        rvPagosMes.setNestedScrollingEnabled(false);

        boolean sinDatos = pagos.isEmpty();
        rvPagosMes.setVisibility(sinDatos ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(sinDatos ? View.VISIBLE : View.GONE);
    }

    // ─── Chips de filtro ────────────────────────────────────────────────────
    private void setupChips(@NonNull View root) {
        ChipGroup chipGroup = root.findViewById(R.id.cgFiltros);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Aquí se filtraría la lista y se actualizaría el chart
        });
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
        lista.add(new PagoMock("Netflix",       "5 Mar · Entretenimiento",  "-$199.00", "#EF4444", R.drawable.ic_app_netflix));
        lista.add(new PagoMock("Spotify",        "8 Mar · Música",           "-$129.00", "#22C55E", R.drawable.ic_app_spotify));
        lista.add(new PagoMock("Xbox Game Pass", "10 Mar · Gaming",          "-$249.00", "#3B82F6", R.drawable.ic_app_xbox));
        lista.add(new PagoMock("Luz Eléctrica",  "15 Mar · Hogar",           "-$640.00", "#F97316", 0));
        lista.add(new PagoMock("YouTube Premium","18 Mar · Entretenimiento", "-$139.00", "#EC4899", R.drawable.ic_app_youtube));
        lista.add(new PagoMock("Internet",       "20 Mar · Hogar",           "-$450.00", "#7C3AED", 0));
        return lista;
    }

    // ─── Adapter interno ────────────────────────────────────────────────────
    private static class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.VH> {

        private final List<PagoMock> items;
        PagoAdapter(List<PagoMock> items) { this.items = items; }

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
