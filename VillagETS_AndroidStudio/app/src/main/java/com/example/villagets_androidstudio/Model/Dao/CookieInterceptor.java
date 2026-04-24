package com.example.villagets_androidstudio.Model.Dao;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View.Profile.LoginActivity;

import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CookieInterceptor implements Interceptor {
    private SessionManager sessionManager;
    private Context context;

    public CookieInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        
        String url = request.url().toString();
        
        if (!url.contains("/auth/login") && !url.contains("/auth/signup")) {
            String token = sessionManager.getToken();
            if (token != null) {
                builder.addHeader("Cookie", token);
            }
        }

        Response originalResponse = chain.proceed(builder.build());

        // Si le serveur retourne 410 (Gone), on déconnecte l'utilisateur
        if (originalResponse.code() == 410) {
            handleLogout();
        }

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            List<String> cookies = originalResponse.headers("Set-Cookie");
            for (String cookie : cookies) {
                if (cookie.contains("token=")) {
                    sessionManager.saveToken(cookie);
                    break;
                }
            }
        }

        return originalResponse;
    }

    private void handleLogout() {
        // 1. Afficher le message sur le thread principal avec traduction
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, context.getString(R.string.error_user_gone), Toast.LENGTH_LONG).show()
        );

        // 2. Nettoyer la session
        sessionManager.logout();

        // 3. Rediriger vers l'écran de connexion
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
