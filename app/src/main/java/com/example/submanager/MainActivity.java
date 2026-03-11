package com.example.submanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.ui.fragment.HomeFragment;
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
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        }

        // Corrección: Usar 'this' en lugar de 'requireContext()' ya que estamos en una Activity
        // Además, las operaciones de Room no deben hacerse en el hilo principal sin .allowMainThreadQueries()
        // Por ahora lo comentamos o lo envolvemos en un hilo si es solo para prueba.
        new Thread(() -> {
            AppDatabase.getInstance(this).getOpenHelper().getWritableDatabase();        }).start();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragmentoSeleccionado = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                fragmentoSeleccionado = new HomeFragment();
            } else if (itemId == R.id.nav_suscripciones) {
                fragmentoSeleccionado = new SuscripcionesFragment();
            } else if (itemId == R.id.nav_estadisticas) {
                // fragmentoSeleccionado = new EstadisticasFragment();
            } else if (itemId == R.id.nav_alertas) {
                // fragmentoSeleccionado = new AlertasFragment();
            } else if (itemId == R.id.nav_perfil) {
                // fragmentoSeleccionado = new PerfilFragment();
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