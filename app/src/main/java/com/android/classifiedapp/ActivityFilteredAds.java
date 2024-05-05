package com.android.classifiedapp;

import android.content.Intent;
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
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.android.classifiedapp.models.SubCategory;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityFilteredAds extends AppCompatActivity {
    Double latitude,longitude,to,from;
    Category category;
    SubCategory subCategory;
    Currency currency;
    ArrayList<Ad> ads;
    RecyclerView rvAds;
    ImageView imgBack;
    TextView tvNoAds;
    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters
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
        setContentView(R.layout.activity_filtered_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvAds = findViewById(R.id.rv_ads);
        imgBack = findViewById(R.id.img_back);
        tvNoAds = findViewById(R.id.tv_no_ads);

        Intent intent = getIntent();
       latitude= intent.getDoubleExtra("latitude",0);
       longitude= intent.getDoubleExtra("longitude",0);
       category= intent.getParcelableExtra("category");
       subCategory= intent.getParcelableExtra("subcategory");
       currency= intent.getParcelableExtra("currency");
       to= intent.getDoubleExtra("to",Double.MAX_VALUE);
        from = intent.getDoubleExtra("from",0);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ads = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (category!=null){
                            if (subCategory!=null){
                                Double price = Double.parseDouble(ad.getPrice());
                                if (price>=from&&price<=to){
                                    Double adLat = ad.getLatitude();
                                    Double adLong = ad.getLongitude();
                                    if (latitude>0){
                                        double distance = calculateDistance(latitude,longitude,adLat,adLong);
                                        LogUtils.e(distance);
                                        if (currency!=null){
                                            if (ad.getCurrency().equals(currency.getCurrency())){
                                                if (distance<=50
                                                        && ad.getSubcategoryId().equals(subCategory.getId())
                                                        && ad.getCategoryId().equals(category.getId())
                                                        && ad.getCurrency().equals(currency.getCurrency()))
                                                            ads.add(ad);
                                            }
                                        }else{ //currency not selected
                                            if (distance<=50 && ad.getSubcategoryId().equals(subCategory.getId()) && ad.getCategoryId().equals(category.getId()))
                                                ads.add(ad);
                                        }
                                        LogUtils.e(calculateDistance(latitude,longitude,adLat,adLong));
                                    }else{ //latitude not selected
                                        if (currency!=null){
                                            if (ad.getCurrency().equals(currency.getCurrency())
                                                    && ad.getSubcategoryId().equals(subCategory.getId())
                                                    && ad.getCategoryId().equals(category.getId())){
                                                    ads.add(ad);
                                            }
                                        }else{ //currency not selected
                                            if (ad.getCategoryId().equals(category.getId())&& ad.getSubcategoryId().equals(subCategory.getId()))
                                                ads.add(ad);
                                        }
                                    }

                                }

                            }
                            else{//if subcat is not selected or doesn't exist
                                Double price = Double.parseDouble(ad.getPrice());
                                if (price>=from&&price<=to){
                                    Double adLat = ad.getLatitude();
                                    Double adLong = ad.getLongitude();
                                    if (latitude>0){
                                        double distance = calculateDistance(latitude,longitude,adLat,adLong);
                                        LogUtils.e(distance);
                                        if (currency!=null){
                                            if (ad.getCurrency().equals(currency.getCurrency())){
                                                if (distance<=50
                                                        && ad.getCategoryId().equals(category.getId())
                                                        && ad.getCurrency().equals(currency.getCurrency()))
                                                    ads.add(ad);
                                            }
                                        }else{ //currency not selected
                                            if (distance<=50 && ad.getCategoryId().equals(category.getId()))
                                                ads.add(ad);
                                        }
                                        LogUtils.e(calculateDistance(latitude,longitude,adLat,adLong));
                                    }else{ //latitude not selected
                                        if (currency!=null){
                                            if (ad.getCurrency().equals(currency.getCurrency())
                                                    && ad.getCategoryId().equals(category.getId())){
                                                ads.add(ad);
                                            }
                                        }else{ //currency not selected
                                            if (ad.getCategoryId().equals(category.getId()))
                                                ads.add(ad);
                                        }
                                    }

                                }
                            }
                        }
                        else{//if category is not selected
                            Double price = Double.parseDouble(ad.getPrice());
                            if (price>=from&&price<=to){
                                Double adLat = ad.getLatitude();
                                Double adLong = ad.getLongitude();
                                if (latitude>0){
                                    double distance = calculateDistance(latitude,longitude,adLat,adLong);
                                    LogUtils.e(distance);
                                    if (currency!=null){
                                        if (ad.getCurrency().equals(currency.getCurrency())){
                                            if (distance<=50
                                                    && ad.getCurrency().equals(currency.getCurrency()))
                                                ads.add(ad);
                                        }
                                    }else{ //currency not selected
                                        if (distance<=50)
                                            ads.add(ad);
                                    }
                                    LogUtils.e(calculateDistance(latitude,longitude,adLat,adLong));
                                }else{ //latitude not selected
                                    if (currency!=null){
                                        if (ad.getCurrency().equals(currency.getCurrency())){
                                            ads.add(ad);
                                        }
                                    }else{ //currency not selected
                                            ads.add(ad);
                                    }
                                }

                            }
                        }
                    }
                    //setAdapter for ads
                    if (!ads.isEmpty())
                    setAdsAdapter(ads);
                    else tvNoAds.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Calculate the distance between two points given their latitude and longitude
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Calculate the Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
    }
    void setAdsAdapter(ArrayList<Ad> ads){
        tvNoAds.setVisibility(View.GONE);
        rvAds.setLayoutManager(new LinearLayoutManager(ActivityFilteredAds.this));
        rvAds.setAdapter(new AdsAdapter(ads,ActivityFilteredAds.this,null));
    }
}