package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.Color;
import android.content.res.ColorStateList;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.example.submanager.utils.CategoryManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NuevaSuscripcionActivity extends AppCompatActivity {

    private static final String OPCION_PERSONALIZADO = "Personalizado…";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_suscripcion);

        // Flecha de regreso → cierra esta Activity y vuelve al fragmento anterior
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Inicializar Chips de Categoría
        ChipGroup chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        CategoryManager.setupCategoryChips(chipGroupCategorias, this, false, "Entretenimiento");

        // ── Ciclo de Facturación ──────────────────────────────────────────────
        setupCicloFacturacion();

        // ── Primera Fecha de Cobro ────────────────────────────────────────────
        setupFechaCobro();

        // ── Fecha Límite de Cancelación ───────────────────────────────────────
        setupFechaLimiteCancelacion();

        // ── Apariencia (Color Picker) ─────────────────────────────────────────
        setupColorPicker();

        // ── Método de Pago ────────────────────────────────────────────────────
        setupMetodoPago();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de facturación: dropdown con opciones predefinidas + personalizado
    // ─────────────────────────────────────────────────────────────────────────
    private void setupCicloFacturacion() {
        AutoCompleteTextView autoCompleteCiclo = findViewById(R.id.autoCompleteCiclo);

        String[] ciclos = getResources().getStringArray(R.array.ciclos_facturacion);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ciclos);
        autoCompleteCiclo.setAdapter(adapter);

        // Mostrar siempre el desplegable completo al pulsar el campo
        autoCompleteCiclo.setOnClickListener(v -> autoCompleteCiclo.showDropDown());

        // Si se elige "Personalizado…" → limpiar y abrir teclado
        autoCompleteCiclo.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);
            if (OPCION_PERSONALIZADO.equals(seleccionado)) {
                autoCompleteCiclo.setText("");
                autoCompleteCiclo.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(autoCompleteCiclo,
                            android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Método de pago: dropdown con opciones predefinidas + personalizado
    // ─────────────────────────────────────────────────────────────────────────
    private void setupMetodoPago() {
        AutoCompleteTextView autoCompletePago = findViewById(R.id.autoCompletePago);

        String[] metodos = getResources().getStringArray(R.array.metodos_pago);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                metodos);
        autoCompletePago.setAdapter(adapter);

        // Mostrar siempre el desplegable completo al pulsar el campo
        autoCompletePago.setOnClickListener(v -> autoCompletePago.showDropDown());

        // Si se elige "Personalizado…" → limpiar y abrir teclado
        autoCompletePago.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);
            if (OPCION_PERSONALIZADO.equals(seleccionado)) {
                autoCompletePago.setText("");
                autoCompletePago.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(autoCompletePago,
                            android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fecha de cobro: abre MaterialDatePicker al tocar el campo o el ícono
    // ─────────────────────────────────────────────────────────────────────────
    private void setupFechaCobro() {
        TextInputLayout tilFecha = findViewById(R.id.tilFecha);
        TextInputEditText etFecha = findViewById(R.id.etFecha);

        // Formato de fecha a mostrar en el campo
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        // Construir el date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Seleccionar fecha de cobro")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        // Al confirmar, escribir la fecha seleccionada en el campo
        datePicker.addOnPositiveButtonClickListener(selection -> {
            String fechaFormateada = sdf.format(new Date(selection));
            etFecha.setText(fechaFormateada);
        });

        // Abrir el calendario al tocar el campo de texto
        etFecha.setOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        // Abrir el calendario al tocar el ícono de calendario
        tilFecha.setEndIconOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fecha límite de cancelación: opcional, material date picker
    // ─────────────────────────────────────────────────────────────────────────
    private void setupFechaLimiteCancelacion() {
        TextInputLayout tilFechaLimite = findViewById(R.id.tilFechaLimite);
        TextInputEditText etFechaLimite = findViewById(R.id.etFechaLimite);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Seleccionar fecha límite")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String fechaFormateada = sdf.format(new Date(selection));
            etFechaLimite.setText(fechaFormateada);
        });

        etFechaLimite.setOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "CANCEL_DATE_PICKER");
            }
        });

        tilFechaLimite.setEndIconOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "CANCEL_DATE_PICKER");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Selector de color (Apariencia)
    // ─────────────────────────────────────────────────────────────────────────
    private String colorSeleccionado = "#2196F3"; // Por defecto azul

    private void setupColorPicker() {
        ImageView ivAvatar = findViewById(R.id.ivSuscripcionAvatar);
        
        int[] colorIds = {
            R.id.colorBlue, R.id.colorPurple, R.id.colorGreen, 
            R.id.colorOrange, R.id.colorRed, R.id.colorPink
        };

        for (int id : colorIds) {
            ImageView colorView = findViewById(id);
            if (colorView == null) continue;

            colorView.setOnClickListener(v -> {
                // Deseleccionar todos
                for (int otherId : colorIds) {
                    ImageView otherView = findViewById(otherId);
                    if (otherView != null) otherView.setAlpha(0.4f);
                }
                
                // Seleccionar el tocado
                v.setAlpha(1.0f);
                colorSeleccionado = (String) v.getTag();
                
                // Actualizar avatar
                ivAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorSeleccionado)));
            });
        }
        
        // Inicializar el avatar con el color por defecto
        ivAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorSeleccionado)));
    }
}