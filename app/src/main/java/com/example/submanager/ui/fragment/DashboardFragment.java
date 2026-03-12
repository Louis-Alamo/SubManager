package com.example.submanager.ui.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    // ══════════════════════════════════════════════════════════════
    //  Modelo mock
    // ══════════════════════════════════════════════════════════════
    static class SuscripcionMock {
        String nombre, colorHex, categoria;
        double monto;
        int iconRes;
        /** días para vencer: ≥0 = pendiente, <0 = ya pagado este mes */
        int diasParaVencer;

        SuscripcionMock(String nombre, double monto, String colorHex,
                        int iconRes, int diasParaVencer, String categoria) {
            this.nombre        = nombre;
            this.monto         = monto;
            this.colorHex      = colorHex;
            this.iconRes       = iconRes;
            this.diasParaVencer = diasParaVencer;
            this.categoria     = categoria;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Datos mock
    // ══════════════════════════════════════════════════════════════
    private List<SuscripcionMock> buildMockData() {
        List<SuscripcionMock> l = new ArrayList<>();
        // --- Pendientes (diasParaVencer >= 0) ---
        l.add(new SuscripcionMock("YouTube Premium",    89.00,  "#FF0000", R.drawable.ic_app_youtube,      0,  "digital"));
        l.add(new SuscripcionMock("Netflix Premium",    219.00, "#E50914", R.drawable.ic_app_netflix,      1,  "digital"));
        l.add(new SuscripcionMock("Luz Eléctrica",      850.00, "#F97316", R.drawable.ic_service_electricity, 2, "hogar"));
        l.add(new SuscripcionMock("Spotify",            99.00,  "#1DB954", R.drawable.ic_app_spotify,      3,  "digital"));
        l.add(new SuscripcionMock("Disney+",            159.00, "#113CCF", R.drawable.ic_app_disneyplus,   5,  "digital"));
        l.add(new SuscripcionMock("Xbox Game Pass",     259.00, "#107C10", R.drawable.ic_app_xbox,         7,  "digital"));
        l.add(new SuscripcionMock("Spotify Familiar",   149.00, "#1DB954", R.drawable.ic_app_spotify,      15, "digital"));
        l.add(new SuscripcionMock("Agua",               180.00, "#0EA5E9", R.drawable.ic_service_water,    20, "hogar"));
        // --- Pagados (diasParaVencer < 0) ---
        l.add(new SuscripcionMock("HBO Max",            189.00, "#7C3AED", R.drawable.ic_app_hbomax,      -5,  "digital"));
        l.add(new SuscripcionMock("Amazon Prime Video", 129.00, "#00A8E0", R.drawable.ic_app_prime_video, -8,  "digital"));
        l.add(new SuscripcionMock("Google One 100GB",   79.00,  "#4285F4", R.drawable.ic_app_google,      -12, "digital"));
        l.add(new SuscripcionMock("Internet Hogar",     599.00, "#2563EB", R.drawable.ic_service_internet,-3,  "hogar"));
        l.add(new SuscripcionMock("Mercado Libre+",     99.00,  "#FFE600", R.drawable.ic_app_mercadolibre,-15, "digital"));
        l.add(new SuscripcionMock("Apple Music",        79.00,  "#FC3C44", R.drawable.ic_app_apple_music, -20, "digital"));
        return l;
    }

    // ══════════════════════════════════════════════════════════════
    //  Vistas
    // ══════════════════════════════════════════════════════════════
    private TextView tvGreeting, tvMontoPendiente, tvMontoPagado, tvMontoTotal;
    private RecyclerView rvProximos;
    private LinearLayout sectionProximos;
    private View emptyUpcoming;
    private UpcomingAdapter adapter;

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

        // ── Shimmer → contenido real ──────────────────────────────
        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmerDashboard);
        View scroll = view.findViewById(R.id.scrollDashboard);

        view.postDelayed(() -> {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
            scroll.setVisibility(View.VISIBLE);
        }, 1200);

        // ── Bind vistas ───────────────────────────────────────────
        tvGreeting        = view.findViewById(R.id.tvGreeting);
        tvMontoPendiente  = view.findViewById(R.id.tvMontoPendiente);
        tvMontoPagado     = view.findViewById(R.id.tvMontoPagado);
        tvMontoTotal      = view.findViewById(R.id.tvMontoTotal);
        rvProximos        = view.findViewById(R.id.rvProximos);
        sectionProximos   = view.findViewById(R.id.sectionProximos);
        emptyUpcoming     = view.findViewById(R.id.emptyUpcoming);

        // ── Datos ─────────────────────────────────────────────────
        List<SuscripcionMock> todos = buildMockData();

        // ── Saludo por hora ───────────────────────────────────────
        setupGreeting();

        // ── Tarjetas resumen ──────────────────────────────────────
        setupSummaryCards(todos);

        // ── Próximos vencimientos (0..7 días) ─────────────────────
        setupUpcomingList(todos);

        // ── Navegación ────────────────────────────────────────────
        setupNavigation(view);
    }

    // ══════════════════════════════════════════════════════════════
    //  Saludo dinámico según hora del día
    // ══════════════════════════════════════════════════════════════
    private void setupGreeting() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saludo;
        if (hora >= 6 && hora < 12)       saludo = "Buenos días ☀️";
        else if (hora >= 12 && hora < 20) saludo = "Buenas tardes 👋";
        else                              saludo = "Buenas noches 🌙";
        tvGreeting.setText(saludo);
    }

    // ══════════════════════════════════════════════════════════════
    //  Totales calculados de los datos mock
    // ══════════════════════════════════════════════════════════════
    private void setupSummaryCards(List<SuscripcionMock> todos) {
        double pendiente = 0, pagado = 0, total = 0;
        for (SuscripcionMock s : todos) {
            total += s.monto;
            if (s.diasParaVencer >= 0) pendiente += s.monto;
            else                       pagado    += s.monto;
        }
        tvMontoPendiente.setText(String.format(Locale.getDefault(), "$%.0f", pendiente));
        tvMontoPagado   .setText(String.format(Locale.getDefault(), "$%.0f", pagado));
        tvMontoTotal    .setText(String.format(Locale.getDefault(), "$%.0f", total));
    }

    // ══════════════════════════════════════════════════════════════
    //  RecyclerView: próximos 7 días
    // ══════════════════════════════════════════════════════════════
    private void setupUpcomingList(List<SuscripcionMock> todos) {
        List<SuscripcionMock> proximos = new ArrayList<>();
        for (SuscripcionMock s : todos) {
            if (s.diasParaVencer >= 0 && s.diasParaVencer <= 7) {
                proximos.add(s);
            }
        }
        // Ordenar por días ascendente
        proximos.sort((a, b) -> Integer.compare(a.diasParaVencer, b.diasParaVencer));

        if (proximos.isEmpty()) {
            if (sectionProximos != null) sectionProximos.setVisibility(View.GONE);
            if (emptyUpcoming  != null) emptyUpcoming.setVisibility(View.VISIBLE);
        } else {
            if (sectionProximos != null) sectionProximos.setVisibility(View.VISIBLE);
            if (emptyUpcoming  != null) emptyUpcoming.setVisibility(View.GONE);
            adapter = new UpcomingAdapter(proximos);
            rvProximos.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvProximos.setNestedScrollingEnabled(false);
            rvProximos.setAdapter(adapter);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Navegación entre tabs
    // ══════════════════════════════════════════════════════════════
    private void setupNavigation(View root) {
        // Ajustes → Perfil
        View ivSettings = root.findViewById(R.id.ivSettings);
        if (ivSettings != null) ivSettings.setOnClickListener(v -> navigateTo(R.id.nav_perfil));

        // Agregar suscripción / servicio → Suscripciones
        View cardSub  = root.findViewById(R.id.cardAgregarSuscripcion);
        View cardServ = root.findViewById(R.id.cardAgregarServicio);
        if (cardSub  != null) cardSub .setOnClickListener(v -> navigateTo(R.id.nav_suscripciones));
        if (cardServ != null) cardServ.setOnClickListener(v -> navigateTo(R.id.nav_suscripciones));

        // Ver todos → Alertas
        View tvSeeAll = root.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) tvSeeAll.setOnClickListener(v -> navigateTo(R.id.nav_alertas));
    }

    private void navigateTo(int navItemId) {
        if (getActivity() == null) return;
        BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setSelectedItemId(navItemId);
    }

    // ══════════════════════════════════════════════════════════════
    //  Adapter interno para rvProximos
    // ══════════════════════════════════════════════════════════════
    private class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.VH> {

        private final List<SuscripcionMock> items;

        UpcomingAdapter(List<SuscripcionMock> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_upcoming, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            SuscripcionMock item = items.get(pos);

            // ── Nombre ───────────────────────────────────────────
            h.tvNombre.setText(item.nombre);

            // ── Subtítulo: categoría capitalizada ────────────────
            String catLabel = "digital".equals(item.categoria) ? "Digital · Mensual"
                                                                : "Hogar · Mensual";
            h.tvSub.setText(catLabel);

            // ── Monto ─────────────────────────────────────────────
            h.tvMonto.setText(String.format(Locale.getDefault(), "-$%.0f", item.monto));

            // ── Ícono o círculo color ─────────────────────────────
            if (item.iconRes != 0) {
                h.ivIcon.setBackground(null);
                h.ivIcon.setImageResource(item.iconRes);
                h.ivIcon.setPadding(0, 0, 0, 0);
            } else {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                try { circle.setColor(Color.parseColor(item.colorHex)); }
                catch (Exception e) { circle.setColor(Color.LTGRAY); }
                h.ivIcon.setBackground(circle);
                h.ivIcon.setImageDrawable(null);
            }

            // ── Badge de días ────────────────────────────────────
            int dias = item.diasParaVencer;
            String badgeText;
            int badgeBgColor, badgeTextColor;

            if (dias == 0) {
                badgeText      = "¡Hoy!";
                badgeBgColor   = 0xFFFEE2E2; // error_bg
                badgeTextColor = 0xFFDC2626; // error
            } else if (dias == 1) {
                badgeText      = "Mañana";
                badgeBgColor   = 0xFFFFF7ED; // orange bg
                badgeTextColor = 0xFFF97316; // warning
            } else {
                badgeText      = dias + " días";
                badgeBgColor   = 0xFFEFF6FF; // primary_tint
                badgeTextColor = 0xFF2563EB; // primary
            }

            h.tvBadge.setText(badgeText);
            h.tvBadge.setTextColor(badgeTextColor);
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(20f);
            badgeBg.setColor(badgeBgColor);
            h.tvBadge.setBackground(badgeBg);

            // ── Tap → Snackbar ────────────────────────────────────
            h.itemView.setOnClickListener(v ->
                    Snackbar.make(v, item.nombre + " – $" + (int) item.monto, Snackbar.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView  tvNombre, tvSub, tvMonto, tvBadge;

            VH(@NonNull View v) {
                super(v);
                ivIcon   = v.findViewById(R.id.ivUpcomingIcon);
                tvNombre = v.findViewById(R.id.tvNombreServicio);
                tvSub    = v.findViewById(R.id.tvDiasRestantes);
                tvMonto  = v.findViewById(R.id.tvMontoUpcoming);
                tvBadge  = v.findViewById(R.id.tvBadgeDias);
            }
        }
    }
}

