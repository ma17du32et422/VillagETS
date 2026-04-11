package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.Dao.UserApi;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etUsername, etFirstName, etLastName, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLoginLink;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnSignUp.setOnClickListener(v -> {
            handleSignUp();
        });

        tvLoginLink.setOnClickListener(v -> {
            finish(); // Go back to login if it exists, or just close for now
        });
    }

    private void handleSignUp() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || 
            TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || 
            TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password, email, firstName, lastName, "2000-01-01");

        executorService.execute(() -> {
            try {
                UserApi api = RetrofitClient.getInstance().create(UserApi.class);
                Response<User> response = api.signup(newUser).execute();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Sign up failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.e("SignupActivity", "Network error", e);
                runOnUiThread(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
