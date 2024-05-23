package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.GalleryActivity;
import com.android.classifiedapp.R;
import com.android.classifiedapp.views.ZoomView;
import com.bumptech.glide.Glide;
import com.otaliastudios.zoom.ZoomImageView;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<String> imageUrls;
    boolean isGallery;

    public ImagePagerAdapter(Context context, List<String> imageUrls, boolean isGallery) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isGallery = isGallery;
    }

    @Override
    public int getItemViewType(int position) {
        return isGallery ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.img_ad, parent, false));
        } else {
            return new PinchZoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zoomable_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String url = imageUrls.get(position);
        ArrayList<String> urls = new ArrayList<>(imageUrls);

        if (holder.getItemViewType() == 0) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            Glide.with(context).load(url).into(imageViewHolder.imageView);
            imageViewHolder.imageView.setOnClickListener(v -> context.startActivity(new Intent(context, GalleryActivity.class).putStringArrayListExtra("imageUrls", urls)));
        } else {
            PinchZoomViewHolder pinchZoomViewHolder = (PinchZoomViewHolder) holder;
            Glide.with(context).load(url).into(pinchZoomViewHolder.zoomImageView);
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

    static class PinchZoomViewHolder extends RecyclerView.ViewHolder {
        ImageView zoomImageView;
        ZoomView zoomView;
        PinchZoomViewHolder(View itemView) {
            super(itemView);
            zoomImageView = itemView.findViewById(R.id.img_zoom);
            zoomView = itemView.findViewById(R.id.zoomView);
            zoomView.setMaxZoom(2.5f);
        }
    }
}
