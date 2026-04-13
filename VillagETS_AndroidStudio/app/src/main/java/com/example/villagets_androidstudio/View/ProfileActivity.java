package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagets_androidstudio.R;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    private boolean isUsernameEditing = false;
    private boolean isPasswordChanging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Views
        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageButton btnEditUsername = findViewById(R.id.btnEditUsername);
        TextInputLayout inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        EditText etUsername = findViewById(R.id.etUsername);

        Button btnShowPasswordChange = findViewById(R.id.btnShowPasswordChange);
        LinearLayout passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
        
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageButton btnEditImage = findViewById(R.id.btnEditImage);

        // Edit Username Logic
        btnEditUsername.setOnClickListener(v -> {
            isUsernameEditing = !isUsernameEditing;
            inputLayoutUsername.setVisibility(isUsernameEditing ? View.VISIBLE : View.GONE);
            if (isUsernameEditing) {
                etUsername.setText(tvUsername.getText());
                etUsername.requestFocus();
            }
        });

        // Show Password Change Logic
        btnShowPasswordChange.setOnClickListener(v -> {
            isPasswordChanging = !isPasswordChanging;
            passwordChangeLayout.setVisibility(isPasswordChanging ? View.VISIBLE : View.GONE);
            btnShowPasswordChange.setText(isPasswordChanging ? "Annuler le changement" : "Changer le mot de passe");
        });

        // Edit Image logic (Mock)
        btnEditImage.setOnClickListener(v -> {
            Toast.makeText(this, "Sélection de l'image (Simulation)", Toast.LENGTH_SHORT).show();
        });

        // Save Profile Logic
        btnSaveProfile.setOnClickListener(v -> {
            if (isUsernameEditing) {
                String newName = etUsername.getText().toString();
                if (!newName.isEmpty()) {
                    tvUsername.setText(newName);
                    inputLayoutUsername.setVisibility(View.GONE);
                    isUsernameEditing = false;
                }
            }
            
            Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
            finish(); // Retour à l'écran précédent
        });
    }
}
