package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText loginInputUser, loginInputPassword;
    private Button loginButton;
    private TextView signupLink;
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        loginInputUser = findViewById(R.id.loginInputUser);
        loginInputPassword = findViewById(R.id.loginInputPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signUpLink);

        // Observer le succès de la connexion
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                // Fermer l'activité de login pour revenir au MainActivity
                finish();
            }
        });

        // Observer les erreurs
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        loginButton.setOnClickListener(v -> {
            String email = loginInputUser.getText().toString();
            String password = loginInputPassword.getText().toString();
            viewModel.login(email, password);
        });

        signupLink.setOnClickListener(v -> {
            // Naviguer vers l'activité de signup
            startActivity(new Intent(this, SignupActivity.class));

        });
    }
}
