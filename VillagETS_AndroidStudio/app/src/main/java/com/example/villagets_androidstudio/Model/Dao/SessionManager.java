package com.example.villagets_androidstudio.Model.Dao;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "VillagETSSession";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Sauvegarde le token extrait du header Set-Cookie.
     * Attend une chaîne du type "token=xxxx.yyyy.zzzz; ..."
     */
    public void saveToken(String cookieString) {
        if (cookieString != null && cookieString.contains("token=")) {
            // On extrait la partie "token=..." jusqu'au premier ";"
            String[] parts = cookieString.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("token=")) {
                    editor.putString(KEY_TOKEN, part.trim());
                    editor.apply();
                    break;
                }
            }
        }
    }

    /**
     * Récupère le token pour l'injecter dans le header "Cookie" des requêtes.
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Vérifie si une session est active.
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /**
     * Supprime le token (déconnexion).
     */
    public void logout() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
