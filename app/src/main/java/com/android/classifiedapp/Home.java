package com.android.classifiedapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.classifiedapp.adapters.HomeCategoriesAdapter;
import com.android.classifiedapp.fragments.FragmentHome;
import com.android.classifiedapp.fragments.FragmentProfile;
import com.android.classifiedapp.models.Category;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        //insertData("Cars","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Fcar.svg?alt=media&token=a49f5eda-92bf-41a9-a6a6-64b75c7616ca");
        //insertData("Motorbikes","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Fmotorcycle.svg?alt=media&token=5f7a58d1-30bf-4cac-882a-b4a56d492eb5");
        //insertData("Real Estate","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Freal%20estate.svg?alt=media&token=487a5548-dee3-4cec-a8db-dbaa25311995");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId()==R.id.item_home){
                    androidx.fragment.app.FragmentManager manager = getSupportFragmentManager(); // or getFragmentManager() if using android.app.Fragment
                    FragmentHome fragmentHome = new FragmentHome();
                    startFragment(manager, fragmentHome);
                }else if(menuItem.getItemId()==R.id.item_profile){
                    startFragment(getSupportFragmentManager(),new FragmentProfile());
                }
                return true;
            }
        });
    }

    void insertData(String name,String url){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("categories").push();
        String id= databaseReference.getKey();
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setImageUrl(url);
        databaseReference.setValue(category);
    }

    public void startFragment(FragmentManager manager, Fragment fragment) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}