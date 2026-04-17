package com.example.submanager.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.submanager.ui.activity.AuthActivity;
import com.example.submanager.ui.activity.PremiumActivity;
import com.example.submanager.utils.CryptoUtils;
import com.example.submanager.utils.SessionManager;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                showSnackbar(root, "☁️ Respaldo iniciado. Tus datos están seguros.");
            } else {
                startActivity(new Intent(requireContext(), PremiumActivity.class));
            }
        });

        // Restaurar ───────────────────────────────────────────────────────────
        rowRestore.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                showSnackbar(root, "✅ Restauración completada desde la nube.");
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

    private void handleInlineLogin(View root) {
        if (tilEmail == null || tilPassword == null) return;
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email    = etEmail    != null && etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
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
            UsuarioModel usuario = AppDatabase.getInstance(requireContext()).usuarioDao().findByEmail(email);
            handler.post(() -> {
                if (!isAdded()) return;
                if (btnLogin != null) btnLogin.setEnabled(true);
                if (usuario == null) {
                    tilEmail.setError("No existe cuenta con ese correo");
                } else if (!CryptoUtils.hashPassword(password, usuario.salt).equals(usuario.passwordHash)) {
                    tilPassword.setError("Contraseña incorrecta");
                } else {
                    sessionManager.saveSession(usuario.email, usuario.nombre);
                    updateAuthUI();
                    showSnackbar(root, "✅ ¡Bienvenido, " + usuario.nombre + "!");
                }
            });
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showSnackbar(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
