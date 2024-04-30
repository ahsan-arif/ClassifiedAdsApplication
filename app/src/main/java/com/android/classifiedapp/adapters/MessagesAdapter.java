package com.android.classifiedapp.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {
    ArrayList<Message> messages;
    Context context;

    public MessagesAdapter(ArrayList<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessagesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        String currentUserId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        Message message = messages.get(position);
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageTextView.getLayoutParams();
        if (isCurrentUser) {
            // If the message is from the current user, align it to the right
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0); // Remove previous rule if any
            holder.messageTextView.setBackgroundResource(R.drawable.bg_chat_message_sender);
            layoutParams.setMarginStart(64); // Add margin to the left for sender
            layoutParams.setMarginEnd(0);
        } else {
            // If the message is from another user, align it to the left
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0); // Remove previous rule if any
            holder.messageTextView.setBackgroundResource(R.drawable.bg_chat_message_receiver);
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd(64); // Add margin to the right for receiver
        }

        holder.messageTextView.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessagesViewHolder extends RecyclerView.ViewHolder{
        TextView messageTextView;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}
