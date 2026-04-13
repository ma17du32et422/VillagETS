package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.Dao.UserApi;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Initialisation des vues
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        ImageButton homeBtn = findViewById(R.id.homeBtn);
        ImageButton moneyBtn = findViewById(R.id.moneyBtn);
        ImageButton addPostBtn = findViewById(R.id.addPostBtn);
        ImageButton profileBtn = findViewById(R.id.profileBtn);
        ImageButton searchBtn = findViewById(R.id.searchBtn);
        ImageButton messageBtn = findViewById(R.id.messageBtn);
        ImageButton notificationBtn = findViewById(R.id.notificationBtn);

        // 2. Configuration du ViewPager2
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 3. Logique du Menu Drawer
        menuBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 4. Logique de navigation via les boutons du bas
        homeBtn.setOnClickListener(v -> viewPager.setCurrentItem(0));
        moneyBtn.setOnClickListener(v -> viewPager.setCurrentItem(1));
        searchBtn.setOnClickListener(v -> viewPager.setCurrentItem(2));
        messageBtn.setOnClickListener(v -> viewPager.setCurrentItem(3));
        notificationBtn.setOnClickListener(v -> viewPager.setCurrentItem(4));


        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Executor pour les tâches en arrière-plan
        ExecutorService executor = Executors.newSingleThreadExecutor();


        // 6. Gestion des Insets
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}
