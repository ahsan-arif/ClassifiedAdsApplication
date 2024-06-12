package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityCustomerSupportChat;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminsAdapter extends RecyclerView.Adapter<AdminsAdapter.AdminHolder> {
    Context context;
    ArrayList<User> admins;

    public AdminsAdapter(Context context, ArrayList<User> admins) {
        this.context = context;
        this.admins = admins;
    }

    @NonNull
    @Override
    public AdminHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdminHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_support,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdminHolder holder, int position) {
        User admin = admins.get(position);

        if (admin.getProfileImage()!=null){
            Glide.with(context).load(admin.getProfileImage()).into(holder.imgAdmin);
        }else{
            holder.imgAdmin.setImageResource(R.drawable.outline_account_circle_24);
        }
        holder.tvAdminName.setText(admin.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e(admin.getId());
                context.startActivity(new Intent(context, ActivityCustomerSupportChat.class).putExtra("id",admin.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return admins.size();
    }

    class AdminHolder extends RecyclerView.ViewHolder{
        CircleImageView imgAdmin;
        TextView tvAdminName;
        TextView tvLastMessage;
        TextView tvTime;
        public AdminHolder(@NonNull View itemView) {
            super(itemView);
            imgAdmin = itemView.findViewById(R.id.img_admin);
            tvAdminName = itemView.findViewById(R.id.tv_admin_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
