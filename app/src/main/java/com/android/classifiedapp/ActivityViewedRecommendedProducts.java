package com.android.classifiedapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.adapters.RecentlyViewedAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.ViewedAd;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ActivityViewedRecommendedProducts extends AppCompatActivity {
ArrayList<Ad> ads;
TextView tvTitle,tvNothing;
ProgressBar progressCircular;
RecyclerView rvAds;
ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewed_recommended_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTitle = findViewById(R.id.tv_title);
        tvNothing = findViewById(R.id.tv_nothing);
        progressCircular = findViewById(R.id.progress_circular);
        rvAds = findViewById(R.id.rv_ads);
        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
       boolean isRecommended = getIntent().getBooleanExtra("isRecommended",false);
       if (isRecommended){
           tvTitle.setText(getString(R.string.recommended_products));
           getInteractedCategories(uid);
       }else{
           tvTitle.setText(getString(R.string.recently_viewed));
           getViewedAdIds(uid);
       }
    }
    void getInteractedCategories(String uid){
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<ViewedAd> viewedAds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ViewedAd viewedAd = new ViewedAd();
                            viewedAd.setViewedOn(dataSnapshot.child("visitedOn").getValue(Long.class));
                            viewedAd.setAdId(dataSnapshot.getKey());
                            LogUtils.e(dataSnapshot.getKey());
                            viewedAds.add(viewedAd);
                        }
                        Collections.sort(viewedAds, new Comparator<ViewedAd>() {
                            @Override
                            public int compare(ViewedAd o1, ViewedAd o2) {
                                return Long.compare(o1.getViewedOn(),o2.getViewedOn());
                            }
                        });
                        getAdsByCategory(viewedAds);
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

    void getAdsByCategory(ArrayList<ViewedAd> interactedCategories){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            for (ViewedAd interactedCat : interactedCategories){
                                if (ad.getCategoryId().equals(interactedCat.getAdId())){
                                    ads.add(ad);
                                }
                            }

                        }
                        if (ads.size()>0){
                            tvNothing.setVisibility(View.GONE);
                            progressCircular.setVisibility(View.GONE);
                            AdsAdapter adsAdapter = new AdsAdapter(ads,ActivityViewedRecommendedProducts.this,null);
                            rvAds.setAdapter(adsAdapter);
                            rvAds.setLayoutManager(new LinearLayoutManager(ActivityViewedRecommendedProducts.this));
                        }else{
                            tvNothing.setVisibility(View.VISIBLE);
                            progressCircular.setVisibility(View.GONE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                  tvNothing.setVisibility(View.VISIBLE);
                  progressCircular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNothing.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);
            }
        });
    }

    void getViewedAdIds(String uid){
        LogUtils.e(uid);
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recentlyVisited").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    LogUtils.e("datasnapshot exists");
                    LogUtils.e(snapshot);
                    LogUtils.e(snapshot.getChildren());
                    try {
                        ads = new ArrayList<>();
                        ArrayList<ViewedAd> viewedAds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            LogUtils.e(dataSnapshot);
                            ViewedAd viewedAd = new ViewedAd();
                            viewedAd.setViewedOn(dataSnapshot.child("visitedOn").getValue(Long.class));
                            viewedAd.setAdId(dataSnapshot.getKey());
                            LogUtils.e(dataSnapshot.getKey());
                            viewedAds.add(viewedAd);
                        }
                        Collections.sort(viewedAds, new Comparator<ViewedAd>() {
                            @Override
                            public int compare(ViewedAd o1, ViewedAd o2) {
                                return Long.compare(o1.getViewedOn(),o2.getViewedOn());
                            }
                        });
                        getViewedAds(viewedAds);
                    }catch (Exception e){
                        LogUtils.e(e.getMessage());
                        e.printStackTrace();
                    }
                }else{
                    tvNothing.setVisibility(View.VISIBLE);
                    progressCircular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNothing.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);
            }
        });
    }

    void getViewedAds(ArrayList<ViewedAd> viewedAds){
        //fetch the details of ad by AdID
        for (ViewedAd viewedAd : viewedAds){
            getAd(viewedAd.getAdId(),viewedAds);
        }
    }
    void getAd(String adId,ArrayList<ViewedAd> viewedAds){
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        Ad ad = snapshot.getValue(Ad.class);
                        ads.add(ad);
                        LogUtils.e(ads.size(),viewedAds.size());
                        if (ads.size()==viewedAds.size()){
                            tvNothing.setVisibility(View.GONE);
                            progressCircular.setVisibility(View.GONE);
                            AdsAdapter adsAdapter = new AdsAdapter(ads,ActivityViewedRecommendedProducts.this,null);
                            rvAds.setAdapter(adsAdapter);
                            rvAds.setLayoutManager(new LinearLayoutManager(ActivityViewedRecommendedProducts.this));

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    tvNothing.setVisibility(View.VISIBLE);
                    progressCircular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNothing.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);
            }
        });
    }
}