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
    private LinearLayout containerPersonas;
    private MaterialButton btnGuardarServicio, btnAgregarPersona;
    private android.widget.TextView tvMontoVariableNote;



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
        btnGuardarServicio   = findViewById(R.id.btnGuardarServicio);
        btnAgregarPersona    = findViewById(R.id.btnAgregarPersona);
        tvMontoVariableNote  = findViewById(R.id.tvMontaVariableNote);
    }

    private void setupChipSelection() {

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

        if (selectedChip != null) {
            selectedChip.setBackgroundTintList(
                androidx.core.content.ContextCompat.getColorStateList(this, R.color.background));


            if (selectedChip.getChildCount() >= 2) {
                android.widget.ImageView icon = (android.widget.ImageView) selectedChip.getChildAt(0);
                android.widget.TextView text = (android.widget.TextView) selectedChip.getChildAt(1);

                icon.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.text_secondary));
                text.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
            }
        }


        selectedChip = chip;
        chip.setBackgroundTintList(
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.primary_tint));

        if (chip.getChildCount() >= 2) {
            android.widget.ImageView icon = (android.widget.ImageView) chip.getChildAt(0);
            android.widget.TextView text = (android.widget.TextView) chip.getChildAt(1);

            icon.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.primary));
            text.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary));
        }
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

            Snackbar.make(btnGuardarServicio, "Servicio guardado correctamente", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }
}
