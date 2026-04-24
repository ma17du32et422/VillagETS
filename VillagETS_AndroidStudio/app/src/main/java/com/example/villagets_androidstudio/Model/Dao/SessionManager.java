package com.example.villagets_androidstudio.Model.Dao;

import android.content.Context;
import android.content.SharedPreferences;
import com.bumptech.glide.Glide;
import java.io.File;

public class SessionManager {
    private static final String PREF_NAME = "VillagETSSession";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
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
     * Supprime toutes les données de session et vide le cache (déconnexion complète).
     */
    public void logout() {
        // 1. Effacer toutes les SharedPreferences connues
        editor.clear().commit();
        
        SharedPreferences userPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userPrefs.edit().clear().commit();

        // 2. Vider le cache de l'application (fichiers temporaires)
        clearCache();

        // 3. Vider le cache Glide (images)
        try {
            Glide.get(context).clearMemory();
            new Thread(() -> Glide.get(context).clearDiskCache()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Note: L'activité appelante doit se charger de rediriger vers le Login
        // et de nettoyer la pile d'activités.
    }

    private void clearCache() {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
            
            File internalFiles = context.getFilesDir();
            deleteDir(internalFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
