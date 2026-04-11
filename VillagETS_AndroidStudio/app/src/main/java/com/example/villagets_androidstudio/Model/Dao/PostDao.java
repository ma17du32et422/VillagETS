package com.example.villagets_androidstudio.Model.Dao;
import com.example.villagets_androidstudio.Model.Post;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostDao {
    public static List<Post> getAllPosts() throws IOException, JSONException {
        List<Post> testPosts = new ArrayList<>();
        
        testPosts.add(new Post("1", 101, "Test depuis le Model",
                "Ceci est un post généré dans le PostDao pour tester l'architecture MVVM.", 
                new String[]{"https://picsum.photos/500"}, "2023-10-27", null));
                
        testPosts.add(new Post("2", 102, "Architecture MVVM validée",
                "Les données transitent maintenant du Model vers le ViewModel, puis vers la View.", 
                new String[]{"https://apivillagets.lesageserveur.com/uploads/5d8d6b76-8558-4ba5-a09c-7eb983fe0a3c.jpg"}, "2023-10-27", null));

        try {
            HttpJsonService service = new HttpJsonService();
            Post apiPost = service.getPostById("f6cd44a4-b45d-4b2f-8fa8-1a37af614fa2");
            if (apiPost != null) {
                testPosts.add(apiPost);
            }
        } catch (Exception e) {
            // L'API peut être hors ligne, on ignore l'erreur pour garder les posts de test
        }
        
        return testPosts;
    }
}
