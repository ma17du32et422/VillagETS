package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.SessionManager;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.villagets_androidstudio.View_Model.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<ImageButton> navButtons;
    private ViewPager2 viewPager;
    private ShapeableImageView profileBtn;
    private UserViewModel userViewModel;
    private User cachedUser;
    private boolean requestedPublicProfilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0. Vérification de la session
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialisation du client Retrofit
        RetrofitClient.getInstance(getApplicationContext());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // 1. Initialisation des vues
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        ImageButton addPostBtn = findViewById(R.id.addPostBtn);
        profileBtn = findViewById(R.id.profileBtn);
        View toolbar = findViewById(R.id.toolbar);
        
        viewPager = findViewById(R.id.view_pager);
        
        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.homeBtn));
        navButtons.add(findViewById(R.id.moneyBtn));
        navButtons.add(findViewById(R.id.searchBtn));
        navButtons.add(findViewById(R.id.messageBtn));
        navButtons.add(findViewById(R.id.notificationBtn));

        cachedUser = User.loadUser(getApplicationContext());
        if (cachedUser != null) {
            loadProfileImage(cachedUser.getPhotoProfil());
        }

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) {
                return;
            }

            if ((user.getPhotoProfil() == null || user.getPhotoProfil().trim().isEmpty())
                    && cachedUser != null
                    && cachedUser.getPhotoProfil() != null
                    && !cachedUser.getPhotoProfil().trim().isEmpty()) {
                user.setPhotoProfil(cachedUser.getPhotoProfil());
            }

            cachedUser = user;
            loadProfileImage(user.getPhotoProfil());
            user.saveUser(getApplicationContext());

            if (!hasPhoto(user.getPhotoProfil())
                    && user.getUserId() != null
                    && !user.getUserId().trim().isEmpty()
                    && !requestedPublicProfilePhoto) {
                requestedPublicProfilePhoto = true;
                userViewModel.fetchUserById(user.getUserId());
                return;
            }

            requestedPublicProfilePhoto = false;
        });

        // 2. Configuration du ViewPager2
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Listener pour mettre à jour la navigation lors du swipe
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBottomNavSelection(position);
            }
        });

        // 3. Logique du Menu Drawer
        menuBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // 4. Logique de navigation via les boutons du bas
        for (int i = 0; i < navButtons.size(); i++) {
            final int index = i;
            navButtons.get(i).setOnClickListener(v -> viewPager.setCurrentItem(index));
        }

        addPostBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // 5. Gestion des Insets (EdgeToEdge support)
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
        
        // Initial selection
        updateBottomNavSelection(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestedPublicProfilePhoto = false;
        userViewModel.fetchUser();
    }

    private void updateBottomNavSelection(int position) {
        int activeColor = ContextCompat.getColor(this, R.color.red_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_inactive);
        
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setImageTintList(ColorStateList.valueOf(i == position ? activeColor : inactiveColor));
        }
    }

    private void loadProfileImage(String photoProfil) {
        if (photoProfil == null || photoProfil.trim().isEmpty()) {
            profileBtn.setImageDrawable(null);
            return;
        }

        String photoUrl = photoProfil.replace("localhost", "10.0.2.2");
        Glide.with(this)
                .load(photoUrl)
                .into(profileBtn);
    }

    private boolean hasPhoto(String photoProfil) {
        return photoProfil != null && !photoProfil.trim().isEmpty();
    }
}
