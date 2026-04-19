package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Post;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PostApi {
    // Tentative en POST sur la racine
    @POST("/feed")
    Call<List<Post>> getFeed(@Body Map<String, Object> body);

    @POST("/post")
    Call<Post> createPost(@Body Post post);

    @PUT("/posts/{id}")
    Call<Post> updatePost(@Path("id") String id, @Body Post post);

    @DELETE("/posts/{id}")
    Call<Void> deletePost(@Path("id") String id);

    @GET("/posts/{id}")
    Call<Post> getPostById(@Path("id") String id);
}
