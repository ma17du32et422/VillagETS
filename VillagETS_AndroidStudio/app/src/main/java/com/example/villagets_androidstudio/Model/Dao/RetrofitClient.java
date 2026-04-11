package com.example.villagets_androidstudio.Model.Dao;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String URL_POINT_ENTREE = "https://apivillagets.lesageserveur.com/";
    private static Retrofit instance = null;

    public static Retrofit getInstance() {
        if (instance == null) {
            // Configuration d'un client OkHttp plus robuste pour le développement local
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true) // Réessayer en cas de coupure
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(URL_POINT_ENTREE)
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}
