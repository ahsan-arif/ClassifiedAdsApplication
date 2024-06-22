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
import com.android.classifiedapp.models.User;
import com.appbroker.roundedimageview.RoundedImageView;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        long now = System.currentTimeMillis();
        FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Glide.with(context).load(ad.getUrls().get(0)).into(holder.imgProduct);
        holder.tvTitle.setText(ad.getTitle());

        holder.tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityAdDetails.class).putExtra("ad",ad));
            }
        });

        if (ad.getLikedByUsers()!=null){
            if (!ad.getLikedByUsers().isEmpty()){
                if (ad.getLikedByUsers().contains(fIrebaseUser.getUid())){
                    holder.imgLike.setImageResource(R.drawable.heart_red);
                }else{
                    holder.imgLike.setImageResource(R.drawable.heart_black);
                }
            }else{
                holder.imgLike.setImageResource(R.drawable.heart_black);
            }
        }else{
            holder.imgLike.setImageResource(R.drawable.heart_black);
        }

        if (ad.getFeatured().equals("1")){
            if (ad.getExpiresOn()<now){
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
                ad.setFeatured("0");
                ad.setFeaturedOn(0);
                ad.setExpiresOn(0);
                databaseReference.setValue(ad);
            }
        }

        if (ad.getFeatured().equals("1")){
            holder.tvFeatured.setVisibility(View.VISIBLE);
        }else{
            holder.tvFeatured.setVisibility(View.GONE);
        }

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLike(ad,fIrebaseUser.getUid(),holder.getAdapterPosition(),holder.imgLike,false);
            }
        });

        getPostedBy(ad.getPostedBy(),holder.tvFeatured,ad);
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
        ImageView imgProduct,imgLike;
        TextView tvTitle,tvPrice,tvFeatured;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            imgLike = itemView.findViewById(R.id.img_like);
            tvFeatured = itemView.findViewById(R.id.tv_featured);
        }
    }

    private void toggleLike(Ad ad, String currentUserId,int position,ImageView imageView,boolean isWishlist) {
        LogUtils.e(position);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId()).child("likedByUsers");
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    List<String> likedByUsers = new ArrayList<>();
                    likedByUsers.add(currentUserId);
                    postRef.setValue(likedByUsers);
                    ad.setLikedByUsers(likedByUsers);
                    notifyItemChanged(position);
                    imageView.setImageResource(R.drawable.heart_red);
                }else{
                    List<String> likedByUsers = new ArrayList<>();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        String userId = snapshot1.getValue(String.class);
                        LogUtils.e(userId);
                        likedByUsers.add(userId);
                    }
                    if (likedByUsers.contains(currentUserId)){
                        likedByUsers.remove(currentUserId);
                        imageView.setImageResource(R.drawable.heart_black);
                        LogUtils.e("removing");
                        if (isWishlist){
                            removeAd(ad);
                        }
                        postRef.setValue(likedByUsers);
                    }
                    else {
                        likedByUsers.add(currentUserId);
                        imageView.setImageResource(R.drawable.heart_red);
                        LogUtils.e("adding");
                        postRef.setValue(likedByUsers);
                    }
                    ad.setLikedByUsers(likedByUsers);
                    notifyItemChanged(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeAd(Ad ad) {
        ads.remove(ad);
        notifyDataSetChanged();
    }

    void getPostedBy( String uid, TextView tvFeatured,Ad ad){
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
                    user.setBenefitsExpiry(snapshot.child("benefitsExpiry").getValue(Long.class));
                    user.setPremiumUser(snapshot.child("premiumUser").getValue(Boolean.class));
                    if (user.isPremiumUser()){
                        LogUtils.e("premium user");
                        long now = System.currentTimeMillis();
                        if (now<user.getBenefitsExpiry()){
                            tvFeatured.setVisibility(View.VISIBLE);
                        }else{
                            tvFeatured.setVisibility(View.GONE);
                        }
                    }else{
                        LogUtils.e("not premium user");
                        if (ad.getFeatured().equals("1")){
                            tvFeatured.setVisibility(View.VISIBLE);
                        }else{
                            tvFeatured.setVisibility(View.GONE);
                        }
                    }
                    /*postedBy.setText(user.getName());
                    if (snapshot.hasChild("profileImage")){
                        user.setProfileImage(snapshot.child("profileImage").getValue(String.class));
                        Glide.with(context).load(user.getProfileImage()).into(circleImageView);
                    }*/
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
