package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Comment;
import com.example.villagets_androidstudio.Model.Post;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApi {
    @POST("/feed")
    Call<List<Post>> getFeed(@Body Map<String, Object> body);

    @POST("/post")
    Call<Post> createPost(@Body Post post);

    @PUT("/posts/{id}")
    Call<Post> updatePost(@Path("id") String id, @Body Post post);

    @DELETE("/post/{id}")
    Call<Void> deletePost(@Path("id") String id);

    @GET("/posts/{id}")
    Call<Post> getPostById(@Path("id") String id);

    @GET("/user/{id}/posts")
    Call<List<Post>> getUserPosts(@Path("id") String userId);

    @POST("/post/{id}/react")
    Call<ReactionResponse> toggleReaction(@Path("id") String id, @Body Map<String, String> body);

    @GET("/post/{id}/react")
    Call<ReactionStatus> getReactionStatus(@Path("id") String id);

    @GET("/comment/{commentId}/replies")
    Call<List<Comment>> getReplies(@Path("commentId") String commentId);

    @POST("/post/{publicationId}/comment")
    Call<Comment> createComment(@Path("publicationId") String publicationId, @Body Map<String, Object> body);

    @DELETE("/comment/{commentId}")
    Call<Map<String, Integer>> deleteComment(@Path("commentId") String commentId);

    @GET("/post/{id}/comments")
    Call<List<Comment>> getPostComments(@Path("id") String id);

    @Multipart
    @POST("/upload")
    Call<Map<String, String>> uploadFile(
            @Part MultipartBody.Part file,
            @Part("nom") RequestBody nom,
            @Part("type") RequestBody type
    );

    class ReactionResponse {
        public int likes;
        public int dislikes;
        public String userReaction;
    }

    class ReactionStatus {
        public String userReaction;
    }
}
