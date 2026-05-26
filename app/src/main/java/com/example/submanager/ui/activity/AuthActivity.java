package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.model.UsuarioModel;
import com.example.submanager.data.remote.SupabaseClient;
import com.example.submanager.data.remote.dto.UsuarioDto;
import com.example.submanager.utils.CryptoUtils;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.util.Patterns;
import com.example.submanager.utils.NetworkUtils;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";
    public static final String RESULT_NOMBRE = "result_nombre";
    private static final String TAG = "AuthActivity";

    private ImageView btnBack;
    private TabLayout tabLayout;
    private LinearLayout formLogin, formRegistro;
    private TextView tvAuthTitle, tvAuthSubtitle;


    private TextInputLayout tilLoginEmail, tilLoginPassword;
    private TextInputEditText etLoginEmail, etLoginPassword;
    private MaterialButton btnIniciarSesion, btnGoogleLogin;
    private TextView tvOlvideContrasena, tvIrRegistro;


    private TextInputLayout tilRegNombre, tilRegEmail, tilRegPassword, tilRegConfirm;
    private TextInputEditText etRegNombre, etRegEmail, etRegPassword, etRegConfirm;
    private MaterialButton btnCrearCuenta;
    private TextView tvIrLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        bindViews();
        setupTabs();
        setupListeners();

        int startTab = getIntent().getIntExtra(EXTRA_TAB, 0);
        if (startTab == 1 && tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).select();
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        formLogin = findViewById(R.id.formLogin);
        formRegistro = findViewById(R.id.formRegistro);
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);

        tilLoginEmail = findViewById(R.id.tilLoginEmail);
        tilLoginPassword = findViewById(R.id.tilLoginPassword);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvOlvideContrasena = findViewById(R.id.tvOlvideContrasena);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);

        tilRegNombre = findViewById(R.id.tilRegNombre);
        tilRegEmail = findViewById(R.id.tilRegEmail);
        tilRegPassword = findViewById(R.id.tilRegPassword);
        tilRegConfirm = findViewById(R.id.tilRegConfirm);
        etRegNombre = findViewById(R.id.etRegNombre);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirm = findViewById(R.id.etRegConfirm);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        tvIrLogin = findViewById(R.id.tvIrLogin);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) showLoginForm();
                    else showRegisterForm();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            }
        );
    }

    private void showLoginForm() {
        formLogin.setVisibility(View.VISIBLE);
        formRegistro.setVisibility(View.GONE);
        tvAuthTitle.setText("Bienvenido a SubManager");
        tvAuthSubtitle.setText("Inicia sesión para sincronizar tus datos");
    }

    private void showRegisterForm() {
        formLogin.setVisibility(View.GONE);
        formRegistro.setVisibility(View.VISIBLE);
        tvAuthTitle.setText("Crea tu cuenta");
        tvAuthSubtitle.setText("Gratis · Sin tarjeta requerida");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnIniciarSesion.setOnClickListener(v -> handleLogin());

        btnGoogleLogin.setOnClickListener(v ->
            Snackbar.make(
                v,
                "Inicio con Google próximamente",
                Snackbar.LENGTH_SHORT
            ).show()
        );

        tvOlvideContrasena.setOnClickListener(v ->
            Snackbar.make(
                v,
                "Revisa tu correo para restablecer la contraseña",
                Snackbar.LENGTH_SHORT
            ).show()
        );

        tvIrRegistro.setOnClickListener(v -> {
            if (tabLayout.getTabAt(1) != null) tabLayout.getTabAt(1).select();
        });

        btnCrearCuenta.setOnClickListener(v -> handleRegister());

        tvIrLogin.setOnClickListener(v -> {
            if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).select();
        });
    }





    private void handleLogin() {
        clearErrors();
        String email =
            etLoginEmail.getText() != null
                ? etLoginEmail.getText().toString().trim().toLowerCase()
                : "";
        String password =
            etLoginPassword.getText() != null
                ? etLoginPassword.getText().toString().trim()
                : "";

        boolean valid = true;
        if (email.isEmpty()) {
            tilLoginEmail.setError("Ingresa tu correo");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilLoginEmail.setError("Correo inválido");
            valid = false;
        }
        if (password.isEmpty()) {
            tilLoginPassword.setError("Ingresa tu contraseña");
            valid = false;
        } else if (password.length() < 8) {
            tilLoginPassword.setError("La contraseña debe tener al menos 8 dígitos");
            valid = false;
        }
        if (!valid) return;

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Snackbar.make(
                btnIniciarSesion,
                "Para iniciar sesión necesitas conexión a internet. Intenta más tarde.",
                Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        btnIniciarSesion.setEnabled(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                    .getUsuarioPorCorreo("eq." + email)
                    .execute();

                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e(TAG, "Error en Supabase login: HTTP " + resp.code());
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginEmail.setError(
                            "No se pudo establecer conexión con el servidor. Intenta más tarde."
                        );
                    });
                    return;
                }

                if (resp.body().isEmpty()) {
                    UsuarioModel usuarioLocal = AppDatabase.getInstance(this)
                        .usuarioDao()
                        .findByEmail(email);
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginEmail.setError(
                            usuarioLocal != null
                                ? "Esta cuenta no existe en el servidor. Regístrate nuevamente con conexión a internet."
                                : "No existe cuenta con ese correo"
                        );
                    });
                    return;
                }

                UsuarioDto remoto = resp.body().get(0);
                if (!isValidRemoteUser(remoto)) {
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginEmail.setError(
                            "No se pudo establecer conexión con el servidor. Intenta más tarde."
                        );
                    });
                    return;
                }

                if (!isRemotePasswordValid(password, remoto)) {
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginPassword.setError("Contraseña incorrecta");
                    });
                    return;
                }

                cacheRemoteUserIfNeeded(remoto);

                SessionManager sm = new SessionManager(this);
                sm.saveSession(remoto.correo, remoto.nombre != null ? remoto.nombre : "");
                sm.saveRemoteUserId(remoto.id);
                applyRemotePremiumState(sm, remoto);

                handler.post(() -> {
                    btnIniciarSesion.setEnabled(true);
                    android.content.Intent result = new android.content.Intent();
                    result.putExtra(RESULT_NOMBRE, remoto.nombre);
                    if (sm.isPremium()) {
                        result.putExtra("result_premium", true);
                    }
                    setResult(RESULT_OK, result);
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error login remoto: " + e.getMessage(), e);
                handler.post(() -> {
                    btnIniciarSesion.setEnabled(true);
                    tilLoginEmail.setError(
                        "No se pudo establecer conexión con el servidor. Intenta más tarde."
                    );
                });
            }
        });
    }



    private boolean isValidRemoteUser(UsuarioDto usuario) {
        return usuario != null &&
            usuario.id != null &&
            usuario.id > 0 &&
            usuario.correo != null &&
            !usuario.correo.trim().isEmpty() &&
            usuario.hashContrasena != null &&
            !usuario.hashContrasena.trim().isEmpty();
    }

    private boolean isRemotePasswordValid(String password, UsuarioDto usuario) {
        String hashIntentoNuevo = CryptoUtils.hashPassword(password, usuario.correo);
        String hashIntentoViejo = CryptoUtils.hashPassword(password, "remote");
        return hashIntentoNuevo.equals(usuario.hashContrasena) ||
            hashIntentoViejo.equals(usuario.hashContrasena);
    }

    private void cacheRemoteUserIfNeeded(UsuarioDto remoto) {
        UsuarioModel existente = AppDatabase.getInstance(this)
            .usuarioDao()
            .findByEmail(remoto.correo);
        if (existente != null) return;

        UsuarioModel nuevoLocal = new UsuarioModel();
        nuevoLocal.nombre = remoto.nombre != null ? remoto.nombre : "";
        nuevoLocal.email = remoto.correo;
        nuevoLocal.salt = remoto.correo;
        nuevoLocal.passwordHash = remoto.hashContrasena;
        nuevoLocal.creadoEn =
            remoto.creadoEn != null
                ? remoto.creadoEn
                : String.valueOf(System.currentTimeMillis());
        AppDatabase.getInstance(this)
            .usuarioDao()
            .insertUsuario(nuevoLocal);
    }

    private void applyRemotePremiumState(SessionManager sm, UsuarioDto remoto) {
        if (isPremiumPlanActive(remoto)) {
            sm.savePremium(
                remoto.tipoPlan,
                remoto.fechaRenovacion != null ? remoto.fechaRenovacion : ""
            );
        } else {
            sm.clearPremium();
        }
    }

    private boolean isPremiumPlanActive(UsuarioDto remoto) {
        return remoto.tipoPlan != null &&
            !"GRATIS".equalsIgnoreCase(remoto.tipoPlan) &&
            remoto.estaActivo != null &&
            remoto.estaActivo;
    }





    private void handleRegister() {
        clearErrors();
        String nombre =
            etRegNombre.getText() != null
                ? etRegNombre.getText().toString().trim()
                : "";
        String email =
            etRegEmail.getText() != null
                ? etRegEmail.getText().toString().trim().toLowerCase()
                : "";
        String password =
            etRegPassword.getText() != null
                ? etRegPassword.getText().toString().trim()
                : "";
        String confirm =
            etRegConfirm.getText() != null
                ? etRegConfirm.getText().toString().trim()
                : "";

        boolean valid = true;
        if (nombre.isEmpty()) {
            tilRegNombre.setError("Ingresa tu nombre");
            valid = false;
        }
        if (email.isEmpty()) {
            tilRegEmail.setError("Ingresa tu correo");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegEmail.setError("Correo inválido");
            valid = false;
        }
        if (password.isEmpty()) {
            tilRegPassword.setError("Ingresa una contraseña");
            valid = false;
        } else if (password.length() < 8) {
            tilRegPassword.setError("La contraseña debe tener al menos 8 dígitos");
            valid = false;
        }
        if (confirm.isEmpty()) {
            tilRegConfirm.setError("Confirma tu contraseña");
            valid = false;
        } else if (!confirm.equals(password)) {
            tilRegConfirm.setError("Las contraseñas no coinciden");
            valid = false;
        }
        if (!valid) return;

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Snackbar.make(
                btnCrearCuenta,
                "Para crear una cuenta necesitas conexión a internet. Intenta más tarde.",
                Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        btnCrearCuenta.setEnabled(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Response<List<UsuarioDto>> checkResp = SupabaseClient.getApi()
                    .getUsuarioPorCorreo("eq." + email)
                    .execute();

                if (!checkResp.isSuccessful() || checkResp.body() == null) {
                    Log.e(TAG, "Error consultando usuario remoto: HTTP " + checkResp.code());
                    handler.post(() -> {
                        btnCrearCuenta.setEnabled(true);
                        Snackbar.make(
                            btnCrearCuenta,
                            "No se pudo establecer conexión con el servidor. Intenta más tarde.",
                            Snackbar.LENGTH_LONG
                        ).show();
                    });
                    return;
                }

                if (!checkResp.body().isEmpty()) {
                    handler.post(() -> {
                        btnCrearCuenta.setEnabled(true);
                        tilRegEmail.setError("Ya hay una cuenta con ese correo");
                    });
                    return;
                }

                UsuarioDto dto = new UsuarioDto();
                dto.nombre = nombre;
                dto.correo = email;
                dto.hashContrasena = CryptoUtils.hashPassword(password, email);
                dto.tipoPlan = "GRATIS";
                dto.fechaInicioPlan = null;
                dto.fechaRenovacion = null;
                dto.estaActivo = true;

                Response<List<UsuarioDto>> createResp =
                    SupabaseClient.getApi().createUsuario(dto).execute();

                if (
                    !createResp.isSuccessful() ||
                    createResp.body() == null ||
                    createResp.body().isEmpty() ||
                    createResp.body().get(0).id == null ||
                    createResp.body().get(0).id <= 0
                ) {
                    Log.w(TAG, "No se pudo crear usuario en Supabase");
                    handler.post(() -> {
                        btnCrearCuenta.setEnabled(true);
                        Snackbar.make(
                            btnCrearCuenta,
                            "No se pudo establecer conexión con el servidor. Intenta más tarde.",
                            Snackbar.LENGTH_LONG
                        ).show();
                    });
                    return;
                }

                UsuarioDto remoto = createResp.body().get(0);
                UsuarioModel nuevo = new UsuarioModel();
                nuevo.nombre = nombre;
                nuevo.email = email;
                nuevo.passwordHash = dto.hashContrasena;
                nuevo.salt = email;
                nuevo.creadoEn = String.valueOf(System.currentTimeMillis());
                AppDatabase.getInstance(this)
                    .usuarioDao()
                    .insertUsuario(nuevo);

                SessionManager sm = new SessionManager(this);
                sm.saveSession(email, nombre);
                sm.saveRemoteUserId(remoto.id);
                sm.clearPremium();

                handler.post(() -> {
                    btnCrearCuenta.setEnabled(true);
                    Snackbar.make(
                        btnCrearCuenta,
                        "¡Cuenta creada! Bienvenido, " + nombre,
                        Snackbar.LENGTH_LONG
                    ).show();
                    btnCrearCuenta.postDelayed(this::finish, 1200);
                });
            } catch (Exception e) {
                Log.w(TAG, "Supabase no disponible al registrar: " + e.getMessage(), e);
                handler.post(() -> {
                    btnCrearCuenta.setEnabled(true);
                    Snackbar.make(
                        btnCrearCuenta,
                        "No se pudo establecer conexión con el servidor. Intenta más tarde.",
                        Snackbar.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void clearErrors() {
        if (tilLoginEmail != null) tilLoginEmail.setError(null);
        if (tilLoginPassword != null) tilLoginPassword.setError(null);
        if (tilRegNombre != null) tilRegNombre.setError(null);
        if (tilRegEmail != null) tilRegEmail.setError(null);
        if (tilRegPassword != null) tilRegPassword.setError(null);
        if (tilRegConfirm != null) tilRegConfirm.setError(null);
    }
}
