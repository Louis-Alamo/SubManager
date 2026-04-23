package com.example.submanager.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.submanager.R;
import com.example.submanager.data.AppDatabase;
import com.example.submanager.data.model.UsuarioModel;
import com.example.submanager.data.remote.SupabaseClient;
import com.example.submanager.data.remote.dto.UsuarioDto;
import com.example.submanager.data.repository.RemoteSyncRepository;
import com.example.submanager.ui.activity.AuthActivity;
import com.example.submanager.ui.activity.PremiumActivity;
import com.example.submanager.utils.CryptoUtils;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    // ── UI references ─────────────────────────────────────────────────────────
    private MaterialSwitch switchNotificaciones;
    private TextView tvHoraRecordatorio;
    private LinearLayout rowTime;
    private LinearLayout rowBackup;
    private LinearLayout rowRestore;
    private LinearLayout rowTerms;
    private LinearLayout rowPrivacy;
    private LinearLayout rowSupport;
    private View btnHelp;

    // ── Campos de login en-pantalla ───────────────────────────────────────────
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    // ── Auth & sesión ───────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private LinearLayout loginSection;
    private LinearLayout loggedInSection;
    private TextView tvUserName;
    private TextView tvUserEmail;

    // ── Estado notificaciones ─────────────────────────────────────────────────
    private boolean notificacionesActivas = true;

    // ── Sincronización remota ─────────────────────────────────────────────────
    private RemoteSyncRepository remoteSyncRepository;
    private TextView tvUltimaSincronizacion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        remoteSyncRepository = new RemoteSyncRepository(requireContext());
        bindViews(view);
        setupListeners(view);
        updateAuthUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUI();
    }

    // ── Binding ───────────────────────────────────────────────────────────────

    private void bindViews(View root) {
        switchNotificaciones = root.findViewById(R.id.switchNotificaciones);
        tvHoraRecordatorio   = root.findViewById(R.id.tvHoraRecordatorio);
        rowTime              = root.findViewById(R.id.rowTime);
        rowBackup            = root.findViewById(R.id.rowBackup);
        rowRestore           = root.findViewById(R.id.rowRestore);
        rowTerms             = root.findViewById(R.id.rowTerms);
        rowPrivacy           = root.findViewById(R.id.rowPrivacy);
        rowSupport           = root.findViewById(R.id.rowSupport);
        btnHelp              = root.findViewById(R.id.btnHelp);

        tvUserName      = root.findViewById(R.id.tvUserName);
        tvUserEmail     = root.findViewById(R.id.tvUserEmail);
        loginSection    = root.findViewById(R.id.loginSection);
        loggedInSection = root.findViewById(R.id.loggedInSection);

        // Campos de login inline
        tilEmail    = root.findViewById(R.id.tilEmail);
        tilPassword = root.findViewById(R.id.tilPassword);
        etEmail     = root.findViewById(R.id.etEmail);
        etPassword  = root.findViewById(R.id.etPassword);

        // Estado inicial del switch
        switchNotificaciones.setChecked(notificacionesActivas);

        // Última sincronización
        tvUltimaSincronizacion = root.findViewById(R.id.tvUltimaSincronizacion);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners(View root) {

        // Switch notificaciones ──────────────────────────────────────────────
        switchNotificaciones.setOnCheckedChangeListener((btn, checked) -> {
            notificacionesActivas = checked;
            String msg = checked
                    ? "Alertas activadas"
                    : "Alertas desactivadas";
            showSnackbar(root, msg);
        });

        // Hora del recordatorio ──────────────────────────────────────────────
        rowTime.setOnClickListener(v ->
                showSnackbar(root, "Selector de hora próximamente"));

        // Respaldar ───────────────────────────────────────────────────────────
        rowBackup.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                iniciarBackup(root);
            } else {
                startActivity(new Intent(requireContext(), PremiumActivity.class));
            }
        });

        // Restaurar ───────────────────────────────────────────────────────────
        rowRestore.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                confirmarRestore(root);
            } else {
                startActivity(new Intent(requireContext(), PremiumActivity.class));
            }
        });

        // Términos ────────────────────────────────────────────────────────────
        rowTerms.setOnClickListener(v ->
                showSnackbar(root, "Abriendo Términos de Servicio…"));

        // Privacidad ──────────────────────────────────────────────────────────
        rowPrivacy.setOnClickListener(v ->
                showSnackbar(root, "Abriendo Política de Privacidad…"));

        // Soporte ─────────────────────────────────────────────────────────────
        rowSupport.setOnClickListener(v ->
                showSnackbar(root, "Abriendo Centro de Soporte…"));

        // Botón Ayuda (toolbar) ───────────────────────────────────────────────
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v ->
                    showSnackbar(root, "¿Necesitas ayuda? Soporte disponible próximamente"));
        }

        // Login inline (usar campos ya en el fragment) ──────────────────────
        View btnLogin   = root.findViewById(R.id.btnLogin);
        View tvRegister = root.findViewById(R.id.tvRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleInlineLogin(root));
        }
        // "¿No tienes cuenta?" → abrir AuthActivity directo en tab Registro
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.putExtra(AuthActivity.EXTRA_TAB, 1);
                startActivity(intent);
            });
        }

        // Premium banner ──────────────────────────────────────────────────────
        View bannerPremium = root.findViewById(R.id.bannerPremium);
        if (bannerPremium != null) {
            bannerPremium.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), PremiumActivity.class)));
        }
        // Logout ──────────────────────────────────────────────────────────
        View btnLogout = root.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.clearSession();
                updateAuthUI();
                showSnackbar(root, "Sesión cerrada");
            });
        }
    }

    // ── Estado de sesión ──────────────────────────────────────────────────────

    private void updateAuthUI() {
        if (!isAdded()) return;
        if (sessionManager.isLoggedIn()) {
            if (tvUserName  != null) tvUserName.setText(sessionManager.getNombre());
            if (tvUserEmail != null) tvUserEmail.setText(sessionManager.getEmail());
            if (loginSection    != null) loginSection.setVisibility(View.GONE);
            if (loggedInSection != null) loggedInSection.setVisibility(View.VISIBLE);
        } else {
            if (tvUserName  != null) tvUserName.setText(getString(R.string.settings_account_guest));
            if (tvUserEmail != null) tvUserEmail.setText(getString(R.string.settings_account_guest_subtitle));
            if (loginSection    != null) loginSection.setVisibility(View.VISIBLE);
            if (loggedInSection != null) loggedInSection.setVisibility(View.GONE);
        }

        // Banner Premium — oculto si ya es suscriptor
        View banner = getView() != null ? getView().findViewById(R.id.bannerPremium) : null;
        View bannerCard = banner != null ? (View) banner.getParent() : null;
        if (bannerCard != null) {
            bannerCard.setVisibility(sessionManager.isPremium() ? View.GONE : View.VISIBLE);
        }
    }

    // ── Login inline ──────────────────────────────────────────────────────────

    private static final String TAG = "PerfilFragment";

    private void handleInlineLogin(View root) {
        if (tilEmail == null || tilPassword == null) return;
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email    = etEmail    != null && etEmail.getText()    != null ? etEmail.getText().toString().trim().toLowerCase()    : "";
        String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean valid = true;
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            valid = false;
        }
        if (!valid) return;

        View btnLogin = root.findViewById(R.id.btnLogin);
        if (btnLogin != null) btnLogin.setEnabled(false);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // ── Paso 1: buscar en Room local ─────────────────────────────────
            UsuarioModel usuarioLocal = AppDatabase.getInstance(requireContext())
                    .usuarioDao().findByEmail(email);
            Log.d(TAG, "Login inline → Room local para '" + email + "': " +
                    (usuarioLocal != null ? "ENCONTRADO" : "NO encontrado"));

            if (usuarioLocal != null) {
                // Verificar contraseña local
                boolean passOk = CryptoUtils.hashPassword(password, usuarioLocal.salt)
                        .equals(usuarioLocal.passwordHash);
                handler.post(() -> {
                    if (!isAdded()) return;
                    if (btnLogin != null) btnLogin.setEnabled(true);
                    if (!passOk) {
                        tilPassword.setError("Contraseña incorrecta");
                    } else {
                        sessionManager.saveSession(usuarioLocal.email, usuarioLocal.nombre);
                        updateAuthUI();
                        showSnackbar(root, "✅ ¡Bienvenido, " + usuarioLocal.nombre + "!");
                    }
                });
                return;
            }

            // ── Paso 2: no está local → consultar Supabase ───────────────────
            Log.d(TAG, "No está en Room → buscando en Supabase: " + email);
            try {
                Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                        .getUsuarioPorCorreo("eq." + email)
                        .execute();

                Log.d(TAG, "Supabase respuesta: HTTP " + resp.code() +
                        " | isSuccessful=" + resp.isSuccessful() +
                        " | body=" + (resp.body() != null ? resp.body().size() + " registros" : "null"));

                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    UsuarioDto remoto = resp.body().get(0);
                    Log.d(TAG, "Usuario remoto encontrado: nombre=" + remoto.nombre +
                            " | correo=" + remoto.correo +
                            " | hashContrasena=" + remoto.hashContrasena);

                    // Verificar contraseña con el mismo algoritmo que AuthActivity (salt = email)
                    String hashIntento = CryptoUtils.hashPassword(password, remoto.correo);
                    Log.d(TAG, "hashIntento=" + hashIntento +
                            " | hashRemoto=" + remoto.hashContrasena);

                    if (!hashIntento.equals(remoto.hashContrasena)) {
                        handler.post(() -> {
                            if (!isAdded()) return;
                            if (btnLogin != null) btnLogin.setEnabled(true);
                            tilPassword.setError("Contraseña incorrecta");
                            // Dialog de diagnóstico para ver hashes
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("🔑 Debug: hash no coincide")
                                    .setMessage(
                                            "correo: " + remoto.correo + "\n\n" +
                                            "hash calculado:\n" + hashIntento + "\n\n" +
                                            "hash en Supabase:\n" + remoto.hashContrasena
                                    )
                                    .setPositiveButton("Cerrar", null)
                                    .show();
                        });
                        return;
                    }

                    // Guardar en Room local para futuros logins offline
                    UsuarioModel nuevoLocal = new UsuarioModel();
                    nuevoLocal.nombre    = remoto.nombre;
                    nuevoLocal.email     = remoto.correo;
                    nuevoLocal.salt      = remoto.correo; // mismo salt usado al registrar
                    nuevoLocal.passwordHash = remoto.hashContrasena;
                    nuevoLocal.creadoEn  = remoto.creadoEn != null
                            ? remoto.creadoEn
                            : String.valueOf(System.currentTimeMillis());
                    AppDatabase.getInstance(requireContext()).usuarioDao().insertUsuario(nuevoLocal);

                    sessionManager.saveSession(remoto.correo, remoto.nombre);
                    sessionManager.saveRemoteUserId(remoto.id);
                    if (remoto.tipoPlan != null && remoto.estaActivo != null && remoto.estaActivo) {
                        sessionManager.savePremium(
                                remoto.tipoPlan,
                                remoto.fechaRenovacion != null ? remoto.fechaRenovacion : ""
                        );
                    }

                    handler.post(() -> {
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        updateAuthUI();
                        String msg = sessionManager.isPremium()
                                ? "✅ ¡Bienvenido, " + remoto.nombre + "! 👑 Premium activo"
                                : "✅ ¡Bienvenido, " + remoto.nombre + "!";
                        showSnackbar(root, msg);
                    });

                } else {
                    // Usuario no encontrado ni local ni en Supabase
                    String errBodyStr = "sin errorBody";
                    try {
                        if (resp.errorBody() != null) errBodyStr = resp.errorBody().string();
                    } catch (Exception ignored) {}
                    final String errDetail = errBodyStr;
                    final int httpCode = resp.code();

                    Log.e(TAG, "Usuario no encontrado en Supabase. HTTP " + httpCode + " | body: " + errDetail);

                    handler.post(() -> {
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        tilEmail.setError("No existe cuenta con ese correo");

                        // Dialog con diagnóstico completo
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("⚠️ Debug: usuario no encontrado")
                                .setMessage(
                                        "Correo buscado: " + email + "\n\n" +
                                        "Respuesta Supabase:\n" +
                                        "HTTP " + httpCode + "\n" +
                                        "Body: " + errDetail + "\n\n" +
                                        "Posibles causas:\n" +
                                        "• El correo en Supabase tiene mayúsculas/espacios\n" +
                                        "• La columna 'correo' tiene otro nombre\n" +
                                        "• RLS (Row Level Security) bloquea la consulta\n" +
                                        "• La URL/Key de Supabase es incorrecta"
                                )
                                .setPositiveButton("Entendido", null)
                                .show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al consultar Supabase en login inline", e);
                handler.post(() -> {
                    if (!isAdded()) return;
                    if (btnLogin != null) btnLogin.setEnabled(true);
                    tilEmail.setError("Error de red al verificar cuenta");

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("❌ Error de conexión")
                            .setMessage(
                                    "No se pudo conectar a Supabase.\n\n" +
                                    "Detalle: " + e.getClass().getSimpleName() + "\n" +
                                    e.getMessage()
                            )
                            .setPositiveButton("Cerrar", null)
                            .show();
                });
            }
        });
    }

    // ── Sincronización real ───────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void iniciarBackup(View root) {
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Sincronizando con la nube…");
        progress.setCancelable(false);
        progress.show();

        remoteSyncRepository.syncAll((status, message) -> {
            if (!isAdded()) return;
            progress.dismiss();
            switch (status) {
                case SUCCESS:
                    sessionManager.saveUltimaSincronizacion(
                            java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm"))
                    );
                    actualizarUltimaSincronizacion();
                    showSnackbar(root, "✅ " + message);
                    break;
                case NO_NETWORK:
                    showSnackbar(root, "📶 Sin conexión a internet. Intenta más tarde.");
                    break;
                case NOT_PREMIUM:
                    startActivity(new Intent(requireContext(), PremiumActivity.class));
                    break;
                case ERROR:
                    // Mostrar error completo en Dialog (puede contener instrucciones largas)
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("⚠️ Error al sincronizar")
                            .setMessage(message)
                            .setPositiveButton("Entendido", null)
                            .show();
                    break;
            }
        });
    }


    private void confirmarRestore(View root) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restaurar desde la nube")
                .setMessage("¿Deseas sobrescribir todos los datos locales con los datos de tu cuenta en la nube?\n\nEsta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", (dialog, which) -> iniciarRestore(root))
                .show();
    }

    @SuppressWarnings("deprecation")
    private void iniciarRestore(View root) {
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Restaurando desde la nube…");
        progress.setCancelable(false);
        progress.show();

        remoteSyncRepository.pullAll((status, message) -> {
            if (!isAdded()) return;
            progress.dismiss();
            switch (status) {
                case SUCCESS:
                    sessionManager.saveUltimaSincronizacion(
                            java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm"))
                    );
                    actualizarUltimaSincronizacion();
                    showSnackbar(root, "✅ " + message);
                    break;
                case NO_NETWORK:
                    showSnackbar(root, "📶 Sin conexión. Intenta más tarde.");
                    break;
                case ERROR:
                    showSnackbar(root, "⚠️ " + message);
                    break;
                default:
                    break;
            }
        });
    }

    private void actualizarUltimaSincronizacion() {
        if (tvUltimaSincronizacion == null) return;
        String ts = sessionManager.getUltimaSincronizacion();
        if (ts != null && !ts.isEmpty()) {
            tvUltimaSincronizacion.setVisibility(View.VISIBLE);
            tvUltimaSincronizacion.setText("Última sincronización: " + ts);
        } else {
            tvUltimaSincronizacion.setVisibility(View.GONE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showSnackbar(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}

