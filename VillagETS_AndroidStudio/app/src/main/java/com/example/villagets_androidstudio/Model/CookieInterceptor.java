package com.example.villagets_androidstudio.Model;

import android.content.Context;
import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CookieInterceptor implements Interceptor {
    private SessionManager sessionManager;

    public CookieInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        
        // Envoi du token stocké vers le serveur
        String token = sessionManager.getToken();
        if (token != null) {
            builder.addHeader("Cookie", token);
        }

        Response originalResponse = chain.proceed(builder.build());

        // Récupération et sauvegarde du token envoyé par le serveur (Set-Cookie)
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
}
