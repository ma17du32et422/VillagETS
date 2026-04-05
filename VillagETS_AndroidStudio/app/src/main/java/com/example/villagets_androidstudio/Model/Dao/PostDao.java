package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Post;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostDao {
    public static List<Post> getAllPosts() throws IOException, JSONException {
        // En attendant que l'API soit prête, on retourne des données de test
        List<Post> testPosts = new ArrayList<>();
        testPosts.add(new Post(1, 101, "Test depuis le Model", 
                "Ceci est un post généré dans le PostDao pour tester l'architecture MVVM.", 
                new String[]{"https://picsum.photos/500"}, "2023-10-27", null));
        testPosts.add(new Post(2, 102, "Architecture MVVM validée", 
                "Les données transitent maintenant du Model vers le ViewModel, puis vers la View.", 
                new String[]{"https://picsum.photos/600"}, "2023-10-27", null));
        
        return testPosts;
        
        /* 
        Code final pour l'API :
        HttpJsonService service = new HttpJsonService();
        return service.getAllPosts();
        */
    }
}
