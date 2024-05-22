package com.android.classifiedapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.classifiedapp.adapters.ImagePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    ViewPager2 pagerImages;
    TabLayout tabsImg;
    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pagerImages = findViewById(R.id.pager_images);
        tabsImg = findViewById(R.id.tabs_img);
        imgBack = findViewById(R.id.img_back);
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        List<String> urls = new ArrayList<>(imageUrls);

        ImagePagerAdapter adapter = new ImagePagerAdapter(this,urls,true);
        pagerImages.setAdapter(adapter);

        // Setup TabLayout with ViewPager
        new TabLayoutMediator(tabsImg, pagerImages, (tab, position) -> {
            // You can set custom tab view here if needed, for now, just add empty text
            tab.setText("");
        }).attach();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}