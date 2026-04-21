package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
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

    @PATCH("/user/pseudo")
    Call<Void> updatePseudo(@Body Map<String, String> body);

    @PATCH("/user/password")
    Call<Void> updatePassword(@Body Map<String, String> body);

    @PATCH("/user/photo")
    Call<Void> updatePhoto(@Body Map<String, String> body);

    @DELETE("/users/{email}")
    Call<Void> deleteUser(@Path("email") String email);

    @GET("/users/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @GET("/user/{id}")
    Call<User> getUserById(@Path("id") String id);

    @POST("/auth/login")
    Call<User> login(@Body User user);

    @GET("/me")
    Call<User> getMe();
}
