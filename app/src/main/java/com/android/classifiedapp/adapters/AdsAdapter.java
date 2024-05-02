package com.android.classifiedapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.ProductsViewHolder> {
    ArrayList<Ad> ads;
    Context context;
    OnAdClickListener listener;

    public AdsAdapter(ArrayList<Ad> ads, Context context,OnAdClickListener listener) {
        this.ads = ads;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsViewHolder holder, int position) {

        final boolean isLiked = false ;
        FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();
Ad ad = ads.get(position);
holder.tvTitle.setText(ad.getTitle());
holder.tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
holder.tvAddress.setText(ad.getAddress());
        Glide.with(context).load(ad.getUrls().get(0)).into(holder.imgProduct);
String posted = context.getString(R.string.posted);
long timestamp = Long.parseLong(ad.getPostedOn());
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.tvPostedOn.setText(posted+" "+ago);

        getPostedBy(context,ad.getPostedBy(),holder.tvPostedBy,holder.imgUser);

        if (ad.getLikedByUsers()!=null){
            if (!ad.getLikedByUsers().isEmpty()){
                if (ad.getLikedByUsers().contains(fIrebaseUser.getUid())){
                    holder.imgLike.setImageResource(R.drawable.heart_red);
                }else{
                    holder.imgLike.setImageResource(R.drawable.heart);
                }
            }else{
                holder.imgLike.setImageResource(R.drawable.heart);
            }
        }else{
            holder.imgLike.setImageResource(R.drawable.heart);
        }

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             toggleLike(ad,fIrebaseUser.getUid(),holder.getAdapterPosition(),holder.imgLike);
            }
        });

       // holder.tvPostedBy.setText(user.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityAdDetails.class).putExtra("ad",ad));
            }
        });

    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    class ProductsViewHolder extends RecyclerView.ViewHolder{
TextView tvTitle,tvPrice,tvPostedOn,tvPostedBy,tvAddress;
ImageView imgProduct,imgLike;
CircleImageView imgUser;
        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvPostedOn = itemView.findViewById(R.id.tv_postedOn);
            tvPostedBy = itemView.findViewById(R.id.tv_postedBy);
            imgUser = itemView.findViewById(R.id.img_user);
            imgLike = itemView.findViewById(R.id.img_like);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }

    void getPostedBy(Context context, String uid, TextView postedBy, CircleImageView circleImageView
    ){
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
                        postedBy.setText(user.getName());
                        if (snapshot.hasChild("profileImage")){
                            user.setProfileImage(snapshot.child("profileImage").getValue(String.class));
                            Glide.with(context).load(user.getProfileImage()).into(circleImageView);
                        }
                   // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface OnAdClickListener{
        void onLikeClicked(Ad ad, ImageView imageView);
    }

    private void toggleLike(Ad ad, String currentUserId,int position,ImageView imageView) {
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
                    imageView.setImageResource(R.drawable.heart);
                    LogUtils.e("removing");
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
}
