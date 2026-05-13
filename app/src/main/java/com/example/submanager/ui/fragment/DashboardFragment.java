package com.example.submanager.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.activity.NuevaSuscripcionActivity;
import com.example.submanager.ui.activity.NuevoServicioActivity;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.example.submanager.utils.SessionManager;

public class DashboardFragment extends Fragment {

    private TextView tvWelcome, tvGreeting, tvMontoPendiente, tvMontoPagado, tvMontoTotal;
    private RecyclerView rvProximos;
    private LinearLayout sectionProximos;
    private View emptyUpcoming;
    private UpcomingAdapter adapter;
    private SuscripcionViewModel viewModel;
    private boolean summaryLoaded = false;
    private boolean upcomingLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmerDashboard);
        View scroll = view.findViewById(R.id.scrollDashboard);

        tvWelcome         = view.findViewById(R.id.tvWelcome);
        tvGreeting        = view.findViewById(R.id.tvGreeting);
        tvMontoPendiente  = view.findViewById(R.id.tvMontoPendiente);
        tvMontoPagado     = view.findViewById(R.id.tvMontoPagado);
        tvMontoTotal      = view.findViewById(R.id.tvMontoTotal);
        rvProximos        = view.findViewById(R.id.rvProximos);
        sectionProximos   = view.findViewById(R.id.sectionProximos);
        emptyUpcoming     = view.findViewById(R.id.emptyUpcoming);

        setupWelcomeUser();
        setupGreeting();

        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        // Resumen general desde la tabla completa
        viewModel.getTodasLasSuscripciones().observe(getViewLifecycleOwner(), suscripciones -> {
            if (suscripciones != null) {
                suscripcionesCache = suscripciones;
                setupSummaryCards();
                summaryLoaded = true;
                maybeShowContent(shimmer, scroll);
            }
        });

        viewModel.getAllRegistrosPagoLiveData().observe(getViewLifecycleOwner(), pagos -> {
            if (pagos != null) {
                pagosCache = pagos;
                setupSummaryCards();
            }
        });

        // Proximos vencimientos directamente desde Room
        viewModel.getSuscripcionesProximas().observe(getViewLifecycleOwner(), suscripcionesProximas -> {
            if (suscripcionesProximas != null) {
                setupUpcomingList(suscripcionesProximas);
                upcomingLoaded = true;
                maybeShowContent(shimmer, scroll);
            }
        });

        setupNavigation(view);
    }

    private void maybeShowContent(ShimmerFrameLayout shimmer, View scroll) {
        if (summaryLoaded && upcomingLoaded) {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
            scroll.setVisibility(View.VISIBLE);
        }
    }

    private void setupGreeting() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saludo;
        if (hora >= 6 && hora < 12)       saludo = "Buenos días ☀️";
        else if (hora >= 12 && hora < 20) saludo = "Buenas tardes 👋";
        else                              saludo = "Buenas noches 🌙";
        tvGreeting.setText(saludo);
    }

    private void setupWelcomeUser() {
        if (tvWelcome == null || getContext() == null) return;

        SessionManager sessionManager = new SessionManager(requireContext());
        String nombre = sessionManager.getNombre();

        if (nombre != null) {
            nombre = nombre.trim();
        }

        if (nombre != null && !nombre.isEmpty()) {
            tvWelcome.setText("Bienvenido, " + nombre);
        } else {
            tvWelcome.setText("Bienvenido");
        }
    }

    private List<SuscripcionModel> suscripcionesCache = new ArrayList<>();
    private List<com.example.submanager.data.model.RegistrosPagoModel> pagosCache = new ArrayList<>();

    private void setupSummaryCards() {
        if (suscripcionesCache == null || pagosCache == null) return;

        double pendiente = 0, pagado = 0, total = 0;

        Calendar calActual = Calendar.getInstance();
        int mesActual = calActual.get(Calendar.MONTH);
        int anioActual = calActual.get(Calendar.YEAR);

        for (SuscripcionModel s : suscripcionesCache) {
            if (!s.isEstaActiva()) continue;
            total += s.getMonto();
        }

        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (com.example.submanager.data.model.RegistrosPagoModel p : pagosCache) {
            if ("Pagado".equalsIgnoreCase(p.getEstado()) && p.getFechaPago() != null) {
                try {
                    Date d = sdfDB.parse(p.getFechaPago());
                    if (d != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(d);
                        if (c.get(Calendar.MONTH) == mesActual && c.get(Calendar.YEAR) == anioActual) {
                            pagado += p.getMonto();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        pendiente = Math.max(0, total - pagado);

        tvMontoPendiente.setText(String.format(Locale.getDefault(), "$%.0f", pendiente));
        tvMontoPagado.setText(String.format(Locale.getDefault(), "$%.0f", pagado));
        tvMontoTotal.setText(String.format(Locale.getDefault(), "$%.0f", total));
    }

    private void setupUpcomingList(List<SuscripcionModel> proximosDesdeBd) {
        List<SuscripcionModel> proximos = new ArrayList<>();
        for (SuscripcionModel s : proximosDesdeBd) {
            if (!s.isEstaActiva()) continue;

            long dias = getDiasRestantes(s.getFechaProximoCobro());
            // Mostrar solo si vence en los próximos 7 días
            if (dias >= 0 && dias <= 7) {
                proximos.add(s);
            }
        }

        proximos.sort((a, b) -> {
            long diasA = getDiasRestantes(a.getFechaProximoCobro());
            long diasB = getDiasRestantes(b.getFechaProximoCobro());
            return Long.compare(diasA, diasB);
        });

        if (proximos.isEmpty()) {
            if (sectionProximos != null) sectionProximos.setVisibility(View.VISIBLE);
            if (rvProximos != null) rvProximos.setVisibility(View.GONE);
            if (emptyUpcoming != null) emptyUpcoming.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.setItems(new ArrayList<>());
            return;
        }

        if (sectionProximos != null) sectionProximos.setVisibility(View.VISIBLE);
        if (rvProximos != null) rvProximos.setVisibility(View.VISIBLE);
        if (emptyUpcoming != null) emptyUpcoming.setVisibility(View.GONE);

        if (adapter == null) {
            adapter = new UpcomingAdapter(proximos);
            rvProximos.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvProximos.setNestedScrollingEnabled(false);
            rvProximos.setAdapter(adapter);
        } else {
            adapter.setItems(proximos);
        }
    }

    private long getDiasRestantes(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return 999;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fechaCobro = sdf.parse(fechaStr);
            if (fechaCobro == null) return 999;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar target = Calendar.getInstance();
            target.setTime(fechaCobro);
            target.set(Calendar.HOUR_OF_DAY, 0);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            long diff = target.getTimeInMillis() - today.getTimeInMillis();
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return 999;
        }
    }

    private void setupNavigation(View root) {
        View ivSettings = root.findViewById(R.id.ivSettings);
        if (ivSettings != null) ivSettings.setOnClickListener(v -> navigateTo(R.id.nav_perfil));

        View cardSub  = root.findViewById(R.id.cardAgregarSuscripcion);
        View cardServ = root.findViewById(R.id.cardAgregarServicio);
        if (cardSub != null) cardSub.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NuevaSuscripcionActivity.class)));
        if (cardServ != null) cardServ.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NuevoServicioActivity.class)));

        // Chips de acceso rapido nuevos
        View chipSubs   = root.findViewById(R.id.cardShortcutSubs);
        View chipAlertas = root.findViewById(R.id.cardShortcutAlertas);
        if (chipSubs != null)    chipSubs.setOnClickListener(v -> navigateTo(R.id.nav_suscripciones));
        if (chipAlertas != null) chipAlertas.setOnClickListener(v -> navigateTo(R.id.nav_alertas));

        View tvSeeAll = root.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) tvSeeAll.setOnClickListener(v -> navigateTo(R.id.nav_alertas));

        // Rellenar label del mes en la Hero Card
        TextView tvMes = root.findViewById(R.id.tvMesActual);
        if (tvMes != null) {
            java.text.SimpleDateFormat sdfMes = new java.text.SimpleDateFormat("MMMM yyyy", new java.util.Locale("es", "MX"));
            String mesLabel = sdfMes.format(new java.util.Date());
            String mesCapital = mesLabel.substring(0, 1).toUpperCase() + mesLabel.substring(1);
            tvMes.setText(mesCapital);
        }
    }

    private void navigateTo(int navItemId) {
        if (getActivity() == null) return;
        BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setSelectedItemId(navItemId);
    }

    private class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.VH> {

        private List<SuscripcionModel> items;

        UpcomingAdapter(List<SuscripcionModel> items) { this.items = items; }

        void setItems(List<SuscripcionModel> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_upcoming, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            SuscripcionModel item = items.get(pos);

            h.tvNombre.setText(item.getNombre());
            String catLabel = item.getCategoria() + " · " + item.getCicloFacturacion();
            h.tvSub.setText(catLabel);
            h.tvMonto.setText(String.format(Locale.getDefault(), "-$%.0f", item.getMonto()));

            int iconResId = getResources().getIdentifier(item.getNombreIcono(), "drawable", requireContext().getPackageName());
            if (iconResId != 0) {
                h.ivIcon.setBackground(null);
                h.ivIcon.setImageResource(iconResId);
                h.ivIcon.setPadding(0, 0, 0, 0);
            } else {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                try { circle.setColor(Color.parseColor(item.getColor())); }
                catch (Exception e) { circle.setColor(Color.LTGRAY); }
                h.ivIcon.setBackground(circle);
                h.ivIcon.setImageDrawable(null);
            }

            long dias = getDiasRestantes(item.getFechaProximoCobro());
            String badge = dias == 0 ? "Hoy" : (dias == 1 ? "1 día" : dias + " días");
            h.tvDias.setText(badge);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvNombre, tvSub, tvMonto, tvDias;
            VH(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivUpcomingIcon);
                tvNombre = itemView.findViewById(R.id.tvNombreServicio);
                tvSub = itemView.findViewById(R.id.tvDiasRestantes);
                tvMonto = itemView.findViewById(R.id.tvMontoUpcoming);
                tvDias = itemView.findViewById(R.id.tvBadgeDias);
            }
        }
    }
}
