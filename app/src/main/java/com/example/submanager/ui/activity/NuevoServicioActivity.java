package com.example.submanager.ui.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;

public class NuevoServicioActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout chipLuz, chipAgua, chipGas, chipInternet, chipTelefono, chipOtro;
    private LinearLayout selectedChip;
    private SwitchMaterial switchMontoVariable, switchCompartido;
    private TextInputEditText etNombreServicio, etProveedor, etMontoServicio;
    private TextInputEditText etFechaCorte, etNotasServicio;
    private TextInputLayout tilMonto;
    private LinearLayout containerPersonas, cardEscanear;
    private MaterialButton btnGuardarServicio, btnEscanear, btnAgregarPersona;
    private android.widget.TextView tvMontoVariableNote;

    // Color constants for selected/unselected chips
    private static final int COLOR_SELECTED_BG   = 0xFFEFF6FF;  // primary_tint
    private static final int COLOR_UNSELECTED_BG = 0xFFF4F5F7;  // background

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_servicio);

        bindViews();
        setupChipSelection();
        setupDatePicker();
        setupSwitches();
        setupButtons();
    }

    private void bindViews() {
        btnBack              = findViewById(R.id.btnBack);
        chipLuz              = findViewById(R.id.chipLuz);
        chipAgua             = findViewById(R.id.chipAgua);
        chipGas              = findViewById(R.id.chipGas);
        chipInternet         = findViewById(R.id.chipInternet);
        chipTelefono         = findViewById(R.id.chipTelefono);
        chipOtro             = findViewById(R.id.chipOtro);
        switchMontoVariable  = findViewById(R.id.switchMontoVariable);
        switchCompartido     = findViewById(R.id.switchCompartido);
        etNombreServicio     = findViewById(R.id.etNombreServicio);
        etProveedor          = findViewById(R.id.etProveedor);
        etMontoServicio      = findViewById(R.id.etMontoServicio);
        etFechaCorte         = findViewById(R.id.etFechaCorte);
        etNotasServicio      = findViewById(R.id.etNotasServicio);
        tilMonto             = findViewById(R.id.tilMonto);
        containerPersonas    = findViewById(R.id.containerPersonas);
        cardEscanear         = findViewById(R.id.cardEscanear);
        btnGuardarServicio   = findViewById(R.id.btnGuardarServicio);
        btnEscanear          = findViewById(R.id.btnEscanear);
        btnAgregarPersona    = findViewById(R.id.btnAgregarPersona);
        tvMontoVariableNote  = findViewById(R.id.tvMontaVariableNote);
    }

    private void setupChipSelection() {
        // Default selection: Luz
        selectChip(chipLuz);

        View.OnClickListener chipListener = v -> {
            selectChip((LinearLayout) v);
        };

        chipLuz.setOnClickListener(chipListener);
        chipAgua.setOnClickListener(chipListener);
        chipGas.setOnClickListener(chipListener);
        chipInternet.setOnClickListener(chipListener);
        chipTelefono.setOnClickListener(chipListener);
        chipOtro.setOnClickListener(chipListener);
    }

    private void selectChip(LinearLayout chip) {
        // Deselect previous
        if (selectedChip != null) {
            selectedChip.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(COLOR_UNSELECTED_BG));
        }
        // Select new
        selectedChip = chip;
        chip.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(COLOR_SELECTED_BG));
    }

    private void setupDatePicker() {
        etFechaCorte.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = String.format("%02d/%02d/%04d", day, month + 1, year);
                    etFechaCorte.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupSwitches() {
        switchMontoVariable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvMontoVariableNote.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                tilMonto.setHint("Monto estimado (MXN)");
            } else {
                tilMonto.setHint("Monto (MXN)");
            }
        });

        switchCompartido.setOnCheckedChangeListener((buttonView, isChecked) -> {
            containerPersonas.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if (btnAgregarPersona != null) {
            btnAgregarPersona.setOnClickListener(v ->
                Snackbar.make(v, "Función de gastos compartidos próximamente", Snackbar.LENGTH_SHORT).show()
            );
        }

        if (cardEscanear != null) {
            cardEscanear.setOnClickListener(v ->
                Snackbar.make(v, "📷 Escáner de recibos próximamente", Snackbar.LENGTH_SHORT).show()
            );
        }

        if (btnEscanear != null) {
            btnEscanear.setOnClickListener(v ->
                Snackbar.make(v, "📷 Escáner de recibos próximamente", Snackbar.LENGTH_SHORT).show()
            );
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnGuardarServicio.setOnClickListener(v -> {
            String nombre = etNombreServicio.getText() != null
                ? etNombreServicio.getText().toString().trim() : "";
            String monto  = etMontoServicio.getText() != null
                ? etMontoServicio.getText().toString().trim() : "";

            if (nombre.isEmpty()) {
                etNombreServicio.setError("Ingresa el nombre del servicio");
                etNombreServicio.requestFocus();
                return;
            }
            if (monto.isEmpty()) {
                etMontoServicio.setError("Ingresa el monto");
                etMontoServicio.requestFocus();
                return;
            }

            Snackbar.make(btnGuardarServicio, "✅ Servicio guardado correctamente", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }
}
