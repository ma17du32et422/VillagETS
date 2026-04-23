package com.example.villagets_androidstudio.View.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View.MainActivity;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText signUpEmail, signUpUsername, signUpName, signUpLastName, signUpPassword, signUpConfirmPassword;
    private Button btnSignUp;
    private TextView tvLoginLink;
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        signUpEmail = findViewById(R.id.signUpEmail);
        signUpUsername = findViewById(R.id.signUpUsername);
        signUpName = findViewById(R.id.signUpName);
        signUpLastName = findViewById(R.id.signUpLastName);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpConfirmPassword = findViewById(R.id.signUpConfirmPassword);

        btnSignUp = findViewById(R.id.signUpButton);
        tvLoginLink = findViewById(R.id.LoginLink);

        tvLoginLink.setOnClickListener(v -> finish());

        viewModel.getSignupSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Sign up successful !", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            String email = signUpEmail.getText().toString().trim();
            String password = signUpPassword.getText().toString().trim();
            String confirmPassword = signUpConfirmPassword.getText().toString().trim();
            String username = signUpUsername.getText().toString().trim();
            String firstName = signUpName.getText().toString().trim();
            String lastName = signUpLastName.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.length() < 3) {
                Toast.makeText(this, "Le pseudo doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setPseudo(username);
            user.setPrenom(firstName);
            user.setNom(lastName);
            user.setPhotoProfil(""); // Using empty string as per specs

            viewModel.signup(user);
        });
    }
}
