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
import com.android.classifiedapp.fragments.FragmentAddProduct;
import com.android.classifiedapp.fragments.FragmentHome;
import com.android.classifiedapp.fragments.FragmentProfile;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
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
  /*     insertData("ARS","Argentina","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fargentina_flag.svg?alt=media&token=7a28bf15-4773-4e3f-b906-d6acb25300e1");
       insertData("MXN","Mexico","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fmexico_flag.svg?alt=media&token=76c14093-9531-49e1-b6b5-d4b9f11d6de1");
       insertData("COP","Colombia","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fcolombia_flag.svg?alt=media&token=caa181c5-3b98-4171-8a0c-c138106cc534");
       insertData("EUR","Europe","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Feuro.svg?alt=media&token=ae74105f-8432-46b5-ae73-33d4ab7443df");
       insertData("$","USA","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fusa_flag.svg?alt=media&token=5dcb7eb1-107e-4593-b004-674dd1bad77f");
       insertData("GBP","United Kingdom","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fuk_flag.svg?alt=media&token=006e938c-470f-4941-bdd3-292101aa47c8");
       insertData("BRL","Brazil","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/flags%2Fbrazil_flag.svg?alt=media&token=8dd84eaf-abc4-43a9-86a6-46fca08dc3a2");*/
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
                }else if(menuItem.getItemId() == R.id.item_sell){
                    startFragment(getSupportFragmentManager(),new FragmentAddProduct());
                }
                return true;
            }
        });
    }

    void insertData(String currency,String country,String url){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("currencies").push();
        String id= databaseReference.getKey();
        Currency cur = new Currency();
        cur.setCurrency(currency);
        cur.setCountry(country);
        cur.setImageUrl(url);
        databaseReference.setValue(cur);
    }

    public void startFragment(FragmentManager manager, Fragment fragment) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}