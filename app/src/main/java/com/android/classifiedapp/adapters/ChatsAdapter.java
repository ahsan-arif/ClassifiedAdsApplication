package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityChat;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Chat;
import com.android.classifiedapp.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    List<Chat> chats;
    Context context;

    public ChatsAdapter(List<Chat> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chats.get(position);

       // Glide.with(context).load(chat.getAd().getUrls().get(0)).into(holder.imgUser);
        //holder.tvProductTitle.setText(chat.getAd().getTitle());
        getAdImageAndTitle(context,chat.getLastMessage().getProductId(),holder.tvProductTitle,holder.imgUser,holder.itemView,holder.tvUsername,chat.getReceiverId());
       //getPostedBy(context,chat.getReceiverId(),holder.tvUsername);
        long time = chat.getLastMessage().getTimestamp();
        Date date = new Date(time);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(ago);
        holder.tvLasMessage.setText(chat.getLastMessage().getMessage());
     /*   holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityChat.class).putExtra("sellerId",chat.getReceiverId()).putExtra("adId",chat.getLastMessage().getProductId()));
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
TextView tvTime,tvProductTitle,tvUsername,tvLasMessage;
ImageView imgUser;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvProductTitle = itemView.findViewById(R.id.tv_productTitle);
            imgUser = itemView.findViewById(R.id.img_user);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvLasMessage = itemView.findViewById(R.id.tv_lasMessage);
        }
    }

    void getAdImageAndTitle(Context context,String adId,TextView tvTitle, ImageView imageView,View itemView,TextView tvPostedBy,String receiverId){
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Ad ad = snapshot.getValue(Ad.class);

                    tvTitle.setText(ad.getTitle());
                    Glide.with(context).load(ad.getUrls().get(0)).into(imageView);
                    //itemView.setOnClickListener(null);
                    getPostedBy(context,ad.getPostedBy(),tvPostedBy);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(context, ActivityChat.class).putExtra("sellerId",receiverId).putExtra("adId",adId));
                        }
                    });

                }else{
                    itemView.setOnClickListener(null);
                    tvTitle.setText(context.getString(R.string.deleted_ad));
                    tvPostedBy.setText(context.getString(R.string.deleted_user));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getPostedBy(Context context, String uid, TextView postedBy){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    postedBy.setText(user.getName());
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
