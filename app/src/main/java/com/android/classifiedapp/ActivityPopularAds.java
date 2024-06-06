package com.android.classifiedapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.adapters.PopularAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ActivityPopularAds extends AppCompatActivity {

    RecyclerView rvAds;
    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popular_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvAds = findViewById(R.id.rv_ads);
        imgBack = findViewById(R.id.img_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getPopularAds();
    }

    void getPopularAds(){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad.getStatus()!=null){
                                if (ad.getLikedByUsers()!=null){
                                    ads.add(ad);
                                }
                            }
                        }
                        if (ads.size()>0){
                            setAdapter(ads);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setAdapter(ArrayList<Ad> ads){
        Collections.sort(ads, new Comparator<Ad>() {
            @Override
            public int compare(Ad ad1, Ad ad2) {
                int size1 = ad1.getLikedByUsers().size();
                int size2 = ad2.getLikedByUsers().size();
                return Integer.compare(size2, size1); // For descending order
            }
        });
        rvAds.setAdapter(new PopularAdsAdapter(ads,ActivityPopularAds.this));
        rvAds.setLayoutManager(new LinearLayoutManager(ActivityPopularAds.this));
    }
}