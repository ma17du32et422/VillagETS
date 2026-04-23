package com.example.villagets_androidstudio.View_Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Entity.Post;
import com.example.villagets_androidstudio.Utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    public void chargerPosts(boolean isMarketplace) {
        rechercherPosts(null, null, isMarketplace);
    }

    public void rechercherPosts(String searchString, List<String> tags, boolean isMarketplace) {
        executorService.execute(() -> {
            try {
                List<Post> posts = PostDao.getFeed(searchString, tags, isMarketplace);
                postsLiveData.postValue(posts);
            } catch (IOException e) {
                message.postValue("Erreur lors du chargement des posts : " + e.getMessage());
            }
        });
    }

    public void chargerUserPosts(String userId) {
        executorService.execute(() -> {
            try {
                List<Post> posts = PostDao.getUserPosts(userId);
                postsLiveData.postValue(posts);
            } catch (IOException e) {
                message.postValue("Erreur lors du chargement des posts : " + e.getMessage());
            }
        });
    }

    public void creerPost(android.content.Context context, Post post, List<android.net.Uri> imageUris) {
        executorService.execute(() -> {
            try {
                if (imageUris != null && !imageUris.isEmpty()) {
                    List<String> uploadedUrls = new ArrayList<>();
                    for (android.net.Uri imageUri : imageUris) {
                        File file = FileUtils.getFileFromUri(context, imageUri);
                        if (file == null) {
                            message.postValue("Erreur lors de la lecture d'une image");
                            saveSuccess.postValue(false);
                            return;
                        }

                        String mimeType = context.getContentResolver().getType(imageUri);
                        if (mimeType == null) {
                            mimeType = "image/jpeg";
                        }

                        String uploadedUrl = PostDao.uploadFile(file, file.getName(), mimeType);
                        if (uploadedUrl == null) {
                            message.postValue("Erreur lors de l'upload des images");
                            saveSuccess.postValue(false);
                            return;
                        }

                        uploadedUrls.add(uploadedUrl);
                    }

                    post.setMedia(uploadedUrls.toArray(new String[0]));
                }

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
