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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityChat extends AppCompatActivity {
    EditText etMessage;
    CircleImageView btnSend;
    String sellerId;
    String currentUserId;
    ArrayList<Message> messages;
    RecyclerView rvMessages;
    MessagesAdapter adapter;
    ImageView imgBack;
    CircleImageView imgUser;
    TextView tvUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sellerId = getIntent().getStringExtra("sellerId");
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        rvMessages = findViewById(R.id.rv_messages);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        imgBack = findViewById(R.id.img_back);
        imgUser = findViewById(R.id.img_user);
        tvUserName = findViewById(R.id.tv_userName);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage.getText().toString().isEmpty()){
                    return;
                }
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").child(sellerId).push();
            String messageId = databaseReference.getKey();
                Message message = new Message();
                message.setSenderId(currentUserId);
                message.setReceiverId(sellerId);
                message.setMessage(etMessage.getText().toString().trim());
                message.setMessageId(messageId);
                databaseReference.setValue(message);
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getMessages();
        getSellerDetails(sellerId);
    }

    void getMessages(){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").child(sellerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

            }
        });
    }

    void getSellerDetails(String sellerId){
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
                    tvUserName.setText(user.getName());
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
}