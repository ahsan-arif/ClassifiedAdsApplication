package com.android.classifiedapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.ProductsViewHolder> {
    ArrayList<Ad> ads;
    Context context;

    public AdsAdapter(ArrayList<Ad> ads, Context context) {
        this.ads = ads;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsViewHolder holder, int position) {
Ad ad = ads.get(position);
holder.tvTitle.setText(ad.getTitle());
holder.tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
        Glide.with(context).load(ad.getUrls().get(0)).into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    class ProductsViewHolder extends RecyclerView.ViewHolder{
TextView tvTitle,tvPrice;
ImageView imgProduct;
        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgProduct = itemView.findViewById(R.id.img_product);
        }
    }
}
