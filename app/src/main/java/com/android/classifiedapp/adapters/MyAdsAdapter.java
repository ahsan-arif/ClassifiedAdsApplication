package com.android.classifiedapp.adapters;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.ActivityEditAd;
import com.android.classifiedapp.ActivityVerifyLogin;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.PlatformPrefs;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
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
import com.paypal.android.corepayments.CoreConfig;
import com.paypal.android.corepayments.Environment;
import com.paypal.android.corepayments.PayPalSDKError;
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient;
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener;
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest;
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.cancel.OnCancel;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.error.ErrorInfo;
import com.paypal.checkout.error.OnError;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.ViewHolder> {
    ArrayList<Ad> ads;
    Context context;

    boolean unApprovedAd;
    boolean isPremiumUser;
    int freeAdsAvailable;

    Application application;
    PaymentButtonClickListener listener;

    public MyAdsAdapter(ArrayList<Ad> ads, Context context,Application application,PaymentButtonClickListener listener) {
        this.ads = ads;
        this.context = context;
        this.application = application;
        this.listener = listener;
    }

    public MyAdsAdapter(ArrayList<Ad> ads, Context context, boolean unApprovedAd,Application application,PaymentButtonClickListener listener) {
        this.ads = ads;
        this.context = context;
        getUser(ads.get(0));
        this.unApprovedAd = unApprovedAd;
        this.application = application;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_product,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ad ad = ads.get(position);
        final String[] featureAdFee = new String[1];
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

        if (ad.getExpiresOn()!=0){
            if (now>ad.getExpiresOn()){
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
                ad.setFeatured("0");
                ad.setFeaturedOn(0);
                ad.setExpiresOn(0);
                databaseReference.setValue(ad);
                holder.paymentButtonContainer.setVisibility(View.VISIBLE);
                holder.tvFeatured.setVisibility(View.GONE);
            }
        }

        if (ad.getFeatured()!=null){
            if (ad.getFeatured().equals("1")){
                holder.paymentButtonContainer.setVisibility(View.GONE);
                holder.tvFeatured.setVisibility(View.VISIBLE);
            }else {
                FirebaseDatabase.getInstance().getReference().child("platform_prefs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            holder.paymentButtonContainer.setVisibility(View.VISIBLE);
                            PlatformPrefs prefs = snapshot.getValue(PlatformPrefs.class);
                           // holder.paymentButtonContainer.setPaypalButtonLabel(context.getString(R.string.to_make_featured_for_24h)+" "+ad.getCurrency()+" "+prefs.getFeaturedAdFee());
                            featureAdFee[0] =prefs.getFeaturedAdFee();
                        }else{
                            holder.paymentButtonContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.tvFeatured.setVisibility(View.GONE);
            }
        }


        getPostedBy(context,ad.getPostedBy(),holder.tvPostedBy,holder.imgUser);

        // holder.tvPostedBy.setText(user.getName());

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(context.getString(R.string.are_you_sure));

                builder.setMessage(context.getString(R.string.do_you_want_to_del_ad));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ToastUtils.showShort("yes");
                        removeAd(ad,context);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityEditAd.class).putExtra("ad",ad)
                        .putExtra("unApprovedAd",unApprovedAd));
            }
        });

        //  holder.tvMakeFeatured.setOnClickListener(new View.OnClickListener() {
        //   @Override
        //   public void onClick(View v) {
        //mark ad as featured in the database
