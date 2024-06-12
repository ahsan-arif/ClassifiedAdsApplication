package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityCustomerSupportChat;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.CustomerSupportChat;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerSupportChatsAdapter extends RecyclerView.Adapter<CustomerSupportChatsAdapter.ViewHolder> {
    Context context;
    ArrayList<CustomerSupportChat> chats;

    public CustomerSupportChatsAdapter(Context context, ArrayList<CustomerSupportChat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_support,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerSupportChat chat = chats.get(position);

        long time = chat.getLastMessage().getTimestamp();
        Date date = new Date(time);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);

        holder.tvLastMessage.setText(chat.getLastMessage().getMessage());
        holder.tvLastMessage.setVisibility(View.VISIBLE);
        holder.tvTime.setVisibility(View.VISIBLE);
        holder.tvTime.setText(ago);

        getAdmin(holder.imgAdmin,holder.tvAdminName,chat.getAdminId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityCustomerSupportChat.class).putExtra("id",chat.getAdminId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imgAdmin;
        TextView tvAdminName;
        TextView tvLastMessage;
        TextView tvTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAdmin = itemView.findViewById(R.id.img_admin);
            tvAdminName = itemView.findViewById(R.id.tv_admin_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    void getAdmin(CircleImageView circleImageView,TextView textView,String adminId){
        FirebaseDatabase.getInstance().getReference().child("users").child(adminId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        User user = new User();
                        user.setEmail(snapshot.child("email").getValue(String.class));
                        // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                        user.setName(snapshot.child("name").getValue(String.class));
                        user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                        user.setRole(snapshot.child("role").getValue(String.class));
                        user.setId(snapshot.child("id").getValue(String.class));

                        if (user.getName()!=null){
                            textView.setText(user.getName());
                        }
                        if (user.getProfileImage()!=null){
                            Glide.with(context).load(user.getProfileImage()).into(circleImageView);
                        }else{
                            circleImageView.setImageResource(R.drawable.outline_account_circle_24);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    textView.setText(context.getString(R.string.deleted_user));
                    circleImageView.setImageResource(R.drawable.outline_account_circle_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
