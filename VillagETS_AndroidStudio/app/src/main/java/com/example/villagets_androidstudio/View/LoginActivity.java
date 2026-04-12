package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText loginInputUser, loginInputPassword;
    private Button loginButton;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginInputUser = findViewById(R.id.loginInputUser);
        loginInputPassword = findViewById(R.id.loginInputPassword);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signUpLink);

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        loginButton.setOnClickListener(v -> {
            UserViewModel viewModel = new UserViewModel();
            String email = loginInputUser.getText().toString();
            String password = loginInputPassword.getText().toString();
            viewModel.login(email, password);


        });
    }
}
