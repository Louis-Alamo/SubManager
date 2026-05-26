package com.example.submanager.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submanager.R;
import com.example.submanager.data.remote.SupabaseClient;
import com.example.submanager.data.remote.dto.UsuarioDto;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class PremiumActivity extends AppCompatActivity {

    private static final String TAG = "PremiumActivity";

    private ImageView btnBack;
    private MaterialCardView cardPlanMensual, cardPlanAnual;
    private RadioButton radioPlanMensual, radioPlanAnual;
    private MaterialButton btnSuscribirse;
    private TextView tvRestaurar;
    private SessionManager sessionManager;

    private boolean planAnualSelected = true;

    private static final int COLOR_SELECTED_STROKE   = 0xFF2563EB;
    private static final int COLOR_UNSELECTED_STROKE = 0xFFE5E7EB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        sessionManager = new SessionManager(this);

        bindViews();
        updatePlanUI();
        setupListeners();

        if (sessionManager.isPremium()) {
            showAlreadyPremiumState();
        }
    }

    private void bindViews() {
        btnBack         = findViewById(R.id.btnBack);
        cardPlanMensual = findViewById(R.id.cardPlanMensual);
        cardPlanAnual   = findViewById(R.id.cardPlanAnual);
        radioPlanMensual = findViewById(R.id.radioPlanMensual);
        radioPlanAnual  = findViewById(R.id.radioPlanAnual);
        btnSuscribirse  = findViewById(R.id.btnSuscribirse);
        tvRestaurar     = findViewById(R.id.tvRestaurar);
    }

    private void updatePlanUI() {
        if (planAnualSelected) {
            radioPlanAnual.setChecked(true);
            radioPlanMensual.setChecked(false);
            cardPlanAnual.setStrokeColor(COLOR_SELECTED_STROKE);
            cardPlanMensual.setStrokeColor(COLOR_UNSELECTED_STROKE);
        } else {
            radioPlanAnual.setChecked(false);
            radioPlanMensual.setChecked(true);
            cardPlanAnual.setStrokeColor(COLOR_UNSELECTED_STROKE);
            cardPlanMensual.setStrokeColor(COLOR_SELECTED_STROKE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardPlanMensual.setOnClickListener(v -> { planAnualSelected = false; updatePlanUI(); });
        radioPlanMensual.setOnClickListener(v -> { planAnualSelected = false; updatePlanUI(); });
        cardPlanAnual.setOnClickListener(v   -> { planAnualSelected = true;  updatePlanUI(); });
        radioPlanAnual.setOnClickListener(v  -> { planAnualSelected = true;  updatePlanUI(); });

        btnSuscribirse.setOnClickListener(v -> activarPremium());

        tvRestaurar.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                Snackbar.make(tvRestaurar,
                        "Premium activo hasta " + sessionManager.getPremiumExpiry(),
                        Snackbar.LENGTH_LONG).show();
            } else {

                recuperarPremiumDesdeNube(v);
            }
        });
    }





    private void activarPremium() {
        String tipoPlan = planAnualSelected ? "ANUAL"   : "MENSUAL";
        String planLabel = planAnualSelected ? "Premium Anual" : "Premium Mensual";
        String monto    = planAnualSelected ? "500.00" : "50.00";


        Calendar cal = Calendar.getInstance();
        if (planAnualSelected) cal.add(Calendar.YEAR, 1);
        else cal.add(Calendar.MONTH, 1);

        String[] months = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        String expiryLabel = cal.get(Calendar.DAY_OF_MONTH)
                + " " + months[cal.get(Calendar.MONTH)]
                + " " + cal.get(Calendar.YEAR);

        String fechaInicio    = LocalDate.now().toString();
        String fechaRenovacion = planAnualSelected
                ? LocalDate.now().plusYears(1).toString()
                : LocalDate.now().plusMonths(1).toString();


        sessionManager.savePremium(planLabel, expiryLabel);


        String email = sessionManager.getEmail();
        if (!email.isEmpty()) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    UsuarioDto update = new UsuarioDto();
                    update.tipoPlan        = tipoPlan;
                    update.fechaInicioPlan = fechaInicio;
                    update.fechaRenovacion = fechaRenovacion;
                    update.estaActivo      = true;

                    Response<Void> resp = SupabaseClient.getApi()
                            .updateUsuario("eq." + email, update)
                            .execute();

                    if (resp.isSuccessful()) {
                        Log.i(TAG, "Plan Premium actualizado en Supabase para: " + email);
                    } else {
                        Log.w(TAG, "No se pudo actualizar premium en Supabase: " + resp.code());
                    }
                } catch (Exception e) {

                    Log.w(TAG, "Sin conexión al actualizar premium remoto: " + e.getMessage());
                }
            });
        }


        Intent intent = new Intent(this, CompraExitosaActivity.class);
        intent.putExtra("plan", planLabel);
        intent.putExtra("monto", monto);
        intent.putExtra("expiry", expiryLabel);
        startActivity(intent);
    }





    private void recuperarPremiumDesdeNube(View anchor) {
        String email = sessionManager.getEmail();
        if (email.isEmpty()) {
            Snackbar.make(anchor, "Inicia sesión para restaurar tu compra", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(anchor, "Buscando tu compra en la nube…", Snackbar.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response<java.util.List<UsuarioDto>> resp = SupabaseClient.getApi()
                        .getUsuarioPorCorreo("eq." + email)
                        .execute();

                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    UsuarioDto usuario = resp.body().get(0);

                    if (usuario.estaActivo != null && usuario.estaActivo
                            && usuario.tipoPlan != null) {

                        sessionManager.savePremium(usuario.tipoPlan, usuario.fechaRenovacion != null ? usuario.fechaRenovacion : "");
                        sessionManager.saveRemoteUserId(usuario.id);

                        runOnUiThread(() -> {
                            showAlreadyPremiumState();
                            Snackbar.make(anchor,
                                    "Premium restaurado. Plan: " + usuario.tipoPlan + " hasta " + usuario.fechaRenovacion,
                                    Snackbar.LENGTH_LONG).show();
                        });
                    } else {
                        runOnUiThread(() ->
                            Snackbar.make(anchor, "No se encontraron compras activas", Snackbar.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                        Snackbar.make(anchor, "No se encontraron compras anteriores", Snackbar.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al recuperar premium", e);
                runOnUiThread(() ->
                    Snackbar.make(anchor, "Sin conexión. Intenta más tarde.", Snackbar.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showAlreadyPremiumState() {
        btnSuscribirse.setText("Ya eres Premium");
        btnSuscribirse.setEnabled(false);
        btnSuscribirse.setAlpha(0.6f);
        cardPlanMensual.setVisibility(View.GONE);
        cardPlanAnual.setVisibility(View.GONE);
    }
}
