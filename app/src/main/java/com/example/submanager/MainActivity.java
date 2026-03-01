package com.example.submanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.submanager.ui.fragment.SuscripcionesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new SuscripcionesFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_suscripciones);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragmentoSeleccionado = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
            } else if (itemId == R.id.nav_suscripciones) {
                fragmentoSeleccionado = new SuscripcionesFragment();
            } else if (itemId == R.id.nav_estadisticas) {
            } else if (itemId == R.id.nav_alertas) {
            } else if (itemId == R.id.nav_perfil) {
            }

            if (fragmentoSeleccionado != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, fragmentoSeleccionado)
                        .commit();
                return true;
            }

            return false;
        });
    }
}