package com.example.submanager.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.ui.adapter.RegistrosPagoAdapter;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import android.content.Intent;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;

import com.example.submanager.data.repository.RemoteSyncRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DetalleSuscripcionActivity extends AppCompatActivity {

    private SuscripcionViewModel viewModel;
    private int suscripcionId = -1;
    private SuscripcionModel currentModel;
    private RegistrosPagoAdapter pagosAdapter;

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
        setupListeners();
        setupHistorial();

        suscripcionId = getIntent().getIntExtra("suscripcion_id", -1);
        viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        if (suscripcionId != -1) {
            viewModel.getSuscripcionById(suscripcionId).observe(this, model -> {
                if (model != null) {
                    currentModel = model;
                    loadData(model);
                } else {
                    finish();
                }
            });

            viewModel.getPagosBySuscripcion(suscripcionId).observe(this, pagos -> {
                if (pagos != null && pagosAdapter != null) {
                    pagosAdapter.setPagos(pagos);
                }
            });
        } else {
            finish();
        }

        setupSyncObserver();
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

    private void loadData(SuscripcionModel model) {
        tvDetalleName.setText(model.getNombre());
        tvDetalleCategoria.setText(model.getCategoria());
        tvDetalleMonto.setText(String.format(Locale.getDefault(), "$%.2f", model.getMonto()));
        tvDetalleCiclo.setText("por " + model.getCicloFacturacion().toLowerCase());

        int iconRes = getIntent().getIntExtra("iconRes", 0);
        if (iconRes != 0) {
            ivDetalleIcon.setImageResource(iconRes);
        } else {
            ivDetalleIcon.setImageResource(R.mipmap.ic_launcher);
        }

        tvProximoCobro.setText(formatearFecha(model.getFechaProximoCobro()));
        tvDiasParaVencer.setText(obtenerTextoDias(model.getFechaProximoCobro()));
        tvCicloInfo.setText(model.getCicloFacturacion());
        tvMetodoPago.setText(model.getMetodoPago());
        tvRecordatorio.setText(model.getDiasAnticipacion() + " días antes");
        tvCancelDate.setText(formatearFechaCancelacion(model.getFechaProximoCobro(), model.getDiasAnticipacion()));
    }

    private void setupHistorial() {
        pagosAdapter = new RegistrosPagoAdapter();
        rvUltimosPagos.setLayoutManager(new LinearLayoutManager(this));
        rvUltimosPagos.setAdapter(pagosAdapter);

        rvUltimosPagos.setVisibility(View.VISIBLE);
        btnMarcarPagado.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (suscripcionId != -1) {
                Intent intent = new Intent(this, NuevaSuscripcionActivity.class);
                intent.putExtra("suscripcion_id", suscripcionId);
                startActivity(intent);
            }
        });

        btnMarcarPagado.setOnClickListener(v -> {
            if (currentModel != null) {
                viewModel.marcarComoPagado(currentModel);
                Snackbar.make(btnMarcarPagado, "✅ Pago registrado correctamente", Snackbar.LENGTH_SHORT).show();
            }
        });

        btnEliminar.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {
        String nombre = currentModel != null ? currentModel.getNombre() : "esta suscripción";
        new MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar suscripción")
            .setMessage("¿Deseas eliminar \"" + nombre + "\"?\n\nEsta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar", (dialog, which) -> {
                if (suscripcionId != -1) {
                    viewModel.eliminar(suscripcionId);
                }
            })
            .show();
    }



    private String formatearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return "N/A";
        try {
            java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "ES"));
            java.util.Date date = sdfIn.parse(fechaStr);
            return sdfOut.format(date);
        } catch (Exception e) {
            return fechaStr;
        }
    }

    private void setupSyncObserver() {
        LinearLayout llSyncIndicator = findViewById(R.id.llSyncIndicator);
        ProgressBar pbSync = findViewById(R.id.pbSync);
        ImageView ivSyncDone = findViewById(R.id.ivSyncDone);
        TextView tvSyncStatus = findViewById(R.id.tvSyncStatus);

        if (llSyncIndicator == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(llSyncIndicator, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = statusBarHeight + 16;
            v.setLayoutParams(lp);
            return insets;
        });

        RemoteSyncRepository.isSyncing.observe(this, isSyncing -> {
            if (isSyncing != null) {
                if (isSyncing) {
                    llSyncIndicator.setVisibility(View.VISIBLE);
                    pbSync.setVisibility(View.VISIBLE);
                    ivSyncDone.setVisibility(View.GONE);
                    tvSyncStatus.setText("Sincronizando...");
                } else {
                    String result = RemoteSyncRepository.syncResult.getValue();
                    if (result != null) {
                        pbSync.setVisibility(View.GONE);
                        ivSyncDone.setVisibility(View.VISIBLE);

                        if (result.equals("SUCCESS")) {
                            tvSyncStatus.setText("Sincronizado");
                            ivSyncDone.setImageResource(R.drawable.ic_check_circle);
                            ivSyncDone.setColorFilter(ContextCompat.getColor(this, R.color.premium));
                        } else {
                            tvSyncStatus.setText("Error al sincronizar");
                            ivSyncDone.setImageResource(R.drawable.ic_check_circle);
                            ivSyncDone.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        }

                        new Handler().postDelayed(() -> {
                            if (Boolean.FALSE.equals(RemoteSyncRepository.isSyncing.getValue())) {
                                llSyncIndicator.setVisibility(View.GONE);
                            }
                        }, 3000);
                    } else {
                        llSyncIndicator.setVisibility(View.GONE);
                    }
                }
            }
        });

        RemoteSyncRepository.syncResult.observe(this, result -> {
            if (result != null && Boolean.FALSE.equals(RemoteSyncRepository.isSyncing.getValue())) {
                pbSync.setVisibility(View.GONE);
                ivSyncDone.setVisibility(View.VISIBLE);

                if (result.equals("SUCCESS")) {
                    tvSyncStatus.setText("Sincronizado");
                    ivSyncDone.setImageResource(R.drawable.ic_check_circle);
                    ivSyncDone.setColorFilter(ContextCompat.getColor(this, R.color.premium));
                } else {
                    tvSyncStatus.setText("Error al sincronizar");
                    ivSyncDone.setImageResource(R.drawable.ic_check_circle);
                    ivSyncDone.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                }

                new Handler().postDelayed(() -> {
                    if (Boolean.FALSE.equals(RemoteSyncRepository.isSyncing.getValue())) {
                        llSyncIndicator.setVisibility(View.GONE);
                    }
                }, 3000);
            }
        });
    }

    private String obtenerTextoDias(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date targetDate = sdf.parse(fechaStr);
            if (targetDate == null) return "";

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar target = Calendar.getInstance();
            target.setTime(targetDate);
            target.set(Calendar.HOUR_OF_DAY, 0);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            long diff = target.getTimeInMillis() - today.getTimeInMillis();
            long dias = TimeUnit.MILLISECONDS.toDays(diff);

            if (dias == 0) return "Hoy";
            else if (dias == 1) return "En 1 día";
            else if (dias < 0) return "Vencido";
            else return "En " + dias + " días";
        } catch (ParseException e) {
            return "";
        }
    }

    private String formatearFechaCancelacion(String fechaStr, int diasAnticipacion) {
        if (fechaStr == null || fechaStr.isEmpty()) return "N/A";
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date baseDate = sdfIn.parse(fechaStr);
            if (baseDate == null) return "N/A";

            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate);
            cal.add(Calendar.DAY_OF_YEAR, -diasAnticipacion);

            SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
            return sdfOut.format(cal.getTime()) + " · " + diasAnticipacion + " días";
        } catch (Exception e) {
            return "N/A";
        }
    }
}
