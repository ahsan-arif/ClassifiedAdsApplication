package com.android.classifiedapp;

import android.app.ProgressDialog;
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
import com.android.classifiedapp.models.Order;
import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityMyOrders extends AppCompatActivity {
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
        getOrders(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

    void setAdapter(ArrayList<Order> orders){
        OrdersAdapter adapter = new OrdersAdapter(ActivityMyOrders.this,orders,true);
        rvOrders.setAdapter(adapter);
        rvOrders.setLayoutManager(new LinearLayoutManager(ActivityMyOrders.this));
    }
}