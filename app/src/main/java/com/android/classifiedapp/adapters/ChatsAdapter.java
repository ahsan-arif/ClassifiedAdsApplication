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

import com.android.classifiedapp.ActivityChat;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Chat;
import com.bumptech.glide.Glide;

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
        if (chat.getReceiver().getProfileImage()!=null){
            Glide.with(context).load(chat.getReceiver().getProfileImage()).into(holder.imgUser);
        }else{
            holder.imgUser.setImageResource(R.drawable.outline_account_circle_24);
        }
        holder.tvLasMessage.setText(chat.getLastMessage().getMessage());
        holder.tvUsername.setText(chat.getReceiver().getName());
        long time = chat.getLastMessage().getTimestamp();
        Date date = new Date(time);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(ago);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityChat.class).putExtra("sellerId",chat.getReceiverId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
TextView tvTime,tvLasMessage,tvUsername;
CircleImageView imgUser;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLasMessage = itemView.findViewById(R.id.tv_lasMessage);
            imgUser = itemView.findViewById(R.id.img_user);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }
    }
}
