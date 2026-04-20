package com.example.villagets_androidstudio.View_Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.Dao.UserDao;
import com.example.villagets_androidstudio.Model.User;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final UserDao userDao = new UserDao();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<User> getUserLiveData() {
        return userLiveData;
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
                    errorMessage.postValue("Échec de la connexion");
                }
            } catch (IOException e) {
                // If there's an error like unrecognized field, it won't be suppressed here yet
                // but by adding @JsonIgnoreProperties in the User model, this exception should be avoided entirely.
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

    public void fetchUser(String email) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByEmail(email);
                userLiveData.postValue(user);
            } catch (IOException e) {
                errorMessage.postValue("Erreur lors de la récupération : " + e.getMessage());
            }
        });
    }

    public void fetchCurrentUser() {
        executorService.execute(() -> {
            try {
                User user = userDao.getMe();
                if (user != null) {
                    userLiveData.postValue(user);
                } else {
                    errorMessage.postValue("Utilisateur non trouvé");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }
}
