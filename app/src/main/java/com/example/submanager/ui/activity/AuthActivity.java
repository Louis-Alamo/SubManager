package com.example.submanager.ui.activity;

import android.app.ProgressDialog;
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
import com.example.submanager.data.repository.RemoteSyncRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";
    private static final String TAG = "AuthActivity";

    private ImageView btnBack;
    private TabLayout tabLayout;
    private LinearLayout formLogin, formRegistro;
    private TextView tvAuthTitle, tvAuthSubtitle;

    // Login form
    private TextInputLayout tilLoginEmail, tilLoginPassword;
    private TextInputEditText etLoginEmail, etLoginPassword;
    private MaterialButton btnIniciarSesion, btnGoogleLogin;
    private TextView tvOlvideContrasena, tvIrRegistro;

    // Register form
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

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN — busca primero en Room, luego en Supabase si no encuentra
    // ─────────────────────────────────────────────────────────────────────────

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
        }
        if (password.isEmpty()) {
            tilLoginPassword.setError("Ingresa tu contraseña");
            valid = false;
        }
        if (!valid) return;

        btnIniciarSesion.setEnabled(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // 1. Buscar en Room local
            UsuarioModel usuarioLocal = AppDatabase.getInstance(this)
                .usuarioDao()
                .findByEmail(email);

            if (usuarioLocal != null) {
                // Verificar contraseña local
                if (
                    !CryptoUtils.hashPassword(
                        password,
                        usuarioLocal.salt
                    ).equals(usuarioLocal.passwordHash)
                ) {
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginPassword.setError("Contraseña incorrecta");
                    });
                    return;
                }
                // Login exitoso (local)
                SessionManager sm = new SessionManager(this);
                sm.saveSession(usuarioLocal.email, usuarioLocal.nombre);

                // Intentar recuperar ID remoto y estado Premium silenciosamente si hay internet
                try {
                    Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                        .getUsuarioPorCorreo("eq." + email)
                        .execute();
                    if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                        UsuarioDto remoto = resp.body().get(0);
                        sm.saveRemoteUserId(remoto.id);
                        if (remoto.tipoPlan != null && remoto.estaActivo != null && remoto.estaActivo) {
                            sm.savePremium(remoto.tipoPlan, remoto.fechaRenovacion != null ? remoto.fechaRenovacion : "");
                        } else {
                            sm.clearPremium();
                        }
                    }
                } catch (Exception ignored) {}

                handler.post(() -> {
                    btnIniciarSesion.setEnabled(true);
                    Snackbar.make(
                        btnIniciarSesion,
                        "¡Bienvenido, " + usuarioLocal.nombre + "!",
                        Snackbar.LENGTH_SHORT
                    ).show();
                    btnIniciarSesion.postDelayed(this::finish, 800);
                });
            } else {
                // 2. No encontrado localmente → buscar en Supabase
                try {
                    Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                        .getUsuarioPorCorreo("eq." + email)
                        .execute();

                    if (
                        resp.isSuccessful() &&
                        resp.body() != null &&
                        !resp.body().isEmpty()
                    ) {
                        UsuarioDto remoto = resp.body().get(0);

                        // Verificar contraseña con el hash remoto (soportando el algoritmo viejo y nuevo)
                        String hashIntentoNuevo = CryptoUtils.hashPassword(password, remoto.correo);
                        String hashIntentoViejo = CryptoUtils.hashPassword(password, "remote");

                        if (!hashIntentoNuevo.equals(remoto.hashContrasena) && !hashIntentoViejo.equals(remoto.hashContrasena)) {
                            handler.post(() -> {
                                btnIniciarSesion.setEnabled(true);
                                tilLoginPassword.setError(
                                    "Contraseña incorrecta"
                                );
                            });
                            return;
                        }

                        // Guardar en Room local para futuros logins offline
                        UsuarioModel nuevoLocal = new UsuarioModel();
                        nuevoLocal.nombre = remoto.nombre;
                        nuevoLocal.email = remoto.correo;
                        nuevoLocal.salt = remoto.correo; // mismo salt que usamos al registrar
                        nuevoLocal.passwordHash = remoto.hashContrasena;
                        nuevoLocal.creadoEn =
                            remoto.creadoEn != null
                                ? remoto.creadoEn
                                : String.valueOf(System.currentTimeMillis());
                        AppDatabase.getInstance(this)
                            .usuarioDao()
                            .insertUsuario(nuevoLocal);

                        // Restaurar estado Premium si el plan es activo
                        SessionManager sm = new SessionManager(this);
                        sm.saveSession(remoto.correo, remoto.nombre);
                        sm.saveRemoteUserId(remoto.id);
                        if (
                            remoto.tipoPlan != null &&
                            remoto.estaActivo != null &&
                            remoto.estaActivo
                        ) {
                            sm.savePremium(
                                remoto.tipoPlan,
                                remoto.fechaRenovacion != null
                                    ? remoto.fechaRenovacion
                                    : ""
                            );
                        }

                        handler.post(() -> {
                            btnIniciarSesion.setEnabled(true);
                            if (sm.isPremium()) {
                                Snackbar.make(
                                    btnIniciarSesion,
                                    "¡Bienvenido, " + remoto.nombre + "! 👑 Premium activo\nDescargando tus datos...",
                                    Snackbar.LENGTH_SHORT
                                ).show();
                                
                                ProgressDialog progress = new ProgressDialog(AuthActivity.this);
                                progress.setMessage("Restaurando desde la nube…");
                                progress.setCancelable(false);
                                progress.show();
                                
                                new RemoteSyncRepository(AuthActivity.this).pullAll((status, message) -> {
                                    if (progress.isShowing()) progress.dismiss();
                                    finish();
                                });
                            } else {
                                Snackbar.make(
                                    btnIniciarSesion,
                                    "¡Bienvenido, " + remoto.nombre + "!",
                                    Snackbar.LENGTH_SHORT
                                ).show();
                                btnIniciarSesion.postDelayed(this::finish, 800);
                            }
                        });
                    } else {
                        String errBody =
                            resp.errorBody() != null
                                ? resp.errorBody().string()
                                : "sin errorBody";
                        Log.e(
                            TAG,
                            "Error en Supabase login: HTTP " +
                                resp.code() +
                                " - " +
                                errBody
                        );
                        handler.post(() -> {
                            btnIniciarSesion.setEnabled(true);
                            tilLoginEmail.setError(
                                "No existe cuenta con ese correo"
                            );
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error login remoto: " + e.getMessage(), e);
                    handler.post(() -> {
                        btnIniciarSesion.setEnabled(true);
                        tilLoginEmail.setError(
                            "Error de red o la cuenta no existe"
                        );
                    });
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRO — Room local + Supabase en paralelo
    // ─────────────────────────────────────────────────────────────────────────

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
        }
        if (password.isEmpty()) {
            tilRegPassword.setError("Ingresa una contraseña");
            valid = false;
        } else if (password.length() < 6) {
            tilRegPassword.setError("Mínimo 6 caracteres");
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

        btnCrearCuenta.setEnabled(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Verificar si ya existe localmente
            UsuarioModel existing = AppDatabase.getInstance(this)
                .usuarioDao()
                .findByEmail(email);
            if (existing != null) {
                handler.post(() -> {
                    btnCrearCuenta.setEnabled(true);
                    tilRegEmail.setError("Ya hay una cuenta con ese correo");
                });
                return;
            }

            // ── 1. Crear en Room local ────────────────────────────────────────
            String salt = CryptoUtils.generateSalt();
            // Para Supabase usamos el email como salt (permite verificar sin salt almacenado)
            String hashRemoto = CryptoUtils.hashPassword(password, email);

            UsuarioModel nuevo = new UsuarioModel();
            nuevo.nombre = nombre;
            nuevo.email = email;
            nuevo.passwordHash = CryptoUtils.hashPassword(password, salt);
            nuevo.salt = salt;
            nuevo.creadoEn = String.valueOf(System.currentTimeMillis());
            long localId = AppDatabase.getInstance(this)
                .usuarioDao()
                .insertUsuario(nuevo);

            if (localId == -1) {
                handler.post(() -> {
                    btnCrearCuenta.setEnabled(true);
                    tilRegEmail.setError("Ya hay una cuenta con ese correo");
                });
                return;
            }

            // ── 2. Crear en Supabase (intento en background, no bloquea) ──────
            long remoteId = -1;
            try {
                // Verificar si ya existe en Supabase
                Response<List<UsuarioDto>> checkResp = SupabaseClient.getApi()
                    .getUsuarioPorCorreo("eq." + email)
                    .execute();

                boolean existeRemoto =
                    checkResp.isSuccessful() &&
                    checkResp.body() != null &&
                    !checkResp.body().isEmpty();

                if (!existeRemoto) {
                    // Crear cuenta base en Supabase
                    UsuarioDto dto = new UsuarioDto();
                    dto.nombre = nombre;
                    dto.correo = email;
                    dto.hashContrasena = hashRemoto; // hash usando email como salt
                    dto.tipoPlan = "GRATIS";
                    dto.fechaInicioPlan = null;
                    dto.fechaRenovacion = null;
                    dto.estaActivo = true;

                    Response<List<UsuarioDto>> createResp =
                        SupabaseClient.getApi().createUsuario(dto).execute();

                    if (
                        createResp.isSuccessful() &&
                        createResp.body() != null &&
                        !createResp.body().isEmpty()
                    ) {
                        remoteId = createResp.body().get(0).id;
                        Log.i(TAG, "Usuario creado en Supabase con ID: " + remoteId);
                    } else {
                        Log.w(TAG, "No se pudo crear usuario en Supabase");
                    }
                } else {
                    // Ya existe remotamente, jalamos el ID para vincular cuenta
                    remoteId = checkResp.body().get(0).id;
                    Log.i(TAG, "Cuenta ya existía remotamente, vinculada con ID: " + remoteId);
                }
            } catch (Exception e) {
                // Sin red o error de Supabase → la cuenta local ya está creada, continuar
                Log.w(
                    TAG,
                    "Supabase no disponible al registrar: " + e.getMessage()
                );
            }

            // ── 3. Guardar sesión ─────────────────────────────────────────────
            SessionManager sm = new SessionManager(this);
            sm.saveSession(email, nombre);
            if (remoteId != -1) sm.saveRemoteUserId(remoteId);

            final long finalRemoteId = remoteId;
            handler.post(() -> {
                btnCrearCuenta.setEnabled(true);
                String extraMsg =
                    finalRemoteId != -1
                        ? " Cuenta vinculada a la nube."
                        : " (sin conexión, solo local)";
                Snackbar.make(
                    btnCrearCuenta,
                    "¡Cuenta creada! Bienvenido, " + nombre + extraMsg,
                    Snackbar.LENGTH_LONG
                ).show();
                btnCrearCuenta.postDelayed(this::finish, 1200);
            });
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
