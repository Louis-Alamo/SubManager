package com.example.submanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME   = "SubManagerSession";
    private static final String KEY_LOGGED  = "isLoggedIn";
    private static final String KEY_EMAIL   = "email";
    private static final String KEY_NOMBRE  = "nombre";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

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
}
