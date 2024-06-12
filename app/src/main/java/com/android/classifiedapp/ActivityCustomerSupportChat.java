package com.android.classifiedapp;

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
import com.blankj.utilcode.util.LogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityCustomerSupportChat extends AppCompatActivity {

    String adminId;
    TextView tvAdminName;
    ImageView imgBack;
    FloatingActionButton btnSend;
    EditText etMessage;

    MessagesAdapter adapter;
    RecyclerView rvChats;

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
}