package com.android.classifiedapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.ActivityChat;
import com.android.classifiedapp.ActivityEditAd;
import com.android.classifiedapp.ActivityMyOrders;
import com.android.classifiedapp.ActivityPageAdDetails;
import com.android.classifiedapp.ActivityViewOrders;
import com.android.classifiedapp.Home;
import com.android.classifiedapp.MainActivity;
import com.android.classifiedapp.R;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        LogUtils.e(data);
//        String deepLink = data.get("deepLink");
        String id = data.get("id");
        String adId = "";
        if (data.get("adId")!=null){
            adId = data.get("adId");
        }
        String title = "";
        if (data.get("title")!=null){
            title = data.get("title");
        }

        //LogUtils.e(id);
        LogUtils.e(adId);
        LogUtils.e(title);
        String clickAction = remoteMessage.getNotification().getClickAction();
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),clickAction,id,adId,title);
    }
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private void showNotification(String title, String body,String clickAction,String id,String adId,String productTitle) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "com.android.classifiedapp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         createOreoNotification(title,body);
        }

      /*  Intent notificationIntent = new Intent();
        notificationIntent.setAction(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(deepLink));
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
        Intent notificationIntent;
        if (clickAction.equals("com.android.classifiedapp.ActivityChat")){
            LogUtils.e("here");
            Bundle bundle = new Bundle();
            bundle.putString("adId", adId);
            bundle.putString("id", id);
            notificationIntent = new Intent(getBaseContext(), ActivityChat.class).putExtra("data",bundle);
        }else if(clickAction.equals("com.android.classifiedapp.ActivityEditAd")){
            notificationIntent = new Intent(getBaseContext(), ActivityEditAd.class).putExtra("id",id);
        }else if (clickAction.equals("com.android.classifiedapp.ActivityAdDetails")){
            notificationIntent = new Intent(getBaseContext(), ActivityAdDetails.class).putExtra("id",id);
        }else if (clickAction.equals("com.android.classifiedapp.ActivityMyOrders")){
            notificationIntent = new Intent(getBaseContext(), ActivityMyOrders.class);
        }else if (clickAction.equals("com.android.classifiedapp.ActivityViewOrders")){
            notificationIntent = new Intent(getBaseContext(), ActivityViewOrders.class).putExtra("adId",adId).putExtra("title",productTitle);
        }
        else{
            notificationIntent = new Intent(getBaseContext(),Home.class);
        }
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);
       /* PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.outline_account_circle_24)
                .setContentTitle(title)
                .setContentIntent(intent)
                .setContentText(body)
                .setContentInfo("Info");

        notificationManager.notify(new Random().nextInt(),notificationBuilder.build());

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message){
        String NOTIFICATION_CHANNEL_ID = "com.android.classifiedapp";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);


        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this,"69")
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.outline_account_circle_24)
                .setAutoCancel(true)
                .build();

        manager.notify(new Random().nextInt(85-65),notification);
        Log.e("PostDetail_makeOffer", "createOreoNotification notification_checking: service class ---service class--Recieved" );


    }

}
