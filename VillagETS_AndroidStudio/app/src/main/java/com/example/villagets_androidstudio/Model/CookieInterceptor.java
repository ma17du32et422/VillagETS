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
