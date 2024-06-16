package com.android.classifiedapp;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.MessagesAdapter;
import com.android.classifiedapp.models.Message;
import com.android.classifiedapp.models.User;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityCustomerSupportChat extends AppCompatActivity {

    String adminId;
    TextView tvAdminName;
    ImageView imgBack;
    FloatingActionButton btnSend;
    EditText etMessage;

    MessagesAdapter adapter;
    RecyclerView rvChats;
    String fcmToken;
    String currentUserName;
    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_support_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        adminId = getIntent().getStringExtra("id");
        tvAdminName = findViewById(R.id.tv_admin_name);
        imgBack = findViewById(R.id.img_back);

        btnSend = findViewById(R.id.btn_send);
        etMessage = findViewById(R.id.et_message);
        rvChats = findViewById(R.id.rv_chats);
        accessToken = SharedPrefManager.getInstance(ActivityCustomerSupportChat.this).getAccessToken();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()){
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("customerSupportChat").child(adminId).push();
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("users").child(adminId).child("customerSupportChat").child(currentUserId).push();
                    Message message1 = new Message();
                    message1.setMessageId(currentUserRef.getKey());
                    message1.setTimestamp(System.currentTimeMillis());
                    message1.setReceiverId(adminId);
                    message1.setSenderId(currentUserId);
                    message1.setMessage(message);

                    currentUserRef.setValue(message1);
                    adminRef.setValue(message1);

                    //TODO send notification to ADMIN
                    try {
                        sendPushNotification(currentUserName,message);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        getAdmin(adminId);
        getCurrentUserDetails(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getMessages(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    void getAdmin(String adminId){
        FirebaseDatabase.getInstance().getReference().child("users").child(adminId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        User user = new User();
                        LogUtils.json(snapshot.getChildren());
                        user.setEmail(snapshot.child("email").getValue(String.class));
                        // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                        user.setName(snapshot.child("name").getValue(String.class));
                        user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                        fcmToken = user.getFcmToken();
                        user.setRole(snapshot.child("role").getValue(String.class));
                        LogUtils.e(snapshot.child("role").getValue(String.class));

                        if (user.getName()!=null)
                            tvAdminName.setText(user.getName());
                        else
                            tvAdminName.setText(getString(R.string.deleted_user));

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

    void getMessages(String currentUserId){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("customerSupportChat").child(adminId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<Message> messages = new ArrayList<>();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                            Message message = dataSnapshot.getValue(Message.class);
                            messages.add(message);
                        }
                        adapter = new MessagesAdapter(messages,ActivityCustomerSupportChat.this);
                        rvChats.setAdapter(adapter);
                        rvChats.setLayoutManager(new LinearLayoutManager(ActivityCustomerSupportChat.this));
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
    void sendPushNotification(String title,String body) throws JSONException {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",fcmToken);

        JSONObject dataObject = new JSONObject();
        dataObject.put("id",currentUserId);
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/support/id:"+currentUserId);
        messageObject.put("data",dataObject);

        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action","com.example.classifiedadsappadmin.ActivityCustomerSupportChat");

        androidObject.put("notification",activityNotificationObject);
        messageObject.put("android",androidObject);

        JSONObject finalObject = new JSONObject();
        finalObject.put("message",messageObject);
        //finalObject.put("data",dataObject);
        LogUtils.json(finalObject);

// Create a new RequestQueue
        RequestQueue queue = Volley.newRequestQueue(ActivityCustomerSupportChat.this);

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
}