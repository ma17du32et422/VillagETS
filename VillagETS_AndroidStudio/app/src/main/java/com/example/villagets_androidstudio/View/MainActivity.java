package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.villagets_androidstudio.R;

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

        // 2. Configuration du ViewPager2
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 3. Logique du Menu Drawer
        menuBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 4. Logique de navigation via les boutons du bas
        homeBtn.setOnClickListener(v -> viewPager.setCurrentItem(0)); // sends to index 0 page
        moneyBtn.setOnClickListener(v -> viewPager.setCurrentItem(1)); // sends to index 1 page

        // 5. Gestion des Insets
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