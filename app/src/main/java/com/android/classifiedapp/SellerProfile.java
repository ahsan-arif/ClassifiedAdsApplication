package com.android.classifiedapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Rating;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SellerProfile extends AppCompatActivity {
TextView tvName;
RatingBar ratingbar;
CircleImageView imgUser;
ImageView imgBack;
RecyclerView rvAds;
String sellerId;
TextView tvNoListing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seller_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvName = findViewById(R.id.tv_name);
        ratingbar = findViewById(R.id.ratingbar);
        imgUser = findViewById(R.id.img_user);
        imgBack = findViewById(R.id.img_back);
        rvAds = findViewById(R.id.rv_ads);
        tvNoListing = findViewById(R.id.tv_no_listing);
        sellerId = getIntent().getStringExtra("sellerId");
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LogUtils.e(sellerId);
        getSellerProfile();
        getAds(sellerId);
    }

    void getSellerProfile(){
        FirebaseDatabase.getInstance().getReference().child("users").child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = new User();
                    user.setEmail(snapshot.child("email").getValue(String.class));
                    // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                    user.setName(snapshot.child("name").getValue(String.class));
                    user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                    tvName.setText(user.getName());
                    if (snapshot.hasChild("profileImage")){
                        user.setProfileImage(snapshot.child("profileImage").getValue(String.class));
                        Glide.with(SellerProfile.this).load(user.getProfileImage()).into(imgUser);
                    }else{
                        imgUser.setImageResource(R.drawable.outline_account_circle_24);
                    }
                    getSellerRating(sellerId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void getSellerRating(String uid){
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("ratings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    float commulativeRating=0;
                    List<Rating> ratings = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Rating rating = dataSnapshot.getValue(Rating.class);
                        ratings.add(rating);
                        commulativeRating = commulativeRating+rating.getRating();
                    }
                    float averageRating = commulativeRating / ratings.size();
                    ratingbar.setRating(averageRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void getAds(String sellerId){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Ad> ads = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (ad.getStatus().equals(getString(R.string.approved)) && ad.getPostedBy().equals(sellerId)){
                            ads.add(ad);
                            tvNoListing.setVisibility(View.GONE);
                        }
                    }
                    setAdsAdapter(ads);

                }else{
                    tvNoListing.setVisibility(View.VISIBLE);
                    rvAds.setVisibility(View.GONE);
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void setAdsAdapter(ArrayList<Ad> ads){
        LogUtils.e(ads.size());
        rvAds.setLayoutManager(new LinearLayoutManager(SellerProfile.this));
        rvAds.setAdapter(new AdsAdapter(ads,SellerProfile.this,null));
    }
}