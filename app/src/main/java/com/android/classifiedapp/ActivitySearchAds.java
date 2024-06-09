package com.android.classifiedapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivitySearchAds extends AppCompatActivity {
ImageView imgBack;
SearchView svAds;
TextView btnSearch;
RecyclerView rvAds;
TextView tvNoProducts;
ProgressBar progressCircular;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgBack = findViewById(R.id.img_back);
        svAds = findViewById(R.id.sv_ads);
        btnSearch = findViewById(R.id.btn_search);
        rvAds = findViewById(R.id.rv_ads);
        tvNoProducts = findViewById(R.id.tv_no_products);
        progressCircular = findViewById(R.id.progress_circular);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String searchQuery = svAds.getQuery().toString().toLowerCase();
               getAds(searchQuery);
                LogUtils.e(searchQuery);
            }
        });

    }

    void getAds(String query){
        rvAds.setVisibility(View.GONE);
        tvNoProducts.setVisibility(View.GONE);
        progressCircular.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Ad> ads = new ArrayList<Ad>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);

                        if (ad.getTitle().toLowerCase().contains(query)){
                            ads.add(ad);
                        }
                    }
                    if (!ads.isEmpty()){
                        rvAds.setVisibility(View.VISIBLE);
                        tvNoProducts.setVisibility(View.GONE);
                        progressCircular.setVisibility(View.GONE);
                        rvAds.setAdapter(new AdsAdapter(ads,ActivitySearchAds.this,null));
                        rvAds.setLayoutManager(new LinearLayoutManager(ActivitySearchAds.this));
                    }else{
                        rvAds.setVisibility(View.GONE);
                        tvNoProducts.setVisibility(View.VISIBLE);
                        progressCircular.setVisibility(View.GONE);
                    }
                }else{
                    rvAds.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(View.VISIBLE);
                    progressCircular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rvAds.setVisibility(View.GONE);
                tvNoProducts.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);
            }
        });
    }
}