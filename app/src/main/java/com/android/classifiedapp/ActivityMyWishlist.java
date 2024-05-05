package com.android.classifiedapp;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class ActivityMyWishlist extends AppCompatActivity {
    TextView tvNoItem;
    ImageView imgBack;
    RecyclerView rvAds;

    ArrayList<Ad> ads;
    AdsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200)); // Replace R.color.your_status_bar_color with your desired color resource
        }
        setContentView(R.layout.activity_my_wishlist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvNoItem = findViewById(R.id.tv_no_item);
        imgBack = findViewById(R.id.img_back);
        rvAds = findViewById(R.id.rv_ads);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ads = new ArrayList<>();
        adapter = new AdsAdapter(ads, ActivityMyWishlist.this, true);
        rvAds.setAdapter(adapter);
        getAds(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    void getAds(String currentUserId) {
        FirebaseDatabase.getInstance().getReference().child("ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (ad.getLikedByUsers() != null) {
                            if (!ad.getLikedByUsers().isEmpty()) {
                                if (ad.getLikedByUsers().contains(currentUserId)) {
                                    ads.add(ad);
                                }
                            }
                        }
                    }
                }
                if (!ads.isEmpty()) {
                    setAddsAdapter(ads);
                    tvNoItem.setVisibility(View.GONE);
                } else {
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setAddsAdapter(ArrayList<Ad> ads) {
    rvAds.setAdapter(new AdsAdapter(ads,ActivityMyWishlist.this,true));
    rvAds.setLayoutManager(new LinearLayoutManager(ActivityMyWishlist.this));
    }
}