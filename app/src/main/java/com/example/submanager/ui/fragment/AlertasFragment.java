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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AlertasFragment extends Fragment {

    // ─── Estado de pago ──────────────────────────────────────────────────────
    static final String PENDIENTE = "pendiente";
    static final String PAGADO    = "pagado";
    static final String VENCIDO   = "vencido";

    // ─── Mock model ──────────────────────────────────────────────────────────
    static class PagoAlerta {
        String nombre, fecha, monto, estado, colorHex;
        int iconRes;

        PagoAlerta(String nombre, String fecha, String monto,
                   String estado, String colorHex, int iconRes) {
            this.nombre   = nombre;
            this.fecha    = fecha;
            this.monto    = monto;
            this.estado   = estado;
            this.colorHex = colorHex;
            this.iconRes  = iconRes;
        }
    }

    // ─── Vistas ──────────────────────────────────────────────────────────────
    private RecyclerView rvPendiente, rvVencido, rvPagado;
    private View sectionPendiente, sectionVencido, sectionPagado, emptyState;
    private TextView tvTotalPendiente, tvTotalPagado, tvTotalVencido;

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

        // Bind vistas
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

        setupSecciones();
    }

    // ─── Construye las tres secciones ────────────────────────────────────────
    private void setupSecciones() {
        List<PagoAlerta> todos = getMockData();

        List<PagoAlerta> pendientes = filtrar(todos, PENDIENTE);
        List<PagoAlerta> pagados    = filtrar(todos, PAGADO);
        List<PagoAlerta> vencidos   = filtrar(todos, VENCIDO);

        // Totales en cards
        tvTotalPendiente.setText(sumar(pendientes));
        tvTotalPagado.setText(sumar(pagados));
        tvTotalVencido.setText(sumar(vencidos));

        // Sección VENCIDO
        montarLista(rvVencido, vencidos);
        sectionVencido.setVisibility(vencidos.isEmpty() ? View.GONE : View.VISIBLE);

        // Sección PENDIENTE
        montarLista(rvPendiente, pendientes);
        sectionPendiente.setVisibility(pendientes.isEmpty() ? View.GONE : View.VISIBLE);

        // Sección PAGADO
        montarLista(rvPagado, pagados);
        sectionPagado.setVisibility(pagados.isEmpty() ? View.GONE : View.VISIBLE);

        // Empty state si todo está vacío
        boolean hayAlgo = !pendientes.isEmpty() || !pagados.isEmpty() || !vencidos.isEmpty();
        emptyState.setVisibility(hayAlgo ? View.GONE : View.VISIBLE);
    }

    private void montarLista(RecyclerView rv, List<PagoAlerta> items) {
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new AlertaAdapter(items));
        rv.setNestedScrollingEnabled(false);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
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
        return String.format("$%.2f", total);
    }

    // ─── Mock data ───────────────────────────────────────────────────────────
    private List<PagoAlerta> getMockData() {
        List<PagoAlerta> lista = new ArrayList<>();

        // 🔴 Vencidos (2)
        lista.add(new PagoAlerta("Netflix",         "Venció 1 Mar",  "199.00", VENCIDO,   "#EF4444", R.drawable.ic_app_netflix));
        lista.add(new PagoAlerta("Luz Eléctrica",   "Venció 4 Mar",  "640.00", VENCIDO,   "#F59E0B", R.drawable.ic_service_electricity));

        // ⏳ Pendientes (5)
        lista.add(new PagoAlerta("Spotify",         "Vence 12 Mar",  "129.00", PENDIENTE, "#22C55E", R.drawable.ic_app_spotify));
        lista.add(new PagoAlerta("Internet",        "Vence 14 Mar",  "450.00", PENDIENTE, "#3B82F6", R.drawable.ic_service_internet));
        lista.add(new PagoAlerta("Disney+",         "Vence 16 Mar",  "159.00", PENDIENTE, "#0064FF", R.drawable.ic_app_disneyplus));
        lista.add(new PagoAlerta("Xbox Game Pass",  "Vence 18 Mar",  "249.00", PENDIENTE, "#107C10", R.drawable.ic_app_xbox));
        lista.add(new PagoAlerta("Gas",             "Vence 20 Mar",  "320.00", PENDIENTE, "#F97316", R.drawable.ic_service_gas));

        // ✅ Pagados (4)
        lista.add(new PagoAlerta("YouTube Premium", "Pagado 2 Mar",  "139.00", PAGADO,    "#FF0000", R.drawable.ic_app_youtube));
        lista.add(new PagoAlerta("Google One",      "Pagado 3 Mar",  "35.00",  PAGADO,    "#4285F4", R.drawable.ic_app_google));
        lista.add(new PagoAlerta("Agua",            "Pagado 5 Mar",  "180.00", PAGADO,    "#0EA5E9", R.drawable.ic_service_water));
        lista.add(new PagoAlerta("Mercado Libre +", "Pagado 8 Mar",  "99.00",  PAGADO,    "#FFE600", R.drawable.ic_app_mercadolibre));

        return lista;
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────
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

            // Ícono (todos los items tienen iconRes)
            h.ivIcon.setBackground(null);
            h.ivIcon.setImageResource(p.iconRes);

            // Badge por estado
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

            // Tap → snackbar de "Marcar pagado" para pendientes/vencidos
            if (!PAGADO.equals(p.estado)) {
                h.itemView.setOnClickListener(v ->
                        Snackbar.make(requireView(),
                                getString(R.string.action_mark_paid_confirm, p.nombre),
                                Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.action_mark_paid), x ->
                                        Snackbar.make(requireView(),
                                                getString(R.string.toast_marked_as_paid, p.nombre),
                                                Snackbar.LENGTH_SHORT).show())
                                .show());
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
