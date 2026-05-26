package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.Calendar;

public class CompraExitosaActivity extends AppCompatActivity {

    private TextView tvCompraPlan, tvCompraMonto, tvCompraRenovacion, tvCompraPago;
    private MaterialButton btnComenzar;
    private TextView tvVerFactura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra_exitosa);

        bindViews();
        loadPurchaseData();
        setupListeners();
    }

    private void bindViews() {
        tvCompraPlan        = findViewById(R.id.tvCompraplan);
        tvCompraMonto       = findViewById(R.id.tvCompraMonto);
        tvCompraRenovacion  = findViewById(R.id.tvCompraRenovacion);
        tvCompraPago        = findViewById(R.id.tvCompraPago);
        btnComenzar         = findViewById(R.id.btnComenzar);
        tvVerFactura        = findViewById(R.id.tvVerFactura);
    }

    private void loadPurchaseData() {
        String plan   = getIntent().getStringExtra("plan");
        String monto  = getIntent().getStringExtra("monto");
        String expiry = getIntent().getStringExtra("expiry");

        if (plan  == null) plan  = "Premium Anual";
        if (monto == null) monto = "500.00";

        tvCompraPlan.setText(plan);
        tvCompraMonto.setText("$" + monto + " MXN");
        tvCompraPago.setText("Tarjeta de crédito");


        if (expiry == null) {
            Calendar cal = Calendar.getInstance();
            if (plan.contains("Anual")) cal.add(Calendar.YEAR, 1);
            else cal.add(Calendar.MONTH, 1);
            String[] months = {"Ene","Feb","Mar","Abr","May","Jun",
                               "Jul","Ago","Sep","Oct","Nov","Dic"};
            expiry = cal.get(Calendar.DAY_OF_MONTH)
                    + " " + months[cal.get(Calendar.MONTH)]
                    + " " + cal.get(Calendar.YEAR);
        }
        tvCompraRenovacion.setText(expiry);


        new SessionManager(this).savePremium(plan, expiry);
    }

    private void setupListeners() {
        btnComenzar.setOnClickListener(v -> {

            android.content.Intent intent = new android.content.Intent(this,
                    com.example.submanager.MainActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        tvVerFactura.setOnClickListener(v ->
            Snackbar.make(tvVerFactura, "Comprobante enviado a tu correo", Snackbar.LENGTH_SHORT).show()
        );
    }
}
