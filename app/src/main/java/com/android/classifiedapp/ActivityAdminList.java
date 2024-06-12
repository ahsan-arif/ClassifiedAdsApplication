package com.android.classifiedapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.AdminsAdapter;
import com.android.classifiedapp.adapters.CustomerSupportChatsAdapter;
import com.android.classifiedapp.models.CustomerSupportChat;
import com.android.classifiedapp.models.Message;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class ActivityAdminList extends AppCompatActivity {
RecyclerView rvAdmins;
ProgressBar progressCircular;

ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvAdmins = findViewById(R.id.rv_admins);
        progressCircular = findViewById(R.id.progress_circular);
        imgBack = findViewById(R.id.img_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //getAdmins();
        getLastConversation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    void getAdmins(){
        progressCircular.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<User> admins = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = new User();
                            LogUtils.json(snapshot.getChildren());
                            user.setEmail(dataSnapshot.child("email").getValue(String.class));
                            // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                            user.setName(dataSnapshot.child("name").getValue(String.class));
                            user.setFcmToken(dataSnapshot.child("fcmToken").getValue(String.class));
                            user.setRole(dataSnapshot.child("role").getValue(String.class));
                            user.setId(dataSnapshot.child("id").getValue(String.class));
                            LogUtils.e(dataSnapshot.child("role").getValue(String.class));
                            if (user.getRole()!=null){
                                if (user.getRole().equals("admin")){
                                    admins.add(user);
                                }
                            }
                        }
                        if (!admins.isEmpty()){
                            progressCircular.setVisibility(View.GONE);
                            AdminsAdapter adapter = new AdminsAdapter(ActivityAdminList.this,admins);
                            rvAdmins.setAdapter(adapter);
                            rvAdmins.setLayoutManager(new LinearLayoutManager(ActivityAdminList.this));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtils.e(e.getMessage());
                        progressCircular.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);
            }
        });
    }

   void  getLastConversation(String currentUserId){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("customerSupportChat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressCircular.setVisibility(View.VISIBLE);
                if (snapshot.exists()){
                    ArrayList<CustomerSupportChat> chats = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        CustomerSupportChat chat = new CustomerSupportChat();
                        chat.setAdminId(dataSnapshot.getKey());

                        ArrayList<Message> messages = new ArrayList<>();
                        for (DataSnapshot messageShot : dataSnapshot.getChildren()){
                            Message message = messageShot.getValue(Message.class);
                            messages.add(message);
                        }

                        chat.setLastMessage(messages.get(messages.size()-1));

                        chats.add(chat);

                    }
                    //set chats adapter
                    progressCircular.setVisibility(View.GONE);
                    CustomerSupportChatsAdapter adapter = new CustomerSupportChatsAdapter(ActivityAdminList.this,chats);
                    rvAdmins.setLayoutManager(new LinearLayoutManager(ActivityAdminList.this));
                    rvAdmins.setAdapter(adapter);
                }else {
                    getAdmins();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);
            }
        });
   }
}