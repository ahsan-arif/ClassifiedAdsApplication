package com.android.classifiedapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.utilities.LocaleHelper;
import com.blankj.utilcode.util.LogUtils;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this, "es");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               Intent appLinkIntent = getIntent();
               Uri appLinkData = appLinkIntent.getData();
               if (appLinkData!=null){
                   String adId = appLinkData.getLastPathSegment();
                   if (adId.contains("ad")){
                       LogUtils.e(adId);
                       startActivity(new Intent(SplashScreen.this,ActivityPageAdDetails.class).putExtra("adId",adId));
                       finish();
                   }else if (adId.contains("chat")){
                       LogUtils.e("splash forwarding");
                       String[] ids = adId.split(":");
                       startActivity(new Intent(SplashScreen.this,ActivityChat.class).putExtra("asdasd",ids[1]));
                       finish();
                   }else if (adId.contains("reportedAdId")){
                       String[] ids = adId.split(":");
                       startActivity(new Intent(SplashScreen.this, ActivityEditAd.class));
                       LogUtils.e(ids);
                       finish();
                   }

               }else{
                   startActivity(new Intent(SplashScreen.this,MainActivity.class));
                   finish();
               }
           }
       },2000);

    }
}