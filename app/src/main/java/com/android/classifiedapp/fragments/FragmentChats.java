package com.android.classifiedapp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.ChatsAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Chat;
import com.android.classifiedapp.models.Message;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentChats#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentChats extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    String currentUserId;
    ArrayList<Chat> chats;
    RecyclerView rvChats;
    TextView tvNoChats;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentChats() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentChats.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentChats newInstance(String param1, String param2) {
        FragmentChats fragment = new FragmentChats();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getChats();
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        rvChats = view.findViewById(R.id.rv_chats);
        tvNoChats = view.findViewById(R.id.tv_no_chats);
        return view;
    }

    /*void getChats(){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    chats = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        LogUtils.e(dataSnapshot);
                        Chat chat = new Chat();
                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot chatShot : dataSnapshot.getChildren()){
                            LogUtils.e(chatShot);
                            Message message = chatShot.getValue(Message.class);
                            messages.add(message);
                        }
                        if (!messages.isEmpty()){
                            Message lastMessage = messages.get(messages.size()-1);
                            chat.setLastMessage(lastMessage);
                        }

                        String userId = dataSnapshot.getKey();
                        LogUtils.e(userId);

                        //getUserDetails
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    LogUtils.e(snapshot);
                                    User user = snapshot.getValue(User.class);
                                    chat.setReceiver(user);
                                    chats.add(chat);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    //set adapter
                    LogUtils.e(chats);
                    ChatsAdapter adapter = new ChatsAdapter(chats,getContext());
                    rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvChats.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

   /* void getChats(){
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    LogUtils.e("snapshot exists");
                    List<Chat> tempChats = new ArrayList<>(); // Temporary list to hold chats
                    int totalChats = (int) snapshot.getChildrenCount();
                    final int[] chatsProcessed = {0};

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        LogUtils.json(dataSnapshot);
                        Chat chat = new Chat();
                        List<Message> messages = new ArrayList<>();

                        for (DataSnapshot chatShot : dataSnapshot.getChildren()){
                            Message message = chatShot.getValue(Message.class);
                            messages.add(message);
                        }

                        if (!messages.isEmpty()){
                            Message lastMessage = messages.get(messages.size()-1);
                            LogUtils.json(lastMessage);
                            chat.setLastMessage(lastMessage);
                            if (!lastMessage.getReceiverId().equals(currentUserId)){
                                chat.setReceiverId(lastMessage.getReceiverId());
                            }else{
                                chat.setReceiverId(lastMessage.getSenderId());
                            }
                        }

                        String adId = dataSnapshot.getKey();
                        // Fetch user details
                        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Ad ad = snapshot.getValue(Ad.class);
                                    chat.setReceiverId(ad.getPostedBy());
                                    chat.setAd(ad);
                                    tempChats.add(chat); // Add chat to temporary list
                                    chatsProcessed[0]++;

                                    // Check if all user details are fetched before setting adapter
                                    if (chatsProcessed[0] == totalChats) {
                                        setAdapter(tempChats);
                                    }
                                }else{
                                    chatsProcessed[0]++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }*/

    void getChats() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_chats));
        progressDialog.setCancelable(false);
        progressDialog.show();
        LogUtils.json(currentUserId);
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    List<Chat> chats = new ArrayList<>();
                    ArrayList<Message> messages  =new ArrayList<>();
                    for (DataSnapshot rootShot : snapshot.getChildren()){
                        String adId = rootShot.getKey();
                        LogUtils.e(adId);
                        LogUtils.e(rootShot);

                        for (DataSnapshot senderReceiverShot : rootShot.getChildren()){
                            LogUtils.e(senderReceiverShot.getKey());
                            LogUtils.e(senderReceiverShot);

                            for (DataSnapshot messageShot : senderReceiverShot.getChildren()){
                                Message message = messageShot.getValue(Message.class);
                                LogUtils.e(message.getMessage());
                                messages.add(message);
                            }

                            Message lastMessage = messages.get(messages.size()-1);
                            Chat chat = new Chat();
                            chat.setLastMessage(lastMessage);
                            if (lastMessage.getSenderId().equals(currentUserId)){
                                chat.setReceiverId(lastMessage.getReceiverId());
                            }else if(lastMessage.getReceiverId().equals(currentUserId)){
                                chat.setReceiverId(lastMessage.getSenderId());
                            }
                            chats.add(chat);
                        }
                    }
                    setAdapter(chats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                // Handle error
            }
        });
    }



    // Method to set adapter after all user details are fetched
    private void setAdapter(List<Chat> chats) {
        LogUtils.e(chats.size());
        if (chats.isEmpty()){
          tvNoChats.setVisibility(View.VISIBLE);
        }else{
            tvNoChats.setVisibility(View.GONE);
        }
        ChatsAdapter adapter = new ChatsAdapter(chats, getContext());
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(adapter);
    }
}