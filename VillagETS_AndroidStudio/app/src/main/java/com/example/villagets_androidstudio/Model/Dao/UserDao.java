package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.User;

import java.io.IOException;
import java.util.List;

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
}
