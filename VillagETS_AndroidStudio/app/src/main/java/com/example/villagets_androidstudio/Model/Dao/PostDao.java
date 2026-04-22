package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Comment;
import com.example.villagets_androidstudio.Model.Post;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class PostDao {
    private static final PostApi api = RetrofitClient.getInstance().create(PostApi.class);

    public static List<Post> getAllPosts(boolean isMarketplace) throws IOException, JSONException {
        try {
            HttpJsonService service = new HttpJsonService();
            List<Post> feed = service.getFeed(null, null, isMarketplace);
            if (feed != null) {
                return feed;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new ArrayList<>();
    }

    public static List<Post> getUserPosts(String userId) throws IOException {
        Response<List<Post>> response = api.getUserPosts(userId).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return new ArrayList<>();
    }

    public static Post createPost(Post post) throws IOException {
        Response<Post> response = api.createPost(post).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public static PostApi.ReactionResponse toggleReaction(String postId, String type) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("type", type);
        Response<PostApi.ReactionResponse> response = api.toggleReaction(postId, body).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public static List<Comment> getPostComments(String postId) throws IOException {
        Response<List<Comment>> response = api.getPostComments(postId).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return new ArrayList<>();
    }

    public static List<Comment> getReplies(String commentId) throws IOException {
        Response<List<Comment>> response = api.getReplies(commentId).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return new ArrayList<>();
    }

    public static Comment createComment(String postId, String content, String parentCommentId) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("contenu", content);
        body.put("parentCommentaireId", parentCommentId);
        Response<Comment> response = api.createComment(postId, body).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public static boolean deleteComment(String commentId) throws IOException {
        Response<Map<String, Integer>> response = api.deleteComment(commentId).execute();
        return response.isSuccessful();
    }

    public static String uploadFile(File file, String nom, String type) throws IOException {
        RequestBody requestFile = RequestBody.create(MediaType.parse(type), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        
        RequestBody nomBody = RequestBody.create(MediaType.parse("text/plain"), nom);
        RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), type);

        Response<Map<String, String>> response = api.uploadFile(body, nomBody, typeBody).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().get("url");
        }
        return null;
    }
}
