package com.android.classifiedapp;

import static com.android.classifiedapp.utilities.FireNotification.getAccessToken;

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
import com.android.classifiedapp.fragments.FragmentChats;
import com.android.classifiedapp.fragments.FragmentHome;
import com.android.classifiedapp.fragments.FragmentProfile;
import com.android.classifiedapp.fragments.FragmentRandom;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Home extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/firebase.messaging"};
    private GoogleCredentials googleCredentials;
    private InputStream jasonfile;
    private String beaerertoken;
    private String BEARERTOKEN;
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
        try {
            getAccessToken();
        } catch (IOException e) {
            LogUtils.e(e);
            //throw new RuntimeException(e);
        }

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
                }else if (menuItem.getItemId() == R.id.item_chat){
                    startFragment(getSupportFragmentManager(),new FragmentChats());
                }else if (menuItem.getItemId() == R.id.item_random){
                    startFragment(getSupportFragmentManager(),new FragmentRandom());
                }
                return true;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.item_home);
        getFCMToken();
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
        transaction.commit();
    }

    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    updateUserToken(token);
                   // LogUtils.e(token);
                }
            }
        });
    }

    void updateUserToken(String token){
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("fcmToken");
        reference.setValue(token);

    }

    private void getAccessToken() throws IOException {
        LogUtils.e(getPackageName());
        jasonfile = getResources().openRawResource(getApplicationContext().getResources().getIdentifier("service_account", "raw", getPackageName()));
        new Thread(() -> {
            try {
                googleCredentials = GoogleCredentials
                        .fromStream(jasonfile)
                        .createScoped(Arrays.asList(SCOPES));
                googleCredentials.refreshAccessToken().getTokenValue();
                beaerertoken = googleCredentials.refreshAccessToken().getTokenValue();
                //LogUtils.e("beaerertoken:  " + beaerertoken);
                BEARERTOKEN = beaerertoken;
                SharedPrefManager.getInstance(Home.this).setAccessToken(beaerertoken);
                LogUtils.e(beaerertoken);
            } catch (IOException e) {
                LogUtils.e("In error statement");
                e.printStackTrace();
            }
        }).start();
    }

}