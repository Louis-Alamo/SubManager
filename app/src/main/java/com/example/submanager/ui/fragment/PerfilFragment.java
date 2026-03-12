package com.example.submanager.ui.fragment;

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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class PerfilFragment extends Fragment {

    // ── UI references ─────────────────────────────────────────────────────────
    private MaterialSwitch switchNotificaciones;
    private TextView tvHoraRecordatorio;
    private LinearLayout rowTone;
    private LinearLayout rowTime;
    private LinearLayout rowBackup;
    private LinearLayout rowRestore;
    private LinearLayout rowTerms;
    private LinearLayout rowPrivacy;
    private LinearLayout rowSupport;
    private View btnHelp;

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
        rowTone              = root.findViewById(R.id.rowTone);
        rowTime              = root.findViewById(R.id.rowTime);
        rowBackup            = root.findViewById(R.id.rowBackup);
        rowRestore           = root.findViewById(R.id.rowRestore);
        rowTerms             = root.findViewById(R.id.rowTerms);
        rowPrivacy           = root.findViewById(R.id.rowPrivacy);
        rowSupport           = root.findViewById(R.id.rowSupport);
        btnHelp              = root.findViewById(R.id.btnHelp);

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

        // Tono de alerta ─────────────────────────────────────────────────────
        rowTone.setOnClickListener(v ->
                showSnackbar(root, "Selector de tono próximamente"));

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

        // Login / Register ────────────────────────────────────────────────────
        View btnLogin   = root.findViewById(R.id.btnLogin);
        View tvRegister = root.findViewById(R.id.tvRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v ->
                    showSnackbar(root, "Inicio de sesión próximamente"));
        }
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v ->
                    showSnackbar(root, "Registro próximamente"));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showSnackbar(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
