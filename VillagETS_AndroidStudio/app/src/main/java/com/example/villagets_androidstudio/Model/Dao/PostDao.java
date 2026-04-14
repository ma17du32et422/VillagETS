package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Post;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class PostDao {
    public static List<Post> getAllPosts() throws IOException, JSONException {
        try {
            HttpJsonService service = new HttpJsonService();
            // Appel à la route /feed via getFeed (searchString=null, tags=null, isMarketplace=false)
            List<Post> feed = service.getFeed(null, null, false);
            if (feed != null) {
                return feed;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new ArrayList<>();
    }

    public static Post createPost(Post post) throws IOException {
        PostApi postApi = RetrofitClient.getInstance().create(PostApi.class);
        Response<Post> response = postApi.createPost(post).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }
}
