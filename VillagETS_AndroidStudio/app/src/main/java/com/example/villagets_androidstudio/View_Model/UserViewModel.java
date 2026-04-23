package com.example.villagets_androidstudio.View_Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.Dao.UserDao;
import com.example.villagets_androidstudio.Model.Entity.User;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final UserDao userDao = new UserDao();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<List<User>> getSearchResultsLiveData() {
        return searchResultsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void login(String email, String password) {
        executorService.execute(() -> {
            try {
                User user = new User();
                user.setEmail(email);
                user.setPassword(password);
                User loggedInUser = userDao.login(user);
                if (loggedInUser != null) {
                    userLiveData.postValue(loggedInUser);
                } else {
                    errorMessage.postValue("Échec de la connexion (401)");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void signup(User user) {
        executorService.execute(() -> {
            try {
                User newUser = userDao.signup(user);
                if (newUser != null) {
                    signupSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Échec de l'inscription");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void fetchUser() {
        executorService.execute(() -> {
            try {
                User user = userDao.getMe();
                if (user != null) {
                    userLiveData.postValue(user);
                } else {
                    errorMessage.postValue("401: Session expirée ou invalide");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur de récupération : " + e.getMessage());
            }
        });
    }

    public void fetchUser(String email) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByEmail(email);
                if (user != null) {
                    userLiveData.postValue(user);
                } else {
                    errorMessage.postValue("Utilisateur non trouvé");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur de récupération : " + e.getMessage());
            }
        });
    }

    public void fetchUserById(String userId) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(userId);
                if (user != null) {
                    userLiveData.postValue(user);
                } else {
                    errorMessage.postValue("Utilisateur non trouvé");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur de récupération : " + e.getMessage());
            }
        });
    }

    public void updatePseudo(String pseudo) {
        executorService.execute(() -> {
            try {
                boolean success = userDao.updatePseudo(pseudo);
                updateSuccess.postValue(success);
                if (!success) errorMessage.postValue("Erreur lors de la mise à jour du pseudo");
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void updatePassword(String currentPassword, String newPassword) {
        executorService.execute(() -> {
            try {
                boolean success = userDao.updatePassword(currentPassword, newPassword);
                updateSuccess.postValue(success);
                if (!success) errorMessage.postValue("Erreur lors de la mise à jour du mot de passe");
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void updatePhoto(android.content.Context context, android.net.Uri uri) {
        executorService.execute(() -> {
            try {
                java.io.File file = com.example.villagets_androidstudio.Utils.FileUtils.getFileFromUri(context, uri);
                if (file == null) {
                    errorMessage.postValue("Impossible de lire le fichier");
                    return;
                }

                String mimeType = context.getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "image/jpeg";

                String uploadedUrl = com.example.villagets_androidstudio.Model.Dao.PostDao.uploadFile(file, file.getName(), mimeType);
                if (uploadedUrl == null) {
                    errorMessage.postValue("Erreur lors de l'upload de l'image");
                    return;
                }

                boolean success = userDao.updatePhoto(uploadedUrl);
                updateSuccess.postValue(success);
                if (!success) {
                    errorMessage.postValue("Erreur lors de la mise à jour de la photo dans le profil");
                } else {
                    fetchUser();
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void updatePhoto(String photoUrl) {
        executorService.execute(() -> {
            try {
                boolean success = userDao.updatePhoto(photoUrl);
                updateSuccess.postValue(success);
                if (!success) errorMessage.postValue("Erreur lors de la mise à jour de la photo");
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void searchUsers(String query) {
        executorService.execute(() -> {
            try {
                List<User> results = userDao.searchUsers(query);
                searchResultsLiveData.postValue(results);
            } catch (IOException e) {
                errorMessage.postValue("Erreur de recherche : " + e.getMessage());
            }
        });
    }
}
