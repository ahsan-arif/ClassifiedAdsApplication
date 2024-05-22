package com.android.classifiedapp;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;
import static com.android.classifiedapp.utilities.FireNotification.getAccessToken;
import static com.android.classifiedapp.utilities.FireNotification.prepNotification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import com.android.classifiedapp.adapters.ImagePagerAdapter;
import com.android.classifiedapp.adapters.MessagesAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Message;
import com.android.classifiedapp.models.User;
import com.android.classifiedapp.utilities.Constants;
import com.android.classifiedapp.utilities.FCMSender;
import com.android.classifiedapp.utilities.FireNotification;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityChat extends AppCompatActivity {
    EditText etMessage;
    FloatingActionButton btnSend;
    String sellerId;
    String currentUserId;
    ArrayList<Message> messages;
    RecyclerView rvMessages;
    MessagesAdapter adapter;
    ImageView imgBack;
    CircleImageView imgUser;
    TextView tvUserName;
    String fcmToken,currentUserName;
    String accessToken;
    boolean isAdmin;
    String adId;
    CircleImageView imgProduct;
    TextView tvProductTitle;
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
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sellerId = getIntent().getStringExtra("sellerId");
        LogUtils.e("seller id ",sellerId);
        adId = getIntent().getStringExtra("adId");
        LogUtils.e("adId id ",adId);
        //LogUtils.e(sellerId);

        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        rvMessages = findViewById(R.id.rv_messages);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        imgBack = findViewById(R.id.img_back);
        imgUser = findViewById(R.id.img_user);
        tvUserName = findViewById(R.id.tv_userName);
        imgProduct = findViewById(R.id.img_product);
        tvProductTitle = findViewById(R.id.tv_productTitle);
        accessToken = SharedPrefManager.getInstance(ActivityChat.this).getAccessToken();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(etMessage, InputMethodManager.SHOW_FORCED);
//        LogUtils.e(accessToken);
        Bundle extras = getIntent().getExtras();
        if (extras!=null&&sellerId==null){
            LogUtils.e(extras);
            String deeplink = extras.getString("deepLink");
            LogUtils.e(deeplink);
            if (deeplink == null){
                Bundle data = extras.getBundle("data");
                sellerId = data.getString("id");
                adId = data.getString("adId");
                LogUtils.e(sellerId ,adId);
            }else{
                String strings[] = deeplink.split(":");
                String items[]= strings[2].split("_");
                sellerId = items[0];
                adId = items[1];
            }



            etMessage.setInputType(InputType.TYPE_CLASS_TEXT);
            etMessage.requestFocus();

            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ActivityChat.this, Home.class));
                    finish();
                }
            });
        }
        else{
            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        LogUtils.e(extras);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage.getText().toString().isEmpty()){
                    return;
                }
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").child(adId).child(sellerId).push();
                DatabaseReference sellerChatRef = FirebaseDatabase.getInstance().getReference().child("users").child(sellerId).child("chats").child(adId).child(currentUserId).push();
                String messageId = databaseReference.getKey();
                Message message = new Message();
                message.setSenderId(currentUserId);
                message.setReceiverId(sellerId);
                message.setMessage(etMessage.getText().toString().trim());
                message.setMessageId(messageId);
                message.setTimestamp(System.currentTimeMillis());
                databaseReference.setValue(message);
                sellerChatRef.setValue(message);
                try {
                    sendPushNotification(fcmToken,currentUserName,message.getMessage(),isAdmin);
                } catch (JSONException e) {
                    LogUtils.e(e.getMessage());
                }
                etMessage.setText("");
            }
        });

        rvMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvMessages.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityChat.this,SellerProfile.class).putExtra("sellerId",sellerId));
            }
        });
        rvMessages.setNestedScrollingEnabled(false);
        rvMessages.setHasFixedSize(true);
        getCurrentUserDetails(currentUserId);
        getSellerDetails(sellerId);
        getListing(adId);
        getMessages();

    }

    void getMessages(){
        ProgressDialog progressDialog = new ProgressDialog(ActivityChat.this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").child(adId).child(sellerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                messages = new ArrayList<>();
                if (snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Message message = dataSnapshot.getValue(Message.class);
                        messages.add(message);
                    }
                    adapter = new MessagesAdapter(messages,ActivityChat.this);
                    rvMessages.setLayoutManager(new LinearLayoutManager(ActivityChat.this));
                    rvMessages.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    void getSellerDetails(String sellerId){
        if (sellerId == null){
            sellerId = getIntent().getStringExtra("sellerId");
        }

        LogUtils.e(sellerId);
        FirebaseDatabase.getInstance().getReference().child("users").child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    // LogUtils.e(snapshot);
                    //   for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = new User();
                    user.setEmail(snapshot.child("email").getValue(String.class));
                    // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                    user.setName(snapshot.child("name").getValue(String.class));
                    user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                    user.setRole(snapshot.child("role").getValue(String.class));
                    if (user.getRole().equals("admin")){
                        isAdmin = true;
                    }else{
                        isAdmin = false;
                    }
                   // tvUserName.setText(user.getName());
                    LogUtils.e(user.getFcmToken());
                    fcmToken = user.getFcmToken();
                    if (snapshot.hasChild("profileImage")){
                        user.setProfileImage(snapshot.child("profileImage").getValue(String.class));
                        Glide.with(ActivityChat.this).load(user.getProfileImage()).into(imgUser);
                    }else{
                        imgUser.setImageResource(R.drawable.outline_account_circle_24);
                    }
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

  /*  void sendPushNotification(String receiverFCM,String title,String message){
       prepNotification(receiverFCM,ActivityChat.this,title,message);

    }*/

    void getCurrentUserDetails(String currentUserId){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User currentUser = snapshot.getValue(User.class);
                    currentUser.setEmail(snapshot.child("email").getValue(String.class));
                    // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                    currentUser.setName(snapshot.child("name").getValue(String.class));
                    currentUserName = currentUser.getName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void sendPushNotification(String toFcmToken,String title,String body,boolean isAdmin) throws JSONException {
        LogUtils.e(isAdmin);
        String clickAction;
        if (isAdmin){
            clickAction = "com.example.classifiedadsappadmin.ActivityChat";
        }else{
            clickAction = "com.android.classifiedapp.ActivityChat";
        }
        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",fcmToken);

        if (isAdmin){
            JSONObject dataObject = new JSONObject();
            dataObject.put("id",currentUserId);
            LogUtils.e(currentUserId);
            dataObject.put("deepLink","https://classifiedadsapplication.page.link/chat:"+currentUserId);

            messageObject.put("data",dataObject);
        }else{
            JSONObject dataObject = new JSONObject();
            dataObject.put("id",currentUserId);
            dataObject.put("adId",adId);
            dataObject.put("deepLink","https://classifiedadsapplication.page.link/chat:"+currentUserId+"_"+adId);
            messageObject.put("data",dataObject);
        }


        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action",clickAction);

        androidObject.put("notification",activityNotificationObject);
        messageObject.put("android",androidObject);

        JSONObject finalObject = new JSONObject();
        finalObject.put("message",messageObject);
        //finalObject.put("data",dataObject);
        LogUtils.json(finalObject);

// Create a new RequestQueue
        RequestQueue queue = Volley.newRequestQueue(ActivityChat.this);

// Create a new JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NOTIFICATION_URL, finalObject,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response from the FCM server
                        //LogUtils.json(response);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        LogUtils.e(error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
// Add the request to the RequestQueue
        queue.add(request);
        //  This code will send a push notification to the device with the title "New Like!" and the body "Someone has liked your post!".
        //I hope this helps! Let me know if you have any other questions.
    }

    void getListing(String adId){
        if (adId==null){
            adId = getIntent().getStringExtra("adId");
        }
        ProgressDialog progressDialog = new ProgressDialog(ActivityChat.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_ad));
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()){
                    FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                  Ad  ad = snapshot.getValue(Ad.class);
                    if (ad!=null){
                        tvUserName.setText(ad.getCurrency()+" "+ad.getPrice());
                       String image=  ad.getUrls().get(0);
                        Glide.with(ActivityChat.this).load(image).into(imgProduct);
                        tvProductTitle.setText(ad.getTitle());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }

}