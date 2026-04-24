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
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int currentPage = 0;
    private boolean isLastPage = false;
    
    // On garde en mémoire les derniers paramètres de recherche pour la pagination
    private String lastSearchString = null;
    private List<String> lastTags = null;
    private boolean lastIsMarketplace = false;
    private Double lastMinPrice = null;
    private Double lastMaxPrice = null;
    private String lastSortMode = "DESC";

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void chargerPosts(boolean isMarketplace) {
        rechercherPosts(null, null, isMarketplace, null, null, 0, "DESC", false);
    }

    public void chargerPageSuivante() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || isLastPage) {
            return;
        }
        currentPage++;
        rechercherPosts(lastSearchString, lastTags, lastIsMarketplace, lastMinPrice, lastMaxPrice, currentPage, lastSortMode, true);
    }

    public void rechercherPosts(String searchString, List<String> tags, boolean isMarketplace) {
        rechercherPosts(searchString, tags, isMarketplace, null, null, 0, "DESC", false);
    }

    public void rechercherPosts(String searchString, List<String> tags, boolean isMarketplace, Double minPrice, Double maxPrice, int pageIndex, String sortMode, boolean isNextPage) {
        if (!isNextPage) {
            currentPage = 0;
            isLastPage = false;
            // On sauvegarde les paramètres pour les appels suivants (pagination)
            lastSearchString = searchString;
            lastTags = tags;
            lastIsMarketplace = isMarketplace;
            lastMinPrice = minPrice;
            lastMaxPrice = maxPrice;
            lastSortMode = sortMode;
        }

        isLoading.postValue(true);
        executorService.execute(() -> {
            try {
                List<Post> newPosts = PostDao.getFeed(searchString, tags, isMarketplace, minPrice, maxPrice, pageIndex, sortMode);
                
                if (newPosts == null || newPosts.isEmpty()) {
                    isLastPage = true;
                    if (!isNextPage) {
                        postsLiveData.postValue(new ArrayList<>());
                    }
                } else {
                    if (isNextPage) {
                        List<Post> currentPosts = postsLiveData.getValue();
                        List<Post> updatedList = new ArrayList<>();
                        if (currentPosts != null) {
                            updatedList.addAll(currentPosts);
                        }
                        updatedList.addAll(newPosts);
                        postsLiveData.postValue(updatedList);
                    } else {
                        postsLiveData.postValue(newPosts);
                    }
                }
            } catch (IOException e) {
                message.postValue("Erreur lors du chargement des posts : " + e.getMessage());
            } finally {
                isLoading.postValue(false);
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
