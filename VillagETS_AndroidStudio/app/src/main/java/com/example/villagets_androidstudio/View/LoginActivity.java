package com.example.villagets_androidstudio.View;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etIdentifier, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(identifier) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        User loginUser = new User();
        // Determine if it's an email or username (pseudo)
        if (identifier.contains("@")) {
            loginUser.setEmail(identifier);
        } else {
            loginUser.setPseudo(identifier);
        }
        loginUser.setPassword(password);

        executorService.execute(() -> {
            try {
                UserApi api = RetrofitClient.getInstance().create(UserApi.class);
                Response<User> response = api.login(loginUser).execute();

                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        user.saveUser(this); // Save user info locally
                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Échec de la connexion. Vérifiez vos identifiants.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.e("LoginActivity", "Network error", e);
                runOnUiThread(() -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
