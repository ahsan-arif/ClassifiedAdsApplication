package com.android.classifiedapp;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Order;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityCheckout extends AppCompatActivity {
    ImageView imgBack;
    TextView tvProduct,tvQuantity,tvBuyerAddress;
    LinearLayout containerAddress;
    PaymentButtonContainer paymentButtonContainer;
    String accessToken;
    int ordersAvailable;
    Ad ad;
    TextView tvTotal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgBack = findViewById(R.id.img_back);
        tvProduct = findViewById(R.id.tv_product);
        tvQuantity = findViewById(R.id.tv_quantity);
        containerAddress = findViewById(R.id.container_address);
        tvBuyerAddress = findViewById(R.id.tv_buyer_address);
        paymentButtonContainer = findViewById(R.id.payment_button_container);
        tvTotal = findViewById(R.id.tv_total);

        String sellerFCMTOken = getIntent().getStringExtra("fcmToken");
        String quantity = getIntent().getStringExtra("quantity");
        String location = getIntent().getStringExtra("location");
        boolean isPremiumUser = getIntent().getBooleanExtra("isPremiumUser",false);
        ordersAvailable = getIntent().getIntExtra("ordersAvailable",0);
        LogUtils.e(ordersAvailable);
        LogUtils.e(isPremiumUser);
        LogUtils.e(location);
        LogUtils.e(quantity);
        ad = getIntent().getParcelableExtra("ad");
        accessToken = SharedPrefManager.getInstance(ActivityCheckout.this).getAccessToken();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvQuantity.setText(quantity);
        tvProduct.setText(ad.getTitle());
        Double total = Double.parseDouble(quantity) * Double.parseDouble(ad.getPrice());
        tvTotal.setText(ad.getCurrency()+" "+total);
        if (!ad.isShippingAvailable()){
            containerAddress.setVisibility(View.GONE);
        }else{
            tvBuyerAddress.setText(location);
        }

        paymentButtonContainer.setup( new CreateOrder() {
            @Override
            public void create(@NotNull CreateOrderActions createOrderActions) {
                LogUtils.e("create: ");
                ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                purchaseUnits.add(
                        new PurchaseUnit.Builder()
                                .amount(
                                        new Amount.Builder()
                                                .currencyCode(CurrencyCode.USD)
                                                .value(String.valueOf(total))
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
                        ToastUtils.showShort( getString(R.string.payment_successful), Toast.LENGTH_SHORT);

                        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference databaseReference=  FirebaseDatabase.getInstance().getReference().child("orders").push();
                        String key = databaseReference.getKey();
                        Order order = new Order();
                        order.setAmount(Double.valueOf(quantity)*Double.valueOf(ad.getPrice()));
                        order.setBuyerId(uid);
                        order.setSellerId(ad.getPostedBy());
                        order.setProductId(ad.getId());
                        order.setTitle(ad.getTitle());
                        order.setQuantity(Integer.parseInt(quantity));
                        order.setStatus(getString(R.string.paid));
                        if (!location.isEmpty()){
                            order.setAddress(location);
                        }
                        order.setCurrency(ad.getCurrency());
                        order.setPlaceOn(String.valueOf(System.currentTimeMillis()));
                        order.setId(key);

                        databaseReference.setValue(order);

                /*        DatabaseReference productReference=  FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId()).child("orders").child(key);
                        productReference.setValue(order);*/

                        if (!isPremiumUser){
                            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(ad.getPostedBy()).child("maximumOrdersAvailable");
                            ordersAvailable = ordersAvailable-1;
                            userReference.setValue(ordersAvailable);
                        }

                        try {
                            sendPushNotification(getString(R.string.new_order),getString(R.string.somebody_placed),sellerFCMTOken);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        startActivity(new Intent(ActivityCheckout.this, Home.class).putExtra("order",order));
                        finish();
                    }
                });
            }
        });

    }
    void sendPushNotification(String title,String body,String sellerFCMTOken) throws JSONException {
        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",sellerFCMTOken);

        JSONObject dataObject = new JSONObject();
        dataObject.put("adId",ad.getId());
        dataObject.put("title",ad.getTitle());
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/user:"+ad.getId()+"_"+ad.getTitle());
        messageObject.put("data",dataObject);

        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action","com.android.classifiedapp.ActivityViewOrders");

        androidObject.put("notification",activityNotificationObject);
        messageObject.put("android",androidObject);

        JSONObject finalObject = new JSONObject();
        finalObject.put("message",messageObject);
        //finalObject.put("data",dataObject);
        LogUtils.json(finalObject);

// Create a new RequestQueue
        RequestQueue queue = Volley.newRequestQueue(ActivityCheckout.this);

// Create a new JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NOTIFICATION_URL, finalObject,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response from the FCM server
                        //LogUtils.json(response);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        LogUtils.e(error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
// Add the request to the RequestQueue
        queue.add(request);
        //  This code will send a push notification to the device with the title "New Like!" and the body "Someone has liked your post!".
        //I hope this helps! Let me know if you have any other questions.
    }
}