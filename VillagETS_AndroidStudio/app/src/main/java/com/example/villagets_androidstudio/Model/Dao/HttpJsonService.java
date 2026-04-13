package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.Model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class HttpJsonService {
    private final PostApi postApi;
    private final UserApi userApi;

    public HttpJsonService() {
        postApi = RetrofitClient.getInstance().create(PostApi.class);
        userApi = RetrofitClient.getInstance().create(UserApi.class);
    }

    // --- Post Methods ---
    public List<Post> getFeed(String searchString, String[] tags, boolean isMarketplace) throws IOException {
        // Préparation du corps de la requête POST pour /feed
        Map<String, Object> body = new HashMap<>();
        body.put("searchString", searchString);
        body.put("tags", tags);
        body.put("isMarketplace", isMarketplace);

        Response<List<Post>> response = postApi.getFeed(body).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }

    public Post getPostById(String id) throws IOException {
        Response<Post> response = postApi.getPostById(id).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }

    // --- User Methods ---
    public List<User> getAllUsers() throws IOException {
        Response<List<User>> response = userApi.getAllUsers().execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }

    public User login(User user) throws IOException {
        Response<User> response = userApi.login(user).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }

    public User signup(User user) throws IOException {
        Response<User> response = userApi.signup(user).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }

    public User getUserByEmail(String email) throws IOException {
        Response<User> response = userApi.getUserByEmail(email).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        return null;
    }
}
