package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagets_androidstudio.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText signUpEmail, signUpUsername, signUpName, signUpLastName, signUpPassword, signUpConfirmPassword;
    private Button btnSignUp;
    private TextView tvLoginLink;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUpEmail = findViewById(R.id.signUpEmail);
        signUpUsername = findViewById(R.id.signUpUsername);
        signUpName = findViewById(R.id.signUpName);
        signUpLastName = findViewById(R.id.signUpLastName);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpConfirmPassword= findViewById(R.id.signUpConfirmPassword);

        btnSignUp = findViewById(R.id.signUpButton);
        tvLoginLink = findViewById(R.id.LoginLink);


    }
}
