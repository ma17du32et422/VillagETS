package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
public interface PostApi {
    @GET("/posts")
    Call<List<Post>> getAllPosts();
    @POST("/posts")
    Call<Post> createPost(@Body Post post);
    @PUT("/posts/{id}")
    Call<Post> updatePost(@Path("id") int id, @Body Post post);
    @DELETE("/posts/{id}")
    Call<Void> deletePost(@Path("id") int id);
    @GET("/posts/{id}")
    Call<Post> getPostById(@Path("id") int id);



}
