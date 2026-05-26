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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorialFragment extends Fragment {


    static class PagoHistorial {
        String nombre;
        String fechaUI;
        double monto;
        String colorHex;
        int iconRes;
        String categoria;

        PagoHistorial(String nombre, String fechaUI, double monto, String colorHex, int iconRes, String categoria) {
            this.nombre = nombre;
            this.fechaUI = fechaUI;
            this.monto = monto;
            this.colorHex = colorHex;
            this.iconRes = iconRes;
            this.categoria = categoria;
        }
    }


    private PieChart pieChart;
    private TextView tvMesSeleccionado;
    private RecyclerView rvPagosMes;
    private View emptyState;
    private PagoAdapter adapter;
    private ChipGroup chipGroup;


    private SuscripcionViewModel viewModel;
    private List<SuscripcionModel> suscripcionesCache = new ArrayList<>();
    private List<RegistrosPagoModel> pagosCache = new ArrayList<>();
    private List<PagoHistorial> pagosProcesadosActuales = new ArrayList<>();


    private Calendar mesSeleccionado;

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

        mesSeleccionado = Calendar.getInstance();

        bindViews(view);
        setupMenu();
        setupRecyclerView();
        setupChips();
        setupMesNavegacion();

        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        viewModel.getTodasLasSuscripciones().observe(getViewLifecycleOwner(), suscripciones -> {
            suscripcionesCache = suscripciones != null ? suscripciones : new ArrayList<>();
            procesarDatosDelMes();
        });

        viewModel.getAllRegistrosPagoLiveData().observe(getViewLifecycleOwner(), pagos -> {
            pagosCache = pagos != null ? pagos : new ArrayList<>();
            procesarDatosDelMes();
        });

        actualizarTextoMes();
    }

    private void bindViews(@NonNull View root) {
        pieChart           = root.findViewById(R.id.pieChart);
        tvMesSeleccionado  = root.findViewById(R.id.tvMesSeleccionado);
        rvPagosMes         = root.findViewById(R.id.rvPagosMes);
        emptyState         = root.findViewById(R.id.emptyStateHistorial);
        chipGroup          = root.findViewById(R.id.cgFiltros);
    }


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
                Snackbar.make(requireView(), R.string.history_export_success, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }


    private void procesarDatosDelMes() {
        if (!isAdded()) return;

        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfUI = new SimpleDateFormat("d MMM", Locale.getDefault());

        int targetMonth = mesSeleccionado.get(Calendar.MONTH);
        int targetYear = mesSeleccionado.get(Calendar.YEAR);

        List<PagoHistorial> pagosDelMes = new ArrayList<>();

        for (RegistrosPagoModel pago : pagosCache) {

            String fechaBase = pago.getFechaVencimiento() != null && !pago.getFechaVencimiento().isEmpty() ?
                               pago.getFechaVencimiento() : pago.getFechaPago();

            if (!"Pagado".equalsIgnoreCase(pago.getEstado()) || fechaBase == null) continue;

            try {
                Date d = sdfDB.parse(fechaBase);
                if (d == null) continue;

                Calendar c = Calendar.getInstance();
                c.setTime(d);

                if (c.get(Calendar.MONTH) == targetMonth && c.get(Calendar.YEAR) == targetYear) {
                    SuscripcionModel sub = null;
                    if (pago.getSuscripcionId() != null) {
                        for (SuscripcionModel s : suscripcionesCache) {
                            if (s.getId() == pago.getSuscripcionId()) {
                                sub = s;
                                break;
                            }
                        }
                    }

                    String fechaTxt = sdfUI.format(d) + " · " + pago.getCategoria();
                    int iconRes = R.drawable.ic_service_otro;
                    if (sub != null && sub.getNombreIcono() != null) {
                        iconRes = getResources().getIdentifier(sub.getNombreIcono(), "drawable", requireContext().getPackageName());
                        if (iconRes == 0) iconRes = R.drawable.ic_service_otro;
                    }

                    pagosDelMes.add(new PagoHistorial(
                            pago.getNombreOrigen(),
                            fechaTxt,
                            pago.getMonto(),
                            pago.getColorOrigen(),
                            iconRes,
                            pago.getCategoria()
                    ));
                }
            } catch (Exception ignored) {}
        }

        pagosProcesadosActuales = pagosDelMes;
        aplicarFiltros();
        generarGraficaDona(pagosProcesadosActuales);
    }


    private void generarGraficaDona(List<PagoHistorial> listaPagos) {
        if (listaPagos.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText("Sin\ndatos");
            pieChart.invalidate();
            updateLegend(new ArrayList<>(), new ArrayList<>());
            return;
        }

        Map<String, Double> sumasPorCat = new HashMap<>();
        Map<String, String> colorPorCat = new HashMap<>();

        double total = 0;
        for (PagoHistorial p : listaPagos) {
            double current = sumasPorCat.containsKey(p.categoria) ? sumasPorCat.get(p.categoria) : 0.0;
            sumasPorCat.put(p.categoria, current + p.monto);
            colorPorCat.put(p.categoria, p.colorHex);
            total += p.monto;
        }

        List<Map.Entry<String, Double>> listaOrdenada = new ArrayList<>(sumasPorCat.entrySet());
        Collections.sort(listaOrdenada, (a, b) -> b.getValue().compareTo(a.getValue()));

        List<PieEntry> entradas = new ArrayList<>();
        List<Integer> colores = new ArrayList<>();

        double sumaOtros = 0;

        for (int i = 0; i < listaOrdenada.size(); i++) {
            Map.Entry<String, Double> entry = listaOrdenada.get(i);
            if (i < 3) {
                float porcentaje = (float) ((entry.getValue() / total) * 100);
                entradas.add(new PieEntry(porcentaje, entry.getKey()));
                try {
                    colores.add(Color.parseColor(colorPorCat.get(entry.getKey())));
                } catch (Exception e) {
                    colores.add(Color.parseColor("#60A5FA"));
                }
            } else {
                sumaOtros += entry.getValue();
            }
        }

        if (sumaOtros > 0) {
            float porcentaje = (float) ((sumaOtros / total) * 100);
            entradas.add(new PieEntry(porcentaje, "Otros"));
            colores.add(Color.parseColor("#94A3B8"));
        }

        PieDataSet dataSet = new PieDataSet(entradas, "");
        dataSet.setColors(colores);
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

        SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM\nyyyy", new Locale("es", "ES"));
        pieChart.setCenterText(capitalize(sdfMes.format(mesSeleccionado.getTime())));
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#0F172A"));

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.animateY(800);
        pieChart.invalidate();


        updateLegend(entradas, colores);
    }

    private void updateLegend(List<PieEntry> entradas, List<Integer> colores) {
        View view = getView();
        if (view == null) return;

        LinearLayout llLegend1 = view.findViewById(R.id.llLegend1);
        View vColor1 = view.findViewById(R.id.vLegendColor1);
        TextView tvText1 = view.findViewById(R.id.tvLegendText1);

        LinearLayout llLegend2 = view.findViewById(R.id.llLegend2);
        View vColor2 = view.findViewById(R.id.vLegendColor2);
        TextView tvText2 = view.findViewById(R.id.tvLegendText2);

        LinearLayout llLegend3 = view.findViewById(R.id.llLegend3);
        View vColor3 = view.findViewById(R.id.vLegendColor3);
        TextView tvText3 = view.findViewById(R.id.tvLegendText3);

        if (llLegend1 != null) llLegend1.setVisibility(View.GONE);
        if (llLegend2 != null) llLegend2.setVisibility(View.GONE);
        if (llLegend3 != null) llLegend3.setVisibility(View.GONE);

        if (entradas.size() > 0 && llLegend1 != null) {
            llLegend1.setVisibility(View.VISIBLE);
            tvText1.setText(entradas.get(0).getLabel());
            vColor1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colores.get(0)));
        }
        if (entradas.size() > 1 && llLegend2 != null) {
            llLegend2.setVisibility(View.VISIBLE);
            tvText2.setText(entradas.get(1).getLabel());
            vColor2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colores.get(1)));
        }
        if (entradas.size() > 2 && llLegend3 != null) {
            llLegend3.setVisibility(View.VISIBLE);
            tvText3.setText(entradas.get(2).getLabel());
            vColor3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colores.get(2)));
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    private void setupRecyclerView() {
        adapter = new PagoAdapter(new ArrayList<>());
        rvPagosMes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPagosMes.setAdapter(adapter);
        rvPagosMes.setNestedScrollingEnabled(false);
    }


    private void setupChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        if (pagosProcesadosActuales == null) return;

        List<PagoHistorial> filtrados = new ArrayList<>();
        List<Integer> checkedIds = chipGroup.getCheckedChipIds();

        if (checkedIds.isEmpty() || checkedIds.get(0) == R.id.chipTodos) {
            filtrados.addAll(pagosProcesadosActuales);
        } else {
            int id = checkedIds.get(0);
            for (PagoHistorial p : pagosProcesadosActuales) {
                boolean esHogar = "Hogar".equalsIgnoreCase(p.categoria) || "Agua".equalsIgnoreCase(p.categoria) || "Luz".equalsIgnoreCase(p.categoria) || "Gas".equalsIgnoreCase(p.categoria);
                if (id == R.id.chipDigital && !esHogar) {
                    filtrados.add(p);
                } else if (id == R.id.chipHogar && esHogar) {
                    filtrados.add(p);
                }
            }
        }

        adapter.updateItems(filtrados);
        boolean sinDatos = filtrados.isEmpty();
        rvPagosMes.setVisibility(sinDatos ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(sinDatos ? View.VISIBLE : View.GONE);
    }


    private void setupMesNavegacion() {
        View btnAnterior  = requireView().findViewById(R.id.btnMesAnterior);
        View btnSiguiente = requireView().findViewById(R.id.btnMesSiguiente);

        btnAnterior.setOnClickListener(v -> {
            mesSeleccionado.add(Calendar.MONTH, -1);
            actualizarTextoMes();
            procesarDatosDelMes();
        });

        btnSiguiente.setOnClickListener(v -> {
            mesSeleccionado.add(Calendar.MONTH, 1);
            actualizarTextoMes();
            procesarDatosDelMes();
        });
    }

    private void actualizarTextoMes() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        tvMesSeleccionado.setText(capitalize(sdf.format(mesSeleccionado.getTime())));
    }


    private static class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.VH> {

        private final List<PagoHistorial> items;
        PagoAdapter(List<PagoHistorial> items) { this.items = items; }

        void updateItems(List<PagoHistorial> nuevos) {
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
            PagoHistorial p = items.get(pos);
            h.tvNombre.setText(p.nombre);
            h.tvFechaCategoria.setText(p.fechaUI);
            h.tvMonto.setText(String.format(Locale.getDefault(), "-$%.2f", p.monto));

            if (p.iconRes != 0) {
                h.ivAppIcon.setBackground(null);
                h.ivAppIcon.setImageResource(p.iconRes);
                h.ivAppIcon.setPadding(0, 0, 0, 0);
            } else {
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
