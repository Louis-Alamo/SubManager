package com.example.submanager.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.submanager.R;
import com.example.submanager.ui.activity.AuthActivity;
import com.example.submanager.ui.activity.PremiumActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    // ── Estado mock ───────────────────────────────────────────────────────────
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
        bindViews(view);
        setupListeners(view);
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

        // Respaldar (Premium lock) ────────────────────────────────────────────
        rowBackup.setOnClickListener(v ->
                showSnackbar(root, "Función exclusiva de Premium 👑"));

        // Restaurar (Premium lock) ────────────────────────────────────────────
        rowRestore.setOnClickListener(v ->
                showSnackbar(root, "Función exclusiva de Premium 👑"));

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
        } else if (password.length() < 6) {
            tilPassword.setError("Mínimo 6 caracteres");
            valid = false;
        }
        if (!valid) return;

        showSnackbar(root, "✅ ¡Bienvenido de vuelta!");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showSnackbar(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