/*                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
                ad.setFeatured("1");
                // Get the current time in milliseconds
                long currentTimeMillis = System.currentTimeMillis();
// Calculate 24 hrs in milliseconds (1440 minutes * 60 seconds * 1000 milliseconds)
                long twentyFourHours = 1440 * 60 * 1000; //24hrs
// Add 5 minutes to the current time
                long newTimeMillis = currentTimeMillis + twentyFourHours;
                ad.setFeaturedOn(currentTimeMillis);
                ad.setExpiresOn(newTimeMillis);
                databaseReference.setValue(ad);
                holder.tvMakeFeatured.setVisibility(View.GONE);
                holder.tvFeatured.setVisibility(View.VISIBLE);*/


        //makePayment(ad.getId(),application);

        //    }
        //  });

       /* holder.paymentButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked(ad,featureAdFee[0],holder.paymentButtonContainer);
            }
        });*/

        holder.paymentButtonContainer.setup( new CreateOrder() {
                                                 @Override
                                                 public void create(@NotNull CreateOrderActions createOrderActions) {
                                                     LogUtils.e("create: ");
                                                     ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                                                     purchaseUnits.add(
                                                             new PurchaseUnit.Builder()
                                                                     .amount(
                                                                             new Amount.Builder()
                                                                                     .currencyCode(CurrencyCode.USD)
                                                                                     .value(featureAdFee[0])
                                                                                     .build()
                                                                     )
                                                                     .build()
                                                     );
                                                     OrderRequest order = new OrderRequest(
                                                             OrderIntent.CAPTURE,
                                                             new AppContext.Builder()
                                                                     .userAction(UserAction.PAY_NOW)
                                                                     .build(),
                                                             purchaseUnits
                                                     );
                                                     createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                                                 }
                                             }, new OnApprove() {
                    @Override
                    public void onApprove(@NotNull Approval approval) {
                        approval.getOrderActions().capture(new OnCaptureComplete() {
                            @Override
                            public void onCaptureComplete(@NotNull CaptureOrderResult result) {
                                LogUtils.e(String.format("CaptureOrderResult: %s", result));
                                ToastUtils.showShort( "Successful", Toast.LENGTH_SHORT);
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
                                ad.setFeatured("1");
                                // Get the current time in milliseconds
                                long currentTimeMillis = System.currentTimeMillis();
// Calculate 24 hrs in milliseconds (1440 minutes * 60 seconds * 1000 milliseconds)
                                long twentyFourHours = 1440 * 60 * 1000; //24hrs
// Add 5 minutes to the current time
                                long newTimeMillis = currentTimeMillis + twentyFourHours;
                                ad.setFeaturedOn(currentTimeMillis);
                                ad.setExpiresOn(newTimeMillis);
                                databaseReference.setValue(ad);
                                holder.paymentButtonContainer.setVisibility(View.GONE);
                                holder.tvFeatured.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle,tvPrice,tvPostedOn,tvPostedBy,tvAddress,tvMakeFeatured,tvFeatured;
        ImageView imgProduct,imgDelete,imgEdit;
        CircleImageView imgUser;
        PaymentButtonContainer paymentButtonContainer;
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
            imgEdit = itemView.findViewById(R.id.img_edit);
            // tvMakeFeatured = itemView.findViewById(R.id.tv_make_featured);
            tvFeatured  = itemView.findViewById(R.id.tv_featured);
            paymentButtonContainer = itemView.findViewById(R.id.payment_button_container);
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

                        if (!isPremiumUser){
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(ad.getPostedBy()).child("freeAdsAvailable");
                            freeAdsAvailable = freeAdsAvailable+1;
                            databaseReference.setValue(freeAdsAvailable);
                        }

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

    public interface OnDeleteImageListener {
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

    void getUser(Ad ad){
        FirebaseDatabase.getInstance().getReference().child("users").child(ad.getPostedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = new User();
                    isPremiumUser = snapshot.child("premiumUser").getValue(Boolean.class);
                    if (!isPremiumUser)
                        freeAdsAvailable = snapshot.child("freeAdsAvailable").getValue(Integer.class);
                    else{
                        freeAdsAvailable = 0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void makePayment(String adId,Application application){
        String clientID = "AQlOefvrxsESdnd_UV3h8C70_e7XRACjk5ODy_NJZUBAyygNaXKeYipCcZNU_tYTQvgZkfZWR2rcLvNQ";
        String returnUrl = "com.android.classifiedapp://paypalpay";
        CoreConfig coreConfig = new CoreConfig(clientID, Environment.SANDBOX);
        PayPalNativeCheckoutClient client = new PayPalNativeCheckoutClient(application,coreConfig,returnUrl);
        client.setListener(new PayPalNativeCheckoutListener() {
            @Override
            public void onPayPalCheckoutStart() {
                LogUtils.e("starting payment process");
            }

            @Override
            public void onPayPalCheckoutSuccess(@NonNull PayPalNativeCheckoutResult payPalNativeCheckoutResult) {
                LogUtils.e(payPalNativeCheckoutResult.getPayerId());
            }

            @Override
            public void onPayPalCheckoutFailure(@NonNull PayPalSDKError payPalSDKError) {
                LogUtils.e(payPalSDKError.getMessage());
            }

            @Override
            public void onPayPalCheckoutCanceled() {
                LogUtils.e("operation cancelled");
            }
        });
        LogUtils.e("order"+System.currentTimeMillis());
        client.startCheckout(new PayPalNativeCheckoutRequest("order"+System.currentTimeMillis()));
    }

    public interface PaymentButtonClickListener{
        void onButtonClicked(Ad ad, String amount,PaymentButtonContainer container);
    }
}
