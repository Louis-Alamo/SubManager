package com.example.submanager.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.RegistrosPagoModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertasFragment extends Fragment {


    static final String PENDIENTE = "pendiente";
    static final String PAGADO    = "pagado";
    static final String VENCIDO   = "vencido";


    static class PagoAlerta {
        String nombre, fecha, monto, estado, colorHex;
        int iconRes;
        SuscripcionModel suscripcionOriginal;

        PagoAlerta(String nombre, String fecha, String monto,
                   String estado, String colorHex, int iconRes, SuscripcionModel suscripcionOriginal) {
            this.nombre   = nombre;
            this.fecha    = fecha;
            this.monto    = monto;
            this.estado   = estado;
            this.colorHex = colorHex;
            this.iconRes  = iconRes;
            this.suscripcionOriginal = suscripcionOriginal;
        }
    }


    private RecyclerView rvPendiente, rvVencido, rvPagado;
    private View sectionPendiente, sectionVencido, sectionPagado, emptyState;
    private TextView tvTotalPendiente, tvTotalPagado, tvTotalVencido;

    private SuscripcionViewModel viewModel;
    private List<SuscripcionModel> cachedSuscripciones = new ArrayList<>();
    private List<RegistrosPagoModel> cachedPagos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alertas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        rvPendiente       = view.findViewById(R.id.rvPendiente);
        rvVencido         = view.findViewById(R.id.rvVencido);
        rvPagado          = view.findViewById(R.id.rvPagado);
        sectionPendiente  = view.findViewById(R.id.sectionPendiente);
        sectionVencido    = view.findViewById(R.id.sectionVencido);
        sectionPagado     = view.findViewById(R.id.sectionPagado);
        emptyState        = view.findViewById(R.id.emptyStateAlertas);
        tvTotalPendiente  = view.findViewById(R.id.tvTotalPendiente);
        tvTotalPagado     = view.findViewById(R.id.tvTotalPagado);
        tvTotalVencido    = view.findViewById(R.id.tvTotalVencido);

        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        viewModel.getSuscripcionesActivasOrdenadas().observe(getViewLifecycleOwner(), suscripciones -> {
            cachedSuscripciones = suscripciones != null ? suscripciones : new ArrayList<>();
            actualizarUI();
        });

        viewModel.getAllRegistrosPagoLiveData().observe(getViewLifecycleOwner(), pagos -> {
            cachedPagos = pagos != null ? pagos : new ArrayList<>();
            actualizarUI();
        });
    }

    private void actualizarUI() {
        if (!isAdded()) return;

        List<PagoAlerta> todos = new ArrayList<>();

        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfUI = new SimpleDateFormat("dd MMM", Locale.getDefault());
        Date today = new Date();
        try { today = sdfDB.parse(sdfDB.format(today)); } catch (Exception ignored) {}


        for (SuscripcionModel s : cachedSuscripciones) {
            if (s.getFechaProximoCobro() == null || s.getFechaProximoCobro().isEmpty()) continue;

            try {
                Date fechaCobro = sdfDB.parse(s.getFechaProximoCobro());
                if (fechaCobro == null) continue;

                boolean isVencido = fechaCobro.before(today);
                String estado = isVencido ? VENCIDO : PENDIENTE;
                String prefix = isVencido ? "Venció " : "Vence ";
                String fechaTexto = prefix + sdfUI.format(fechaCobro);

                int iconRes = getResources().getIdentifier(s.getNombreIcono(), "drawable", requireContext().getPackageName());
                if (iconRes == 0) iconRes = R.drawable.ic_service_otro;

                todos.add(new PagoAlerta(s.getNombre(), fechaTexto, String.format(Locale.getDefault(), "%.2f", s.getMonto()), estado, s.getColor(), iconRes, s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        for (RegistrosPagoModel p : cachedPagos) {
            if ("Pagado".equalsIgnoreCase(p.getEstado())) {
                String fechaTexto = "Pagado";
                if (p.getFechaPago() != null && !p.getFechaPago().isEmpty()) {
                    try {
                        Date d = sdfDB.parse(p.getFechaPago());
                        if (d != null) fechaTexto = "Pagado " + sdfUI.format(d);
                    } catch (Exception ignored) {}
                }


                int iconRes = R.drawable.ic_service_otro;
                if (p.getSuscripcionId() != null) {
                    for (SuscripcionModel s : cachedSuscripciones) {
                        if (s.getId() == p.getSuscripcionId()) {
                            iconRes = getResources().getIdentifier(s.getNombreIcono(), "drawable", requireContext().getPackageName());
                            if (iconRes == 0) iconRes = R.drawable.ic_service_otro;
                            break;
                        }
                    }
                }

                todos.add(new PagoAlerta(p.getNombreOrigen(), fechaTexto, String.format(Locale.getDefault(), "%.2f", p.getMonto()), PAGADO, p.getColorOrigen(), iconRes, null));
            }
        }

        List<PagoAlerta> pendientes = filtrar(todos, PENDIENTE);
        List<PagoAlerta> pagados    = filtrar(todos, PAGADO);
        List<PagoAlerta> vencidos   = filtrar(todos, VENCIDO);


        tvTotalPendiente.setText(sumar(pendientes));
        tvTotalPagado.setText(sumar(pagados));
        tvTotalVencido.setText(sumar(vencidos));


        montarLista(rvVencido, vencidos);
        sectionVencido.setVisibility(vencidos.isEmpty() ? View.GONE : View.VISIBLE);


        montarLista(rvPendiente, pendientes);
        sectionPendiente.setVisibility(pendientes.isEmpty() ? View.GONE : View.VISIBLE);


        montarLista(rvPagado, pagados);
        sectionPagado.setVisibility(pagados.isEmpty() ? View.GONE : View.VISIBLE);


        boolean hayAlgo = !pendientes.isEmpty() || !pagados.isEmpty() || !vencidos.isEmpty();
        emptyState.setVisibility(hayAlgo ? View.GONE : View.VISIBLE);
    }

    private void montarLista(RecyclerView rv, List<PagoAlerta> items) {
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new AlertaAdapter(items));
        rv.setNestedScrollingEnabled(false);
    }


    private List<PagoAlerta> filtrar(List<PagoAlerta> todos, String estado) {
        List<PagoAlerta> resultado = new ArrayList<>();
        for (PagoAlerta p : todos) {
            if (estado.equals(p.estado)) resultado.add(p);
        }
        return resultado;
    }

    private String sumar(List<PagoAlerta> lista) {
        double total = 0;
        for (PagoAlerta p : lista) {
            try { total += Double.parseDouble(p.monto.replace("$", "").replace(",", "")); }
            catch (NumberFormatException ignore) {}
        }
        return String.format(Locale.getDefault(), "$%.2f", total);
    }


    private class AlertaAdapter extends RecyclerView.Adapter<AlertaAdapter.VH> {

        private final List<PagoAlerta> items;
        AlertaAdapter(List<PagoAlerta> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alerta_pago, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PagoAlerta p = items.get(pos);
            h.tvNombre.setText(p.nombre);
            h.tvFecha.setText(p.fecha);
            h.tvMonto.setText("$" + p.monto);


            h.ivIcon.setBackground(null);
            h.ivIcon.setImageResource(p.iconRes);


            switch (p.estado) {
                case VENCIDO:
                    h.tvBadge.setText(getString(R.string.status_overdue));
                    h.tvBadge.setTextColor(Color.parseColor("#DC2626"));
                    h.tvBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                    h.tvMonto.setTextColor(Color.parseColor("#DC2626"));
                    break;
                case PENDIENTE:
                    h.tvBadge.setText(getString(R.string.status_pending));
                    h.tvBadge.setTextColor(Color.parseColor("#EA580C"));
                    h.tvBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF7ED")));
                    h.tvMonto.setTextColor(Color.parseColor("#EA580C"));
                    break;
                case PAGADO:
                default:
                    h.tvBadge.setText(getString(R.string.status_paid));
                    h.tvBadge.setTextColor(Color.parseColor("#16A34A"));
                    h.tvBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                    h.tvMonto.setTextColor(Color.parseColor("#16A34A"));
                    break;
            }


            if (!PAGADO.equals(p.estado) && p.suscripcionOriginal != null) {
                h.itemView.setOnClickListener(v ->
                        Snackbar.make(requireView(),
                                getString(R.string.action_mark_paid_confirm, p.nombre),
                                Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.action_mark_paid), x -> {
                                    viewModel.marcarComoPagado(p.suscripcionOriginal);
                                    Snackbar.make(requireView(),
                                            getString(R.string.toast_marked_as_paid, p.nombre),
                                            Snackbar.LENGTH_SHORT).show();
                                }).show());
            } else {
                h.itemView.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            android.widget.ImageView ivIcon;
            TextView tvNombre, tvFecha, tvMonto, tvBadge;

            VH(@NonNull View v) {
                super(v);
                ivIcon   = v.findViewById(R.id.ivAlertaIcon);
                tvNombre = v.findViewById(R.id.tvAlertaNombre);
                tvFecha  = v.findViewById(R.id.tvAlertaFecha);
                tvMonto  = v.findViewById(R.id.tvAlertaMonto);
                tvBadge  = v.findViewById(R.id.tvAlertaBadge);
            }
        }
    }
}
