package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.SessionManager;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    private boolean isUsernameEditing = false;
    private boolean isPasswordChanging = false;
    private UserViewModel viewModel;
    private User cachedUser;
    private boolean requestedPublicProfilePhoto;
    private ShapeableImageView profileImage;
    private TextView tvUsername;
    private EditText etUsername, etNewPassword, etConfirmPassword, etCurrentPassword;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    profileImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        tvUsername = findViewById(R.id.tvUsername);
        profileImage = findViewById(R.id.profileImage);
        etUsername = findViewById(R.id.etUsername);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        ImageButton btnEditUsername = findViewById(R.id.btnEditUsername);
        TextInputLayout inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        Button btnShowPasswordChange = findViewById(R.id.btnShowPasswordChange);
        LinearLayout passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageButton btnEditImage = findViewById(R.id.btnEditImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnLogout = findViewById(R.id.btnLogout);

        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                tvUsername.setText(user.getPseudo());
                String photoToDisplay = hasPhoto(user.getPhotoProfil())
                        ? user.getPhotoProfil()
                        : (cachedUser != null ? cachedUser.getPhotoProfil() : null);
                loadProfileImage(photoToDisplay);

                if (!hasPhoto(user.getPhotoProfil()) && cachedUser != null && hasPhoto(cachedUser.getPhotoProfil())) {
                    user.setPhotoProfil(cachedUser.getPhotoProfil());
                }

                cachedUser = user;
                user.saveUser(getApplicationContext());

                if (!hasPhoto(user.getPhotoProfil())
                        && user.getUserId() != null
                        && !user.getUserId().trim().isEmpty()
                        && !requestedPublicProfilePhoto) {
                    requestedPublicProfilePhoto = true;
                    viewModel.fetchUserById(user.getUserId());
                    return;
                }

                requestedPublicProfilePhoto = false;
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        cachedUser = User.loadUser(getApplicationContext());
        requestedPublicProfilePhoto = false;
        if (cachedUser != null) {
            if (cachedUser.getPseudo() != null && !cachedUser.getPseudo().trim().isEmpty()) {
                tvUsername.setText(cachedUser.getPseudo());
            }
            loadProfileImage(cachedUser.getPhotoProfil());
        }

        viewModel.fetchUser();

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditUsername.setOnClickListener(v -> {
            isUsernameEditing = !isUsernameEditing;
            inputLayoutUsername.setVisibility(isUsernameEditing ? View.VISIBLE : View.GONE);
            if (isUsernameEditing) {
                etUsername.setText(tvUsername.getText());
            }
        });

        btnShowPasswordChange.setOnClickListener(v -> {
            isPasswordChanging = !isPasswordChanging;
            passwordChangeLayout.setVisibility(isPasswordChanging ? View.VISIBLE : View.GONE);
            btnShowPasswordChange.setText(isPasswordChanging ? "Cancel" : "Change password");
        });

        btnEditImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnSaveProfile.setOnClickListener(v -> {
            if (isUsernameEditing) {
                String newPseudo = etUsername.getText().toString().trim();
                if (!newPseudo.isEmpty()) {
                    viewModel.updatePseudo(newPseudo);
                }
            }

            if (isPasswordChanging) {
                String currentPass = etCurrentPassword.getText().toString().trim();
                String newPass = etNewPassword.getText().toString().trim();
                String confirm = etConfirmPassword.getText().toString().trim();
                
                if (currentPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Please fill in all password fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!newPass.equals(confirm)) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                viewModel.updatePassword(currentPass, newPass);
            }

            if (selectedImageUri != null) {
                viewModel.updatePhoto(this, selectedImageUri);
            }
        });
    }

    private void loadProfileImage(String photoProfil) {
        if (photoProfil == null || photoProfil.trim().isEmpty()) {
            profileImage.setImageDrawable(null);
            return;
        }

        String photoUrl = photoProfil.replace("localhost", "apivillagets.lesageserveur.com");
        Glide.with(this)
                .load(photoUrl)
                .into(profileImage);
    }

    private boolean hasPhoto(String photoProfil) {
        return photoProfil != null && !photoProfil.trim().isEmpty();
    }
}
