package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.R;

public class ItemDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);

        View toolbarContainer = findViewById(R.id.toolbarContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbarContainer.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        Log.d("ItemDetails", "Loading title: " + title);
        Log.d("ItemDetails", "Loading image URL: " + imageUrl);

        TextView tvTitle = findViewById(R.id.tvItemTitle);
        TextView tvDescriptionContent = findViewById(R.id.tvItemDescriptionContent);
        TextView tvPrice = findViewById(R.id.tvItemPrice);
        ImageView ivPhoto = findViewById(R.id.ivItemPhoto);

        tvTitle.setText(title);
        tvDescriptionContent.setText(description);
        tvPrice.setText(price);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String finalUrl = imageUrl;
            if (finalUrl.contains("localhost")) {
                finalUrl = finalUrl.replace("localhost", "10.0.2.2");
            }
            
            Glide.with(this)
                    .load(finalUrl)
                    .placeholder(R.drawable.silicate)
                    .error(R.drawable.silicate)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.silicate);
        }
    }
}
