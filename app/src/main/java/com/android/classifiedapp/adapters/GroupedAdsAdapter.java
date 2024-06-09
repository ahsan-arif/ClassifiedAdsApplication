package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.AdsByCategory;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.GroupedItem;
import com.appbroker.roundedimageview.RoundedImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupedAdsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GroupedItem> groupedItemList;
Context context;
    public GroupedAdsAdapter(List<GroupedItem> groupedItemList,Context context) {
        this.groupedItemList = groupedItemList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return groupedItemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GroupedItem.TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_header, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_viewed_ad, parent, false);
            return new AdViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupedItem groupedItem = groupedItemList.get(position);
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(groupedItem.getCategory(),groupedItem.getAd(),context);
        } else if (holder instanceof AdViewHolder) {
            ((AdViewHolder) holder).bind(groupedItem.getAd().get(0),context);
        }
    }

    @Override
    public int getItemCount() {
        return groupedItemList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryNameTextView;
        CircleImageView imgCategory;
        RecyclerView rvAds;
        TextView tvSeeAll;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.tv_category);
            imgCategory = itemView.findViewById(R.id.img_category);
            rvAds = itemView.findViewById(R.id.rv_ads);
            tvSeeAll = itemView.findViewById(R.id.tv_see_all);
        }

        public void bind(Category category, ArrayList<Ad> ads, Context context) {
            categoryNameTextView.setText(category.getName());
            Glide.with(context).load(category.getImageUrl()).into(imgCategory);
            rvAds.setAdapter(new RecentlyViewedAdsAdapter(context,ads));
            rvAds.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));

            if (ads.size()>5){
                tvSeeAll.setVisibility(View.VISIBLE);
            }else{
                tvSeeAll.setVisibility(View.GONE);
            }

            tvSeeAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, AdsByCategory.class).putExtra("category",category));
                }
            });
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        ImageView imgProduct;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            imgProduct = itemView.findViewById(R.id.img_product);
        }

        public void bind(Ad ad,Context context) {
            titleTextView.setText(ad.getTitle());
            Glide.with(context).load(ad.getUrls().get(0)).into(imgProduct);
        }
    }
}