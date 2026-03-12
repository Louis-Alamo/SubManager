package com.example.submanager.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.submanager.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class DetalleSuscripcionActivity extends AppCompatActivity {

    private ImageView ivDetalleIcon;
    private TextView tvDetalleName, tvDetalleCategoria, tvDetalleMonto;
    private TextView tvDetalleCiclo, tvDetalleEstado;
    private TextView tvProximoCobro, tvDiasParaVencer;
    private TextView tvCicloInfo, tvMetodoPago, tvRecordatorio, tvCancelDate;
    private RecyclerView rvUltimosPagos;
    private MaterialButton btnEliminar, btnMarcarPagado;
    private ImageView btnBack, btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_suscripcion);

        bindViews();
        loadData();
        setupListeners();
        setupHistorial();
    }

    private void bindViews() {
        btnBack        = findViewById(R.id.btnBack);
        btnEdit        = findViewById(R.id.btnEdit);
        ivDetalleIcon  = findViewById(R.id.ivDetalleIcon);
        tvDetalleName  = findViewById(R.id.tvDetalleName);
        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria);
        tvDetalleMonto = findViewById(R.id.tvDetalleMonto);
        tvDetalleCiclo = findViewById(R.id.tvDetalleCiclo);
        tvDetalleEstado = findViewById(R.id.tvDetalleEstado);
        tvProximoCobro = findViewById(R.id.tvProximoCobro);
        tvDiasParaVencer = findViewById(R.id.tvDiasParaVencer);
        tvCicloInfo    = findViewById(R.id.tvCicloInfo);
        tvMetodoPago   = findViewById(R.id.tvMetodoPago);
        tvRecordatorio = findViewById(R.id.tvRecordatorio);
        tvCancelDate   = findViewById(R.id.tvCancelDate);
        rvUltimosPagos = findViewById(R.id.rvUltimosPagos);
        btnEliminar    = findViewById(R.id.btnEliminar);
        btnMarcarPagado = findViewById(R.id.btnMarcarPagado);
    }

    private void loadData() {
        // Receive data from Intent extras (name, monto, categoria, ciclo, etc.)
        String nombre    = getIntent().getStringExtra("nombre");
        String monto     = getIntent().getStringExtra("monto");
        String categoria = getIntent().getStringExtra("categoria");
        String ciclo     = getIntent().getStringExtra("ciclo");
        int    iconRes   = getIntent().getIntExtra("iconRes", R.drawable.ic_app_netflix);

        // Apply data to views (use mock defaults if extras are null)
        tvDetalleName.setText(nombre != null ? nombre : "Netflix Premium");
        tvDetalleCategoria.setText(categoria != null ? categoria : "Entretenimiento");
        tvDetalleMonto.setText(monto != null ? "$" + monto : "$219.00");
        tvDetalleCiclo.setText(ciclo != null ? "por " + ciclo.toLowerCase() : "por mes");
        ivDetalleIcon.setImageResource(iconRes);

        // Mock detail data
        tvProximoCobro.setText("14 Abr 2026");
        tvDiasParaVencer.setText("En 3 días");
        tvCicloInfo.setText(ciclo != null ? ciclo : "Mensual");
        tvMetodoPago.setText("Tarjeta de crédito");
        tvRecordatorio.setText("3 días antes");
        tvCancelDate.setText("11 Abr 2026 · 3 días");
    }

    private void setupHistorial() {
        // Simple mock list of last payments as TextViews inside a card via adapter
        List<String[]> pagos = new ArrayList<>();
        pagos.add(new String[]{"11 Mar 2026", "$219.00", "Pagado"});
        pagos.add(new String[]{"11 Feb 2026", "$219.00", "Pagado"});
        pagos.add(new String[]{"11 Ene 2026", "$219.00", "Pagado"});

        rvUltimosPagos.setLayoutManager(new LinearLayoutManager(this));
        rvUltimosPagos.setAdapter(new HistorialMockAdapter(pagos));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v ->
            Snackbar.make(btnEdit, "Función de edición próximamente", Snackbar.LENGTH_SHORT).show()
        );

        btnMarcarPagado.setOnClickListener(v ->
            Snackbar.make(btnMarcarPagado, "✅ Pago registrado correctamente", Snackbar.LENGTH_SHORT).show()
        );

        btnEliminar.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar suscripción")
            .setMessage("¿Estás seguro de que deseas eliminar esta suscripción? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                Snackbar.make(btnEliminar, "Suscripción eliminada", Snackbar.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    // ─── Inner Adapter for mock payment history ────────────────────────────
    static class HistorialMockAdapter extends RecyclerView.Adapter<HistorialMockAdapter.VH> {
        private final List<String[]> items;
        HistorialMockAdapter(List<String[]> items) { this.items = items; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_historial_pago, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            String[] item = items.get(pos);
            if (h.tvNombre  != null) h.tvNombre.setText("Netflix Premium");
            if (h.tvFecha   != null) h.tvFecha.setText(item[0]);
            if (h.tvMonto   != null) h.tvMonto.setText(item[1]);
            if (h.tvEstado  != null) h.tvEstado.setText(item[2]);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvNombre, tvFecha, tvMonto, tvEstado;
            VH(View v) {
                super(v);
                tvNombre = v.findViewById(R.id.tvNombre);
                tvFecha  = v.findViewById(R.id.tvFechaCategoria);
                tvMonto  = v.findViewById(R.id.tvMonto);
                tvEstado = v.findViewById(R.id.tvBadgePagado);
            }
        }
    }
}
