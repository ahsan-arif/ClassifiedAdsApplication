package com.android.classifiedapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.OrdersAdapter;
import com.android.classifiedapp.models.Order;
import com.android.classifiedapp.models.Rating;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityMyOrders extends AppCompatActivity implements OrdersAdapter.RateButtonClickListener {
ImageView imgBack;
RecyclerView rvOrders;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgBack = findViewById(R.id.img_back);
        rvOrders = findViewById(R.id.rv_orders);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //getOrders(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getOrdersNew(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    void getOrders(String userId){
        ProgressDialog progressDialog = new ProgressDialog(ActivityMyOrders.this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_orders));
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()){
                    ArrayList<Order> orders = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Order order = dataSnapshot.getValue(Order.class);
                        orders.add(order);
                    }

                    setAdapter(orders);
                }else{
                    ToastUtils.showShort(getString(R.string.no_orders_placed));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }

    void getOrdersNew(String userId){
        ProgressDialog progressDialog = new ProgressDialog(ActivityMyOrders.this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_orders));
        progressDialog.show();
        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference().child("orders");
        databaseReference.orderByChild("buyerId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if(snapshot.exists()){
                    try {
                        ArrayList<Order> orders = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Order order = dataSnapshot.getValue(Order.class);
                            orders.add(order);
                        }

                        setAdapter(orders);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
progressDialog.dismiss();
            }
        });
    }

    void setAdapter(ArrayList<Order> orders){
        OrdersAdapter adapter = new OrdersAdapter(ActivityMyOrders.this,orders,this);
        rvOrders.setAdapter(adapter);
        rvOrders.setLayoutManager(new LinearLayoutManager(ActivityMyOrders.this));
    }

    @Override
    public void onRateButtonClicked(Order order) {
        showRateBottomSheet(order);
    }

    void showRateBottomSheet(Order order){
        BottomSheetDialog rateDialog = new BottomSheetDialog(ActivityMyOrders.this);
        rateDialog.setContentView(R.layout.bottom_sheet_dialog_rate_seller_product);
        AppCompatRatingBar ratingSeller = rateDialog.findViewById(R.id.rating_seller);
        AppCompatRatingBar ratingProduct = rateDialog.findViewById(R.id.rating_product);
        TextView btnSubmitRating = rateDialog.findViewById(R.id.btn_submit_rating);
        final float[] sellerRating = new float[1];
        final float[] productRating = new float[1];
        ratingSeller.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                sellerRating[0] = rating;
                LogUtils.e(sellerRating[0]);
            }
        });
        ratingProduct.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                productRating[0] = rating;
                LogUtils.e(productRating[0] );
            }
        });

        btnSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference().child("ratings").push();
                Rating rating = new Rating();
                rating.setProductRating(productRating[0]);
                rating.setSellerRating(sellerRating[0]);
                rating.setRatedBy(order.getBuyerId());
                rating.setProductId(order.getProductId());
                rating.setOrderId(order.getId());
                rating.setSellerId(order.getSellerId());
                rating.setRatedOn(String.valueOf(System.currentTimeMillis()));
                ratingRef.setValue(rating);


                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference().child("orders").child(order.getId());

                Map<String,Object> updates = new HashMap<>();
                updates.put("rated",true);

                orderRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ToastUtils.showShort(getString(R.string.your_feedback_recorded));
                        rateDialog.dismiss();
                    }
                });
            }
        });
        rateDialog.show();
    }
}