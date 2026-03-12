package com.example.submanager.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.submanager.R;
import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.model.UsuarioModel;
import com.example.submanager.utils.CryptoUtils;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";

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

        // Abrir en el tab correcto según quién llamó la Activity
        int startTab = getIntent().getIntExtra(EXTRA_TAB, 0);
        if (startTab == 1 && tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).select();
        }
    }

    private void bindViews() {
        btnBack          = findViewById(R.id.btnBack);
        tabLayout        = findViewById(R.id.tabLayout);
        formLogin        = findViewById(R.id.formLogin);
        formRegistro     = findViewById(R.id.formRegistro);
        tvAuthTitle      = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle   = findViewById(R.id.tvAuthSubtitle);

        // Login
        tilLoginEmail    = findViewById(R.id.tilLoginEmail);
        tilLoginPassword = findViewById(R.id.tilLoginPassword);
        etLoginEmail     = findViewById(R.id.etLoginEmail);
        etLoginPassword  = findViewById(R.id.etLoginPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnGoogleLogin   = findViewById(R.id.btnGoogleLogin);
        tvOlvideContrasena = findViewById(R.id.tvOlvideContrasena);
        tvIrRegistro     = findViewById(R.id.tvIrRegistro);

        // Register
        tilRegNombre     = findViewById(R.id.tilRegNombre);
        tilRegEmail      = findViewById(R.id.tilRegEmail);
        tilRegPassword   = findViewById(R.id.tilRegPassword);
        tilRegConfirm    = findViewById(R.id.tilRegConfirm);
        etRegNombre      = findViewById(R.id.etRegNombre);
        etRegEmail       = findViewById(R.id.etRegEmail);
        etRegPassword    = findViewById(R.id.etRegPassword);
        etRegConfirm     = findViewById(R.id.etRegConfirm);
        btnCrearCuenta   = findViewById(R.id.btnCrearCuenta);
        tvIrLogin        = findViewById(R.id.tvIrLogin);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showLoginForm();
                } else {
                    showRegisterForm();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
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

        // Login form listeners
        btnIniciarSesion.setOnClickListener(v -> handleLogin());

        btnGoogleLogin.setOnClickListener(v ->
            Snackbar.make(v, "Inicio con Google próximamente", Snackbar.LENGTH_SHORT).show()
        );

        tvOlvideContrasena.setOnClickListener(v ->
            Snackbar.make(v, "Revisa tu correo para restablecer la contraseña", Snackbar.LENGTH_SHORT).show()
        );

        tvIrRegistro.setOnClickListener(v -> {
            tabLayout.getTabAt(1).select();
        });

        // Register form listeners
        btnCrearCuenta.setOnClickListener(v -> handleRegister());

        tvIrLogin.setOnClickListener(v -> {
            tabLayout.getTabAt(0).select();
        });
    }

    private void handleLogin() {
        clearErrors();
        String email    = etLoginEmail.getText()    != null ? etLoginEmail.getText().toString().trim()    : "";
        String password = etLoginPassword.getText() != null ? etLoginPassword.getText().toString().trim() : "";

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
            UsuarioModel usuario = AppDatabase.getInstance(this).usuarioDao().findByEmail(email);
            handler.post(() -> {
                btnIniciarSesion.setEnabled(true);
                if (usuario == null) {
                    tilLoginEmail.setError("No existe cuenta con ese correo");
                } else if (!CryptoUtils.hashPassword(password, usuario.salt).equals(usuario.passwordHash)) {
                    tilLoginPassword.setError("Contraseña incorrecta");
                } else {
                    new SessionManager(this).saveSession(usuario.email, usuario.nombre);
                    Snackbar.make(btnIniciarSesion, "✅ ¡Bienvenido, " + usuario.nombre + "!", Snackbar.LENGTH_SHORT).show();
                    btnIniciarSesion.postDelayed(this::finish, 800);
                }
            });
        });
    }

    private void handleRegister() {
        clearErrors();
        String nombre   = etRegNombre.getText()   != null ? etRegNombre.getText().toString().trim()   : "";
        String email    = etRegEmail.getText()    != null ? etRegEmail.getText().toString().trim()    : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
        String confirm  = etRegConfirm.getText()  != null ? etRegConfirm.getText().toString().trim()  : "";

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
            UsuarioModel existing = AppDatabase.getInstance(this).usuarioDao().findByEmail(email);
            if (existing != null) {
                handler.post(() -> {
                    btnCrearCuenta.setEnabled(true);
                    tilRegEmail.setError("Ya hay una cuenta con ese correo");
                });
                return;
            }
            String salt = CryptoUtils.generateSalt();
            UsuarioModel nuevo = new UsuarioModel();
            nuevo.nombre       = nombre;
            nuevo.email        = email;
            nuevo.passwordHash = CryptoUtils.hashPassword(password, salt);
            nuevo.salt         = salt;
            nuevo.creadoEn     = String.valueOf(System.currentTimeMillis());
            long id = AppDatabase.getInstance(this).usuarioDao().insertUsuario(nuevo);
            handler.post(() -> {
                btnCrearCuenta.setEnabled(true);
                if (id == -1) {
                    tilRegEmail.setError("Ya hay una cuenta con ese correo");
                } else {
                    new SessionManager(this).saveSession(email, nombre);
                    Snackbar.make(btnCrearCuenta, "🎉 ¡Cuenta creada! Bienvenido, " + nombre, Snackbar.LENGTH_SHORT).show();
                    btnCrearCuenta.postDelayed(this::finish, 800);
                }
            });
        });
    }

    private void clearErrors() {
        if (tilLoginEmail    != null) tilLoginEmail.setError(null);
        if (tilLoginPassword != null) tilLoginPassword.setError(null);
        if (tilRegNombre     != null) tilRegNombre.setError(null);
        if (tilRegEmail      != null) tilRegEmail.setError(null);
        if (tilRegPassword   != null) tilRegPassword.setError(null);
        if (tilRegConfirm    != null) tilRegConfirm.setError(null);
    }
}
