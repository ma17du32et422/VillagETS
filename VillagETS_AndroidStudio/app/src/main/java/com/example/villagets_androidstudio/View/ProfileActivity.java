package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView profileImage;
    private TextView tvPseudo, tvPostCount, tvFullName, tvUserId;
    private RecyclerView rvPosts;
    private ImageButton btnSettings;
    private PostAdapter postAdapter;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = getIntent().getStringExtra("userId");
        User me = User.loadUser(this);
        boolean isMe = false;
        
        if (userId == null) {
            if (me != null) {
                userId = me.getUserId();
                isMe = true;
            }
        } else if (me != null && userId.equals(me.getUserId())) {
            isMe = true;
        }

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews(isMe);
        setupRecyclerView();
        setupViewModels();

        userViewModel.fetchUserById(userId);
        postViewModel.chargerUserPosts(userId);
    }

    private void initViews(boolean isMe) {
        profileImage = findViewById(R.id.profileImage);
        tvPseudo = findViewById(R.id.tvPseudo);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvFullName = findViewById(R.id.tvFullName);
        tvUserId = findViewById(R.id.tvUserId);
        rvPosts = findViewById(R.id.rvPosts);
        btnSettings = findViewById(R.id.btnSettings);

        if (isMe) {
            btnSettings.setVisibility(View.VISIBLE);
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ProfileSettingActivity.class);
                startActivity(intent);
            });
        } else {
            btnSettings.setVisibility(View.GONE);
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter();
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);
    }

    private void setupViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                tvPseudo.setText(user.getPseudo());
                tvFullName.setText(user.getPrenom() + " " + user.getNom());
                tvUserId.setText("User ID: " + user.getUserId());
                
                if (user.getPhotoProfil() != null) {
                    String photoUrl = user.getPhotoProfil().replace("localhost", "10.0.2.2");
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.profile_placeholder).into(profileImage);
                }
            }
        });

        postViewModel.getPostsLiveData().observe(this, posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
                tvPostCount.setText(String.valueOf(posts.size()));
            }
        });

        userViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
