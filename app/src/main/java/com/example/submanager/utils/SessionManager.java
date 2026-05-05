package com.example.submanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME         = "SubManagerSession";
    private static final String KEY_LOGGED        = "isLoggedIn";
    private static final String KEY_EMAIL         = "email";
    private static final String KEY_NOMBRE        = "nombre";
    private static final String KEY_PREMIUM       = "isPremium";
    private static final String KEY_PREM_PLAN     = "premiumPlan";
    private static final String KEY_PREM_EXPIRY   = "premiumExpiry";
    // ── Nuevas claves para sincronización remota ──
    private static final String KEY_REMOTE_USER_ID = "remoteUserId";
    private static final String KEY_ULTIMA_SYNC    = "ultimaSincronizacion";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ─── Sesión básica ────────────────────────────────────────────────────────

    public void saveSession(String email, String nombre) {
        prefs.edit()
                .putBoolean(KEY_LOGGED, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NOMBRE, nombre)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED, false);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    // ─── Premium ──────────────────────────────────────────────────────────────

    public void savePremium(String plan, String expiry) {
        prefs.edit()
                .putBoolean(KEY_PREMIUM, true)
                .putString(KEY_PREM_PLAN, plan)
                .putString(KEY_PREM_EXPIRY, expiry)
                .apply();
    }

    public void clearPremium() {
        prefs.edit()
                .remove(KEY_PREMIUM)
                .remove(KEY_PREM_PLAN)
                .remove(KEY_PREM_EXPIRY)
                .apply();
    }

    public boolean isPremium() {
        return prefs.getBoolean(KEY_PREMIUM, false);
    }

    public String getPremiumPlan() {
        return prefs.getString(KEY_PREM_PLAN, "");
    }

    public String getPremiumExpiry() {
        return prefs.getString(KEY_PREM_EXPIRY, "");
    }

    // ─── Sincronización remota ────────────────────────────────────────────────

    /**
     * Guarda el ID del usuario en Supabase para vincularlo con sus datos remotos.
     * Se usa en la tabla configuracion_app (campo usuario_id).
     */
    public void saveRemoteUserId(long id) {
        prefs.edit().putLong(KEY_REMOTE_USER_ID, id).apply();
    }

    /**
     * @return ID remoto del usuario en Supabase, o -1 si no existe.
     */
    public long getRemoteUserId() {
        return prefs.getLong(KEY_REMOTE_USER_ID, -1L);
    }

    public boolean hasRemoteAccount() {
        return getRemoteUserId() != -1L;
    }

    /**
     * Guarda el timestamp de la última sincronización exitosa (para mostrar en UI).
     */
    public void saveUltimaSincronizacion(String timestamp) {
        prefs.edit().putString(KEY_ULTIMA_SYNC, timestamp).apply();
    }

    public String getUltimaSincronizacion() {
        return prefs.getString(KEY_ULTIMA_SYNC, null);
    }
}
