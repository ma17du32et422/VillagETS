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
    private final UserDao userDao = new UserDao();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
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
}
