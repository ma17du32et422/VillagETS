package com.example.villagets_androidstudio.Model.Dao;

import android.content.Context;
import com.example.villagets_androidstudio.Model.CookieInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String URL_POINT_ENTREE = "https://apivillagets.lesageserveur.com/";
    private static Retrofit instance = null;

    public static Retrofit getInstance(Context context) {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(new CookieInterceptor(context.getApplicationContext()))
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(URL_POINT_ENTREE)
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static Retrofit getInstance() {
        return instance;
    }
}
