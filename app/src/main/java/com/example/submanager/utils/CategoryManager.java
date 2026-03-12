package com.example.submanager.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.example.submanager.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class CategoryManager {

    /**
     * Llena dinámicamente un ChipGroup con las categorías.
     * @param group El ChipGroup donde se insertarán.
     * @param context Contexto.
     * @param includeAllOption Si es true, añade al principio un Chip con "Todas" y lo selecciona.
     * @param selectedCategory El nombre de la categoría a seleccionar por defecto (o null).
     */
    public static void setupCategoryChips(ChipGroup group, Context context, boolean includeAllOption, String selectedCategory) {
        group.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        String[] categories = context.getResources().getStringArray(R.array.categorias_suscripciones_all);

        if (includeAllOption) {
            Chip chipTodas = (Chip) inflater.inflate(R.layout.item_category_chip, group, false);
            chipTodas.setText("Todas");
            chipTodas.setId(View.generateViewId());
            group.addView(chipTodas);
            if ("Todas".equals(selectedCategory) || selectedCategory == null) {
                chipTodas.setChecked(true);
            }
        }

        for (String cat : categories) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_category_chip, group, false);
            chip.setText(cat);
            chip.setId(View.generateViewId());
            group.addView(chip);

            if (cat.equals(selectedCategory)) {
                chip.setChecked(true);
            }
        }
    }
}
