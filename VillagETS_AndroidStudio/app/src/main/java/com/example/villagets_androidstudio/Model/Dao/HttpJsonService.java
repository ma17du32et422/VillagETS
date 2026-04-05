package com.example.villagets_androidstudio.Model.Dao;
import com.example.villagets_androidstudio.Model.Dao.PostApi;
import com.example.villagets_androidstudio.Model.Dao.PostApi;
import com.example.villagets_androidstudio.Model.Post;

import java.io.IOException;

import java.util.List;

import retrofit2.Response;
public class HttpJsonService {
    private final PostApi api;
    public HttpJsonService() {
        api = RetrofitClient.getInstance().create(PostApi.class);
    }
public List<Post> getAllPosts() throws IOException {
    Response<List<Post>> response = api.getAllPosts().execute();
    if (response.isSuccessful() && response.body() != null) {
        return response.body();
    }
    return null;
}

}

