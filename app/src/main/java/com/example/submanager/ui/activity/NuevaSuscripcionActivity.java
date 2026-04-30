package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.Color;
import android.content.res.ColorStateList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.submanager.ui.viewmodel.SuscripcionViewModel;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.R;
import com.example.submanager.utils.CategoryManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.GridLayout;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NuevaSuscripcionActivity extends AppCompatActivity {

    private static final String OPCION_PERSONALIZADO = "Personalizado…";
    private int iconoSeleccionadoResId = R.drawable.ic_letter_s;

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

        // ── Icon Picker ───────────────────────────────────────────────────────
        setupIconPicker();

        // ── Guardar Suscripción ───────────────────────────────────────────────
        setupGuardarButton();
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
            } else {
                calcularFechaLimite();
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
            calcularFechaLimite();
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

    // ─────────────────────────────────────────────────────────────────────────
    // Selector de Icono
    // ─────────────────────────────────────────────────────────────────────────
    private void setupIconPicker() {
        FloatingActionButton fabEdit = findViewById(R.id.fabEditAvatar);
        ImageView ivAvatar = findViewById(R.id.ivSuscripcionAvatar);

        int[] availableIcons = {
            R.drawable.ic_app_netflix, R.drawable.ic_app_spotify, R.drawable.ic_app_youtube,
            R.drawable.ic_app_xbox, R.drawable.ic_app_mercadolibre, R.drawable.ic_app_apple_music,
            R.drawable.ic_app_crunchyroll, R.drawable.ic_app_duolingo, R.drawable.ic_app_google,
            R.drawable.ic_app_twitch, R.drawable.ic_app_hbomax, R.drawable.ic_app_prime_video,
            R.drawable.ic_app_tiktok, R.drawable.ic_app_paramount, R.drawable.ic_app_disneyplus,
            R.drawable.ic_app_copilot, R.drawable.ic_service_home, R.drawable.ic_service_card,
            R.drawable.ic_service_electricity, R.drawable.ic_service_gas, R.drawable.ic_service_water,
            R.drawable.ic_service_internet, R.drawable.ic_service_phone, R.drawable.ic_service_fitness
        };

        fabEdit.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_picker, null);
            GridLayout gridLayout = dialogView.findViewById(R.id.gridLayoutIcons);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            for (int iconResId : availableIcons) {
                View iconWrapper = LayoutInflater.from(this).inflate(R.layout.item_icon_selector, gridLayout, false);
                ImageView iconImageView = iconWrapper.findViewById(R.id.ivIcon);
                iconImageView.setImageResource(iconResId);

                iconWrapper.setOnClickListener(iv -> {
                    iconoSeleccionadoResId = iconResId;
                    ivAvatar.setImageResource(iconResId);
                    alertDialog.dismiss();
                });

                gridLayout.addView(iconWrapper);
            }

            dialogView.findViewById(R.id.btnCancelIcon).setOnClickListener(btn -> alertDialog.dismiss());

            alertDialog.show();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica para Guardar en Base de Datos
    // ─────────────────────────────────────────────────────────────────────────
    private void setupGuardarButton() {
        SuscripcionViewModel viewModel = new ViewModelProvider(this).get(SuscripcionViewModel.class);

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            TextInputEditText etNombre = findViewById(R.id.etNombre);
            TextInputEditText etMonto = findViewById(R.id.etMonto);
            AutoCompleteTextView autoCompleteCiclo = findViewById(R.id.autoCompleteCiclo);
            AutoCompleteTextView autoCompletePago = findViewById(R.id.autoCompletePago);
            TextInputEditText etFecha = findViewById(R.id.etFecha);
            TextInputEditText etFechaLimite = findViewById(R.id.etFechaLimite);
            TextInputEditText etDiasAnticipacion = findViewById(R.id.etDiasAnticipacion);
            ChipGroup chipGroupCategorias = findViewById(R.id.chipGroupCategorias);

            String nombre = etNombre.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();
            String ciclo = autoCompleteCiclo.getText().toString().trim();
            String metodoPago = autoCompletePago.getText().toString().trim();
            String fechaPrimerCobro = etFecha.getText().toString().trim();
            String fechaLimite = etFechaLimite.getText() != null ? etFechaLimite.getText().toString().trim() : "";
            String diasStr = etDiasAnticipacion.getText() != null ? etDiasAnticipacion.getText().toString().trim() : "";

            if (nombre.isEmpty() || montoStr.isEmpty() || ciclo.isEmpty() || metodoPago.isEmpty() || fechaPrimerCobro.isEmpty()) {
                Toast.makeText(this, "Por favor, completa los campos obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int diasAnticipacion = 3;
            if (!diasStr.isEmpty()) {
                try {
                    diasAnticipacion = Integer.parseInt(diasStr);
                } catch (NumberFormatException e) {
                    diasAnticipacion = 3;
                }
            }

            // Validar categoría seleccionada
            int checkedChipId = chipGroupCategorias.getCheckedChipId();
            String categoria = "Otra";
            if (checkedChipId != View.NO_ID) {
                Chip chip = findViewById(checkedChipId);
                if (chip != null) {
                    categoria = chip.getText().toString();
                }
            }

            // Obtener nombre del icono a partir de su ID
            String nombreIcono = getResources().getResourceEntryName(iconoSeleccionadoResId);

            // Fecha de creación ISO básica
            String timestampActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Instanciar modelo
            SuscripcionModel nuevaSuscripcion = new SuscripcionModel(
                    nombre,
                    monto,
                    ciclo,
                    colorSeleccionado,
                    categoria,
                    metodoPago,
                    fechaPrimerCobro,
                    fechaPrimerCobro, // Usamos la misma para proximo cobro inicial
                    fechaLimite.isEmpty() ? null : fechaLimite,
                    true, // recordatorio habilitado
                    diasAnticipacion, // configurado por usuario
                    false, // notificacion no silenciada
                    true,  // activa por defecto
                    nombreIcono,
                    timestampActual,
                    timestampActual
            );

            // Guardar en Room usando ViewModel
            viewModel.insertar(nuevaSuscripcion);

            Toast.makeText(this, "Suscripción guardada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void calcularFechaLimite() {
        TextInputEditText etFecha = findViewById(R.id.etFecha);
        TextInputEditText etFechaLimite = findViewById(R.id.etFechaLimite);
        AutoCompleteTextView autoCompleteCiclo = findViewById(R.id.autoCompleteCiclo);
        
        String fechaStr = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        String ciclo = autoCompleteCiclo.getText() != null ? autoCompleteCiclo.getText().toString().trim() : "";
        
        if (fechaStr.isEmpty() || ciclo.isEmpty() || OPCION_PERSONALIZADO.equals(ciclo)) {
            return; // No hay suficientes datos para calcular
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date firstDate = sdf.parse(fechaStr);
            if (firstDate == null) return;
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(firstDate);
            
            // Sumar el ciclo de facturación
            switch (ciclo.toLowerCase()) {
                case "diario": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                case "semanal": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                case "quincenal": cal.add(java.util.Calendar.DAY_OF_YEAR, 15); break;
                case "mensual": cal.add(java.util.Calendar.MONTH, 1); break;
                case "bimestral": cal.add(java.util.Calendar.MONTH, 2); break;
                case "trimestral": cal.add(java.util.Calendar.MONTH, 3); break;
                case "semestral": cal.add(java.util.Calendar.MONTH, 6); break;
                case "anual": cal.add(java.util.Calendar.YEAR, 1); break;
                default: return; // No auto-calcular si no está mapeado
            }
            
            // Restar un día
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            
            etFechaLimite.setText(sdf.format(cal.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}