package com.android.classifiedapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.OrdersAdapter;
import com.android.classifiedapp.adapters.ProductOrdersAdapter;
import com.android.classifiedapp.models.Order;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityViewOrders extends AppCompatActivity {
    RecyclerView rvOrders;
    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvOrders = findViewById(R.id.rv_orders);
        imgBack = findViewById(R.id.img_back);
        String adId,title;
        adId = getIntent().getStringExtra("adId");
        title = getIntent().getStringExtra("title");

        LogUtils.e(adId);
        LogUtils.e(title);

        Bundle extras = getIntent().getExtras();
        LogUtils.e(extras);
        //getOrders(adId,title);
        getOrdersNew(adId,title);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extras!=null){
                    startActivity(new Intent(ActivityViewOrders.this, Home.class));
                }
                finish();
            }
        });
    }

    void getOrders(String adId,String title){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(adId).child("orders");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
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
                }else{
                    ToastUtils.showShort(getString(R.string.no_orders_for_listing));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }});
    }

    void getOrdersNew(String adId, String productTitle){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("orders");
        databaseReference.orderByChild("productId").equalTo(adId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
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

            }
        });
    }

    void setAdapter(ArrayList<Order> orders){
        ProductOrdersAdapter adapter = new ProductOrdersAdapter(ActivityViewOrders.this,orders);
        rvOrders.setAdapter(adapter);
        rvOrders.setLayoutManager(new LinearLayoutManager(ActivityViewOrders.this));
    }
}