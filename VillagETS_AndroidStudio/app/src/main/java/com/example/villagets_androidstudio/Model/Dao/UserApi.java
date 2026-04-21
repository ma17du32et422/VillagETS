package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/users")
    Call<List<User>> getAllUsers();

    @POST("/users")
    Call<User> createUser(@Body User user);
    
    @POST("/auth/signup")
    Call<User> signup(@Body User user);

    @PUT("/users/{email}")
    Call<User> updateUser(@Path("email") String email, @Body User user);

    @DELETE("/users/{email}")
    Call<Void> deleteUser(@Path("email") String email);

    @GET("/users/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @POST("/auth/login")
    Call<User> login(@Body User user);

    @GET("/me")
    Call<User> getMe();
}
