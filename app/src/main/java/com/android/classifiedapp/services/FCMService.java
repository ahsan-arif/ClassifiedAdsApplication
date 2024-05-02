package com.android.classifiedapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.classifiedapp.Home;
import com.android.classifiedapp.MainActivity;
import com.android.classifiedapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "token";
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReferenceForNotification;
    public static FirebaseAuth auth;
    String CHANNEL_ID = "1000";
    String pushKey;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size()>0){

            //Firebase Instances
            auth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReferenceForNotification = firebaseDatabase.getReference("notification");


            Map<String,String> map = remoteMessage.getData();

            String title = map.get("title");
            String message = map.get("body");
//            String activity = map.get("activity");

            /*String getDate = getDate();
            String getTime = utilMethods.getTime();
            pushKey = databaseReferenceForNotification.push().getKey();

            storeNotificationInRealTime(new NotificationModel(getDate,getTime,title,message,pushKey,false));*/


            Log.e("PostDetail_makeOffer", "notification_checking: service class ---service class--Recieved" );


            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                createOreoNotification(title,message);
            }
            else {
                createNotification(title,message);
            }
        }

        super.onMessageReceived(remoteMessage);
    }
    @Override
    public void onNewToken(@NonNull String s) {
        updateToken(s);
        super.onNewToken(s);
    }

    private void updateToken(String token) {
        Log.i(TAG, "PostDetail_makeOffer: " + token);
    }

    private void createNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round);

        Intent intent = new Intent(this, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85-65), builder.build());
        Log.e("PostDetail_makeOffer", "createNotification notification_checking: service class ---service class--Recieved" );

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message){

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);


        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .build();

        manager.notify(new Random().nextInt(85-65),notification);
        Log.e("PostDetail_makeOffer", "createOreoNotification notification_checking: service class ---service class--Recieved" );


    }

}
