package com.example.villagets_androidstudio.Model.Dao;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
public class RetrofitClient {

    private static final String URL_POINT_ENTREE = "http://10.0.2.2:3000";//placeholder
    private static Retrofit instance = null;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(URL_POINT_ENTREE)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}
