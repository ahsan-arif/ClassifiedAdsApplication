package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.android.classifiedapp.GalleryActivity;
import com.android.classifiedapp.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter  extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    Context context;
    List<String> imageUrls;
    boolean isGallery;

    public ImagePagerAdapter(Context context, List<String> imageUrls,boolean isGallery) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isGallery = isGallery;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.img_ad,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(context).load(url).into(holder.imageView);
        ArrayList<String> urls = new ArrayList<>(imageUrls);
        if (!isGallery){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, GalleryActivity.class).putStringArrayListExtra("imageUrls",urls));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;

    ImageViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.img_ad);
    }
}
}
