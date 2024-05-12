package com.android.classifiedapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.models.Order;
import com.android.classifiedapp.models.Rating;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityPaymentSuccessful extends AppCompatActivity {
    TextView tvProductQuan,tvCurTotal;
    RatingBar ratingbar;
    float rating1;

    TextView btnDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200)); // Replace R.color.your_status_bar_color with your desired color resource
        }
        setContentView(R.layout.activity_payment_successful);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Order order = getIntent().getParcelableExtra("order");
        tvProductQuan = findViewById(R.id.tv_product_quan);
        tvCurTotal = findViewById(R.id.tv_cur_total);
        ratingbar = findViewById(R.id.ratingbar);
        btnDone = findViewById(R.id.btn_done);

        tvProductQuan.setText(order.getTitle()+" "+"x"+order.getQuantity());;
        tvCurTotal.setText(order.getCurrency()+" "+order.getAmount());

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating1 = rating;
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e(order.getSellerId());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(order.getSellerId()).child("ratings").push();
                Rating r = new Rating();
                r.setRating(rating1);
                r.setRatedBy(order.getBuyerId());
                r.setProductId(order.getProductId());
                r.setRatedOn(String.valueOf(System.currentTimeMillis()));

                databaseReference.setValue(r);

                ToastUtils.showShort(getString(R.string.thank_you_feedback));
                startActivity(new Intent(ActivityPaymentSuccessful.this, Home.class));
                finish();
            }
        });
    }
}