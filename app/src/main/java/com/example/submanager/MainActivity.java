package com.example.submanager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.repository.RemoteSyncRepository;
import com.example.submanager.ui.activity.AuthActivity;
import com.example.submanager.ui.fragment.AlertasFragment;
import com.example.submanager.ui.fragment.DashboardFragment;
import com.example.submanager.ui.fragment.PerfilFragment;
import com.example.submanager.ui.fragment.HistorialFragment;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import com.example.submanager.ui.fragment.SuscripcionesFragment;
import com.example.submanager.worker.AlertWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> authLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        authLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nombre = result.getData().getStringExtra(AuthActivity.RESULT_NOMBRE);
                    boolean esPremium = result.getData().getBooleanExtra("result_premium", false);


                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    if (!(current instanceof com.example.submanager.ui.fragment.DashboardFragment)) {
                        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new com.example.submanager.ui.fragment.DashboardFragment())
                            .commit();
                        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_inicio);
                    }


                    View anchor = findViewById(R.id.nav_host_fragment);
                    if (anchor != null && nombre != null && !nombre.isEmpty()) {
                        String mensaje = esPremium
                            ? "¡Bienvenido, " + nombre + "! 👑 Premium activo"
                            : "¡Bienvenido, " + nombre + "!";
                        com.google.android.material.snackbar.Snackbar
                            .make(anchor, mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                            .show();
                    }


                    if (esPremium) {
                        new RemoteSyncRepository(this).pullAll((status, message) -> {

                        });
                    }
                }
            }
        );


        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }


        PeriodicWorkRequest alertWorkRequest = new PeriodicWorkRequest.Builder(AlertWorker.class, 12, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AlertWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                alertWorkRequest
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);



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


        View fragmentContainer = findViewById(R.id.nav_host_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = statusBarHeight;
            v.setLayoutParams(lp);
            return insets;
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


        setupSyncObserver();
    }


    public void launchAuthActivity(int startTab) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_TAB, startTab);
        authLauncher.launch(intent);
    }

    private void setupSyncObserver() {
        LinearLayout llSyncIndicator = findViewById(R.id.llSyncIndicator);
        ProgressBar pbSync = findViewById(R.id.pbSync);
        ImageView ivSyncDone = findViewById(R.id.ivSyncDone);
        TextView tvSyncStatus = findViewById(R.id.tvSyncStatus);


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
}