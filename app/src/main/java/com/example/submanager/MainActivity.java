package com.example.submanager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.ui.fragment.AlertasFragment;
import com.example.submanager.ui.fragment.DashboardFragment;
import com.example.submanager.ui.fragment.PerfilFragment;
import com.example.submanager.ui.fragment.HistorialFragment;
import com.example.submanager.ui.fragment.SuscripcionesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge: el contenido se dibuja detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Aplicar el inset de la barra de navegación del sistema como padding inferior
        // de la BottomNav, para que el contenido no quede oculto detrás de ella.
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    navBarHeight
            );
            return WindowInsetsCompat.CONSUMED;
        });

        // Para que el FragmentContainer también respete la barra de estado superior
        View fragmentContainer = findViewById(R.id.nav_host_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = statusBarHeight;
            v.setLayoutParams(lp);
            return WindowInsetsCompat.CONSUMED;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new DashboardFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        }

        new Thread(() ->
            AppDatabase.getInstance(this).getOpenHelper().getWritableDatabase()
        ).start();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragmentoSeleccionado = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                fragmentoSeleccionado = new DashboardFragment();
            } else if (itemId == R.id.nav_suscripciones) {
                fragmentoSeleccionado = new SuscripcionesFragment();
            } else if (itemId == R.id.nav_estadisticas) {
                fragmentoSeleccionado = new HistorialFragment();
            } else if (itemId == R.id.nav_alertas) {
                fragmentoSeleccionado = new AlertasFragment();
            } else if (itemId == R.id.nav_perfil) {
                fragmentoSeleccionado = new PerfilFragment();
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