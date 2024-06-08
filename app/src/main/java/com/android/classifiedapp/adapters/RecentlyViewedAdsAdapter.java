package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.appbroker.roundedimageview.RoundedImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecentlyViewedAdsAdapter extends RecyclerView.Adapter<RecentlyViewedAdsAdapter.ViewHolder> {
    Context context;
    ArrayList<Ad> ads;

    public RecentlyViewedAdsAdapter(Context context, ArrayList<Ad> ads) {
        this.context = context;
        this.ads = ads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_viewed_ad,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ad ad = ads.get(position);

        Glide.with(context).load(ad.getUrls().get(0)).into(holder.imgProduct);
        holder.tvTitle.setText(ad.getTitle());

        holder.tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityAdDetails.class).putExtra("ad",ad));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (ads.size()>5)
        return 5;
        else {
            return ads.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView imgProduct;
        TextView tvTitle,tvPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
