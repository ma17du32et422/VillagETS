package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.SessionManager;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.Utils.CustomTypefaceSpan;
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

        // Activation du mode Edge-to-Edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialisation du client Retrofit
        RetrofitClient.getInstance(getApplicationContext());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // 1. Initialisation des vues
        TextView appTitle = findViewById(R.id.appTitle);
        setupTitleSpannable(appTitle);

        ImageButton addPostBtn = findViewById(R.id.addPostBtn);
        profileBtn = findViewById(R.id.profileBtn);
        View toolbar = findViewById(R.id.toolbar);
        View bottomNav = findViewById(R.id.bottomNavContainer);
        
        viewPager = findViewById(R.id.view_pager);
        
        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.homeBtn));
        navButtons.add(findViewById(R.id.moneyBtn));
        navButtons.add(findViewById(R.id.searchBtn));
        navButtons.add(findViewById(R.id.messageBtn));

        cachedUser = User.loadUser(getApplicationContext());
        if (cachedUser != null) {
            loadProfileImage(cachedUser.getPhotoProfil());
        }

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) return;
            if ((user.getPhotoProfil() == null || user.getPhotoProfil().trim().isEmpty())
                    && cachedUser != null && cachedUser.getPhotoProfil() != null) {
                user.setPhotoProfil(cachedUser.getPhotoProfil());
            }
            cachedUser = user;
            loadProfileImage(user.getPhotoProfil());
            user.saveUser(getApplicationContext());
        });

        // 2. Configuration du ViewPager2
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBottomNavSelection(position);
            }
        });

        // 3. Logique de navigation
        for (int i = 0; i < navButtons.size(); i++) {
            final int index = i;
            navButtons.get(i).setOnClickListener(v -> viewPager.setCurrentItem(index));
        }

        addPostBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreatePostActivity.class)));
        profileBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // 4. Gestion des Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(toolbar.getPaddingLeft(), systemBars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), systemBars.bottom);
            return insets;
        });
        
        updateBottomNavSelection(0);
    }

    private void setupTitleSpannable(TextView textView) {
        String fullText = getString(R.string.villagets); // Assumes "VillagETS"
        SpannableString spannable = new SpannableString(fullText);

        Typeface deltaLight = ResourcesCompat.getFont(this, R.font.delta_light);
        Typeface deltaMedium = ResourcesCompat.getFont(this, R.font.delta_medium);

        if (deltaLight != null && deltaMedium != null) {
            int splitIndex = 6; // "Villag" is 6 characters
            if (fullText.length() >= splitIndex) {
                spannable.setSpan(new CustomTypefaceSpan("", deltaLight), 0, splitIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new CustomTypefaceSpan("", deltaMedium), splitIndex, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        textView.setText(spannable);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Glide.with(this).load(photoUrl).into(profileBtn);
    }
}
