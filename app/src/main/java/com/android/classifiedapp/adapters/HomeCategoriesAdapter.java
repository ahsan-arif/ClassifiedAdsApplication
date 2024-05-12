package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.AdsByCategory;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Category;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeCategoriesAdapter extends RecyclerView.Adapter<HomeCategoriesAdapter.HomeCategoriesViewHolder> {
List<Category> categories;
Context context;

    public HomeCategoriesAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeCategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeCategoriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCategoriesViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvCategory.setText(category.getName());
        //LogUtils.e(category.getImageUrl());
        Glide.with(context).load(category.getImageUrl()).into(holder.imgCategory);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, AdsByCategory.class).putExtra("category",category));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class HomeCategoriesViewHolder extends RecyclerView.ViewHolder{
        TextView tvCategory;
        CircleImageView imgCategory;
        public HomeCategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            imgCategory = itemView.findViewById(R.id.img_category);

        }
    }
}
