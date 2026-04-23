package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.Entity.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class UserDao {
    private final UserApi api;

    public UserDao() {
        api = RetrofitClient.getInstance().create(UserApi.class);
    }

    public List<User> getAllUsers() throws IOException {
        Response<List<User>> response = api.getAllUsers().execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public User getUserByEmail(String email) throws IOException {
        Response<User> response = api.getUserByEmail(email).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public User getUserById(String userId) throws IOException {
        Response<User> response = api.getUserById(userId).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public User login(User user) throws IOException {
        Response<User> response = api.login(user).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public User signup(User user) throws IOException {
        Response<User> response = api.signup(user).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public User getMe() throws IOException {
        Response<User> response = api.getMe().execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    public boolean updatePseudo(String pseudo) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("pseudo", pseudo);
        Response<Void> response = api.updatePseudo(body).execute();
        return response.isSuccessful();
    }

    public boolean updatePassword(String currentPassword, String newPassword) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);
        Response<Void> response = api.updatePassword(body).execute();
        return response.isSuccessful();
    }

    public boolean updatePhoto(String photoUrl) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("photoUrl", photoUrl);
        Response<Void> response = api.updatePhoto(body).execute();
        return response.isSuccessful();
    }

    public List<User> searchUsers(String query) throws IOException {
        Response<List<User>> response = api.searchUsers(query).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }
}
