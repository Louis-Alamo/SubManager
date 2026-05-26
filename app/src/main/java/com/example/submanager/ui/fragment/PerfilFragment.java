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
import com.example.submanager.data.model.ConfiguracionAppModel;
import com.example.submanager.data.model.UsuarioModel;
import com.example.submanager.data.remote.SupabaseClient;
import com.example.submanager.data.remote.dto.UsuarioDto;
import com.example.submanager.data.repository.RemoteSyncRepository;
import com.example.submanager.ui.activity.AuthActivity;
import com.example.submanager.ui.activity.PremiumActivity;
import com.example.submanager.utils.CryptoUtils;
import com.example.submanager.utils.NetworkUtils;
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


    private MaterialSwitch switchNotificaciones;
    private TextView tvHoraRecordatorio;
    private LinearLayout rowTime;
    private LinearLayout rowBackup;
    private LinearLayout rowRestore;
    private LinearLayout rowTerms;
    private LinearLayout rowPrivacy;
    private LinearLayout rowSupport;
    private View btnHelp;


    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;


    private SessionManager sessionManager;
    private LinearLayout loginSection;
    private LinearLayout loggedInSection;
    private TextView tvUserName;
    private TextView tvUserEmail;


    private boolean notificacionesActivas = true;


    private RemoteSyncRepository remoteSyncRepository;
    private TextView tvUltimaSincronizacion;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(
        @NonNull View view,
        @Nullable Bundle savedInstanceState
    ) {
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



    private void bindViews(View root) {
        switchNotificaciones = root.findViewById(R.id.switchNotificaciones);
        tvHoraRecordatorio = root.findViewById(R.id.tvHoraRecordatorio);
        rowTime = root.findViewById(R.id.rowTime);
        rowBackup = root.findViewById(R.id.rowBackup);
        rowRestore = root.findViewById(R.id.rowRestore);
        rowTerms = root.findViewById(R.id.rowTerms);
        rowPrivacy = root.findViewById(R.id.rowPrivacy);
        rowSupport = root.findViewById(R.id.rowSupport);
        btnHelp = root.findViewById(R.id.btnHelp);

        tvUserName = root.findViewById(R.id.tvUserName);
        tvUserEmail = root.findViewById(R.id.tvUserEmail);
        loginSection = root.findViewById(R.id.loginSection);
        loggedInSection = root.findViewById(R.id.loggedInSection);


        tilEmail = root.findViewById(R.id.tilEmail);
        tilPassword = root.findViewById(R.id.tilPassword);
        etEmail = root.findViewById(R.id.etEmail);
        etPassword = root.findViewById(R.id.etPassword);


        switchNotificaciones.setChecked(notificacionesActivas);


        tvUltimaSincronizacion = root.findViewById(R.id.tvUltimaSincronizacion);
    }



    private void setupListeners(View root) {

        switchNotificaciones.setOnCheckedChangeListener((btn, checked) -> {
            notificacionesActivas = checked;
            String msg = checked ? "Alertas activadas" : "Alertas desactivadas";
            showSnackbar(root, msg);
        });


        rowTime.setOnClickListener(v ->
            showSnackbar(root, "Selector de hora próximamente")
        );


        rowBackup.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                iniciarBackup(root);
            } else {
                startActivity(
                    new Intent(requireContext(), PremiumActivity.class)
                );
            }
        });


        rowRestore.setOnClickListener(v -> {
            if (sessionManager.isPremium()) {
                confirmarRestore(root);
            } else {
                startActivity(
                    new Intent(requireContext(), PremiumActivity.class)
                );
            }
        });


        rowTerms.setOnClickListener(v ->
            showSnackbar(root, "Abriendo Términos de Servicio…")
        );


        rowPrivacy.setOnClickListener(v ->
            showSnackbar(root, "Abriendo Política de Privacidad…")
        );


        rowSupport.setOnClickListener(v ->
            showSnackbar(root, "Abriendo Centro de Soporte…")
        );


        if (btnHelp != null) {
            btnHelp.setOnClickListener(v ->
                showSnackbar(
                    root,
                    "¿Necesitas ayuda? Soporte disponible próximamente"
                )
            );
        }


        View btnLogin = root.findViewById(R.id.btnLogin);
        View tvRegister = root.findViewById(R.id.tvRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleInlineLogin(root));
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.submanager.MainActivity) {
                    ((com.example.submanager.MainActivity) getActivity()).launchAuthActivity(1);
                } else {
                    Intent intent = new Intent(requireContext(), AuthActivity.class);
                    intent.putExtra(AuthActivity.EXTRA_TAB, 1);
                    startActivity(intent);
                }
            });
        }


        View bannerPremium = root.findViewById(R.id.bannerPremium);
        if (bannerPremium != null) {
            bannerPremium.setOnClickListener(v ->
                startActivity(
                    new Intent(requireContext(), PremiumActivity.class)
                )
            );
        }

        View btnLogout = root.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.clearSession();


                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    db.suscripcionDao().deleteAllSuscripciones();
                    db.suscripcionDao().deleteAllServiciosFisicos();
                    db.suscripcionDao().deleteAllTerceros();
                    db.suscripcionDao().deleteAllRegistrosPago();


                    ConfiguracionAppModel config = db
                        .suscripcionDao()
                        .getConfiguracionSync();
                    if (config != null) {
                        config.setUltimaSincronizacion(null);
                        db.suscripcionDao().upsertConfiguracion(config);
                    }

                    handler.post(() -> {
                        if (!isAdded()) return;
                        actualizarUltimaSincronizacion();
                        updateAuthUI();
                        showSnackbar(root, "Sesión cerrada de forma segura");
                    });
                });
            });
        }
    }



    private void updateAuthUI() {
        if (!isAdded()) return;
        if (sessionManager.isLoggedIn()) {
            if (tvUserName != null) tvUserName.setText(
                sessionManager.getNombre()
            );
            if (tvUserEmail != null) tvUserEmail.setText(
                sessionManager.getEmail()
            );
            if (loginSection != null) loginSection.setVisibility(View.GONE);
            if (loggedInSection != null) loggedInSection.setVisibility(
                View.VISIBLE
            );
        } else {
            if (tvUserName != null) tvUserName.setText(
                getString(R.string.settings_account_guest)
            );
            if (tvUserEmail != null) tvUserEmail.setText(
                getString(R.string.settings_account_guest_subtitle)
            );
            if (loginSection != null) loginSection.setVisibility(View.VISIBLE);
            if (loggedInSection != null) loggedInSection.setVisibility(
                View.GONE
            );
        }


        View banner =
            getView() != null
                ? getView().findViewById(R.id.bannerPremium)
                : null;
        View bannerCard = banner != null ? (View) banner.getParent() : null;
        if (bannerCard != null) {
            bannerCard.setVisibility(
                sessionManager.isPremium() ? View.GONE : View.VISIBLE
            );
        }


        View badgeBackup = getView() != null ? getView().findViewById(R.id.tvBadgeBackup) : null;
        View badgeRestore = getView() != null ? getView().findViewById(R.id.tvBadgeRestore) : null;
        if (badgeBackup != null) badgeBackup.setVisibility(sessionManager.isPremium() ? View.GONE : View.VISIBLE);
        if (badgeRestore != null) badgeRestore.setVisibility(sessionManager.isPremium() ? View.GONE : View.VISIBLE);
    }



    private static final String TAG = "PerfilFragment";

    private void handleInlineLogin(View root) {
        if (tilEmail == null || tilPassword == null) return;
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email =
            etEmail != null && etEmail.getText() != null
                ? etEmail.getText().toString().trim().toLowerCase()
                : "";
        String password =
            etPassword != null && etPassword.getText() != null
                ? etPassword.getText().toString().trim()
                : "";

        boolean valid = true;
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Correo inválido");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            valid = false;
        } else if (password.length() < 8) {
            tilPassword.setError("La contraseña debe tener al menos 8 dígitos");
            valid = false;
        }
        if (!valid) return;

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showSnackbar(root, "Para iniciar sesión necesitas conexión a internet. Intenta más tarde.");
            return;
        }

        View btnLogin = root.findViewById(R.id.btnLogin);
        if (btnLogin != null) btnLogin.setEnabled(false);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Response<List<UsuarioDto>> resp = SupabaseClient.getApi()
                    .getUsuarioPorCorreo("eq." + email)
                    .execute();

                if (!resp.isSuccessful() || resp.body() == null) {
                    handler.post(() -> {
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        tilEmail.setError("No se pudo establecer conexión con el servidor. Intenta más tarde.");
                    });
                    return;
                }

                if (resp.body().isEmpty()) {
                    UsuarioModel usuarioLocal = AppDatabase.getInstance(requireContext())
                        .usuarioDao()
                        .findByEmail(email);
                    handler.post(() -> {
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        tilEmail.setError(
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
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        tilEmail.setError("No se pudo establecer conexión con el servidor. Intenta más tarde.");
                    });
                    return;
                }

                String hashIntentoNuevo = CryptoUtils.hashPassword(password, remoto.correo);
                String hashIntentoViejo = CryptoUtils.hashPassword(password, "remote");
                if (!hashIntentoNuevo.equals(remoto.hashContrasena) && !hashIntentoViejo.equals(remoto.hashContrasena)) {
                    handler.post(() -> {
                        if (!isAdded()) return;
                        if (btnLogin != null) btnLogin.setEnabled(true);
                        tilPassword.setError("Contraseña incorrecta");
                    });
                    return;
                }

                cacheRemoteUserIfNeeded(remoto);
                sessionManager.saveSession(remoto.correo, remoto.nombre != null ? remoto.nombre : "");
                sessionManager.saveRemoteUserId(remoto.id);
                applyRemotePremiumState(remoto);

                handler.post(() -> {
                    if (!isAdded()) return;
                    if (btnLogin != null) btnLogin.setEnabled(true);
                    String bienvenida = "¡Bienvenido, " + (remoto.nombre != null ? remoto.nombre : "") + "!";
                    if (sessionManager.isPremium()) {
                        iniciarRestoreConNavegacion(bienvenida + " Premium activo");
                    } else {
                        navigateToDashboard(bienvenida);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error login", e);
                handler.post(() -> {
                    if (!isAdded()) return;
                    if (btnLogin != null) btnLogin.setEnabled(true);
                    tilEmail.setError("No se pudo establecer conexión con el servidor. Intenta más tarde.");
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

    private void cacheRemoteUserIfNeeded(UsuarioDto remoto) {
        UsuarioModel existente = AppDatabase.getInstance(requireContext())
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
        AppDatabase.getInstance(requireContext())
            .usuarioDao()
            .insertUsuario(nuevoLocal);
    }

    private void applyRemotePremiumState(UsuarioDto remoto) {
        if (isPremiumPlanActive(remoto)) {
            sessionManager.savePremium(
                remoto.tipoPlan,
                remoto.fechaRenovacion != null ? remoto.fechaRenovacion : ""
            );
        } else {
            sessionManager.clearPremium();
        }
    }

    private boolean isPremiumPlanActive(UsuarioDto remoto) {
        return remoto.tipoPlan != null &&
            !"GRATIS".equalsIgnoreCase(remoto.tipoPlan) &&
            remoto.estaActivo != null &&
            remoto.estaActivo;
    }



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
                        java.time.ZonedDateTime.now(
                            java.time.ZoneId.systemDefault()
                        ).format(
                            java.time.format.DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy · HH:mm"
                            )
                        )
                    );
                    actualizarUltimaSincronizacion();
                    showSnackbar(root, message);
                    break;
                case NO_NETWORK:
                    showSnackbar(
                        root,
                        "Sin conexión a internet. Intenta más tarde."
                    );
                    break;
                case NOT_PREMIUM:
                    startActivity(
                        new Intent(requireContext(), PremiumActivity.class)
                    );
                    break;
                case ERROR:

                    new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error al sincronizar")
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
            .setMessage(
                "¿Deseas sobrescribir todos los datos locales con los datos de tu cuenta en la nube?\n\nEsta acción no se puede deshacer."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Restaurar", (dialog, which) ->
                iniciarRestore(root)
            )
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
                        java.time.ZonedDateTime.now(
                            java.time.ZoneId.systemDefault()
                        ).format(
                            java.time.format.DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy · HH:mm"
                            )
                        )
                    );
                    actualizarUltimaSincronizacion();
                    showSnackbar(root, message);
                    break;
                case NO_NETWORK:
                    showSnackbar(root, "Sin conexión. Intenta más tarde.");
                    break;
                case ERROR:
                    showSnackbar(root, message);
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



    private void showSnackbar(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }





    private void navigateToDashboard(String welcomeMessage) {
        if (getActivity() == null) return;
        com.google.android.material.bottomnavigation.BottomNavigationView nav =
            getActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_inicio);
        }

        View rootView = getActivity().findViewById(android.R.id.content);
        if (rootView != null && welcomeMessage != null) {
            rootView.post(() ->
                Snackbar.make(rootView, welcomeMessage, Snackbar.LENGTH_LONG).show()
            );
        }
    }





    @SuppressWarnings("deprecation")
    private void iniciarRestoreConNavegacion(String welcomeMessage) {
        if (!isAdded()) return;
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Restaurando tus datos…");
        progress.setCancelable(false);
        progress.show();

        remoteSyncRepository.pullAll((status, message) -> {
            if (!isAdded()) return;
            if (progress.isShowing()) progress.dismiss();
            switch (status) {
                case SUCCESS:
                    sessionManager.saveUltimaSincronizacion(
                        java.time.ZonedDateTime.now(
                            java.time.ZoneId.systemDefault()
                        ).format(
                            java.time.format.DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy · HH:mm"
                            )
                        )
                    );
                    navigateToDashboard(welcomeMessage);
                    break;
                case NO_NETWORK:

                    navigateToDashboard(welcomeMessage);
                    break;
                case ERROR:
                default:

                    navigateToDashboard(welcomeMessage);
                    break;
            }
        });
    }
}
