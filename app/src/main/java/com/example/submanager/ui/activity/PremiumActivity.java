package com.example.submanager.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class PremiumActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialCardView cardPlanMensual, cardPlanAnual;
    private RadioButton radioPlanMensual, radioPlanAnual;
    private MaterialButton btnSuscribirse;
    private TextView tvRestaurar;

    private boolean planAnualSelected = true;

    // Border colors (as int)
    private static final int COLOR_SELECTED_STROKE   = 0xFF2563EB; // primary
    private static final int COLOR_UNSELECTED_STROKE  = 0xFFE5E7EB; // border

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        bindViews();
        updatePlanUI();
        setupListeners();
    }

    private void bindViews() {
        btnBack          = findViewById(R.id.btnBack);
        cardPlanMensual  = findViewById(R.id.cardPlanMensual);
        cardPlanAnual    = findViewById(R.id.cardPlanAnual);
        radioPlanMensual = findViewById(R.id.radioPlanMensual);
        radioPlanAnual   = findViewById(R.id.radioPlanAnual);
        btnSuscribirse   = findViewById(R.id.btnSuscribirse);
        tvRestaurar      = findViewById(R.id.tvRestaurar);
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

        cardPlanMensual.setOnClickListener(v -> {
            planAnualSelected = false;
            updatePlanUI();
        });
        radioPlanMensual.setOnClickListener(v -> {
            planAnualSelected = false;
            updatePlanUI();
        });

        cardPlanAnual.setOnClickListener(v -> {
            planAnualSelected = true;
            updatePlanUI();
        });
        radioPlanAnual.setOnClickListener(v -> {
            planAnualSelected = true;
            updatePlanUI();
        });

        btnSuscribirse.setOnClickListener(v -> {
            String plan  = planAnualSelected ? "Premium Anual"   : "Premium Mensual";
            String monto = planAnualSelected ? "500.00" : "50.00";
            Intent intent = new Intent(this, CompraExitosaActivity.class);
            intent.putExtra("plan", plan);
            intent.putExtra("monto", monto);
            startActivity(intent);
        });

        tvRestaurar.setOnClickListener(v ->
            Snackbar.make(tvRestaurar, "Buscando compras anteriores...", Snackbar.LENGTH_SHORT).show()
        );
    }
}
