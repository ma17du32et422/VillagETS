package com.example.villagets_androidstudio.View_Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Post;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public void chargerPosts() {
        executorService.execute(() -> {
            try {
                List<Post> posts = PostDao.getAllPosts();
                postsLiveData.postValue(posts);
            } catch (IOException | JSONException e) {
                message.postValue("Erreur lors du chargement des posts : " + e.getMessage());
            }
        });
    }

    public void creerPost(Post post) {
        executorService.execute(() -> {
            try {
                Post createdPost = PostDao.createPost(post);
                if (createdPost != null) {
                    saveSuccess.postValue(true);
                } else {
                    message.postValue("Erreur lors de la création du post");
                    saveSuccess.postValue(false);
                }
            } catch (IOException e) {
                message.postValue("Erreur réseau : " + e.getMessage());
                saveSuccess.postValue(false);
            }
        });
    }
}
