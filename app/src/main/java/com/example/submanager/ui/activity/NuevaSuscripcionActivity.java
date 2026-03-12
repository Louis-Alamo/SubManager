package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.example.submanager.utils.CategoryManager;
import com.google.android.material.chip.ChipGroup;

public class NuevaSuscripcionActivity extends AppCompatActivity {

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
    }
}