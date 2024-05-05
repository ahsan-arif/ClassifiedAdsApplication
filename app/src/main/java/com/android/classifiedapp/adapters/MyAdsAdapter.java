package com.android.classifiedapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.ViewHolder> {
ArrayList<Ad> ads;
Context context;

    public MyAdsAdapter(ArrayList<Ad> ads, Context context) {
        this.ads = ads;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_product,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        // holder.tvPostedBy.setText(user.getName());

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAd(ad,context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle,tvPrice,tvPostedOn,tvPostedBy,tvAddress;
        ImageView imgProduct,imgDelete;
        CircleImageView imgUser;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvPostedOn = itemView.findViewById(R.id.tv_postedOn);
            tvPostedBy = itemView.findViewById(R.id.tv_postedBy);
            imgUser = itemView.findViewById(R.id.img_user);
            tvAddress = itemView.findViewById(R.id.tv_address);
            imgDelete = itemView.findViewById(R.id.img_delete);
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

    void removeAd(Ad ad) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Step 1: Remove ad data from Realtime Database
        //databaseRef.child("ads").child(ad.getId()).removeValue();
        // Step 3: Delete images from Firebase Storage
        for (String imageUrl : ad.getUrls()) {
            // Delete image from Firebase Storage using its URL
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Image deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to delete image
                }
            });
        }
        // Step 4: Remove ad's data node from Realtime Database
        databaseRef.child("ads").child(ad.getId()).removeValue();
    }

    void removeAd(Ad ad, Context context) {
        // Display progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting ad...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<String> imageUrls = ad.getUrls();

        // Recursive function to delete images
        deleteImagesRecursive(imageUrls, 0, new OnDeleteImageListener() {
            @Override
            public void onDeleteImageSuccess() {
                // All images deleted successfully, now remove ad from Realtime Database
                databaseRef.child("ads").child(ad.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        removeAdfromList(ad);
                        Toast.makeText(context, "Ad deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to delete ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDeleteImageFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed to delete image: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnDeleteImageListener {
        void onDeleteImageSuccess();
        void onDeleteImageFailure(String errorMessage);
    }

    void deleteImagesRecursive(List<String> imageUrls, int index, OnDeleteImageListener listener) {
        if (index >= imageUrls.size()) {
            // All images deleted successfully
            listener.onDeleteImageSuccess();
            return;
        }

        String imageUrl = imageUrls.get(index);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully, proceed to delete next image
                deleteImagesRecursive(imageUrls, index + 1, listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to delete image
                listener.onDeleteImageFailure(e.getMessage());
            }
        });
    }

    public void removeAdfromList(Ad ad) {
        ads.remove(ad);
        notifyDataSetChanged();
    }
}
