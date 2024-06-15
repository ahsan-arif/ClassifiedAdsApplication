package com.android.classifiedapp.adapters;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.ActivityChat;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Order;
import com.android.classifiedapp.models.User;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    Context context;
    ArrayList<Order> orders;

    String productTitle;

    RateButtonClickListener listener;

    public OrdersAdapter(Context context, ArrayList<Order> orders,RateButtonClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvTitle.setText(order.getTitle());
        holder.tvStatus.setText(context.getString(R.string.status)+": "+order.getStatus());
        holder.tvTotal.setText(order.getCurrency()+" "+order.getAmount());
        holder.tvQuantity.setText(context.getString(R.string.quantity)+": "+String.valueOf(order.getQuantity()));
        holder.tvOrderId.setText(order.getId());

        String paid = context.getString(R.string.paid);
        String orderConfirmed = context.getString(R.string.order_confirmed);
        String shipped = context.getString(R.string.shipped);
        String received = context.getString(R.string.received);
        String delivered = context.getString(R.string.delivered);

        String status = order.getStatus();
        long timestamp=0;
        if (status.equals(paid)){
            timestamp = Long.parseLong(order.getPlaceOn());
        }else if (status.equals(orderConfirmed)){
            timestamp = Long.parseLong(order.getConfirmedOn());
        }else if (status.equals(shipped)){
            timestamp = Long.parseLong(order.getShippedOn());
        }else if (status.equals(received)){
            timestamp = Long.parseLong(order.getPickedOn());
        }else if (status.equals(delivered)){
            timestamp = Long.parseLong(order.getDeliveredOn());
        }
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.tvOrderedOn.setText(ago);
        LogUtils.e(order.getStatus());

            if (order.getStatus().equals(context.getString(R.string.order_confirmed))){
                if (order.getAddress()!=null){
                    holder.tvInstructions.setText(context.getString(R.string.preparing_order));
                    holder.tvInstructions.setVisibility(View.VISIBLE);
                    holder.vgConfirm.setVisibility(View.GONE);
                }else{
                    holder.tvInstructions.setText(context.getString(R.string.contact_seller_for_pickup_spot));
                    holder.tvInstructions.setVisibility(View.VISIBLE);
                    holder.vgConfirm.setVisibility(View.VISIBLE);
                    holder.tvConfirmPickup.setVisibility(View.VISIBLE);
                    holder.tvConfirmDelivery.setVisibility(View.GONE);
                    holder.tvRate.setVisibility(View.GONE);
                }
            }else if (order.getStatus().equals(context.getString(R.string.shipped))){
                holder.tvInstructions.setText(context.getString(R.string.confirm_product_received));
                holder.tvInstructions.setVisibility(View.VISIBLE);
                holder.vgConfirm.setVisibility(View.VISIBLE);
                holder.tvConfirmPickup.setVisibility(View.GONE);
                holder.tvConfirmDelivery.setVisibility(View.VISIBLE);
            }else if (order.getStatus().equals(context.getString(R.string.delivered))||order.getStatus().equals(context.getString(R.string.received))){
                holder.tvInstructions.setVisibility(View.GONE);
                holder.vgConfirm.setVisibility(View.VISIBLE);
                holder.tvConfirmPickup.setVisibility(View.GONE);
                holder.tvContactSeller.setVisibility(View.GONE);
                holder.tvConfirmDelivery.setVisibility(View.GONE);
                if (!order.isRated()){
                    holder.tvRate.setVisibility(View.VISIBLE);
                }else{
                    holder.tvRate.setVisibility(View.GONE);
                }
            }

        if (order.getAddress()!=null){
            holder.tvAddress.setText(order.getAddress());
            holder.tvAddress.setVisibility(View.VISIBLE);
        }else{
            holder.tvAddress.setVisibility(View.GONE);
        }

        LogUtils.e(order.getBuyerId());
        LogUtils.e(FirebaseAuth.getInstance().getCurrentUser().getUid());

        holder.tvContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ActivityChat.class)
                        .putExtra("sellerId",order.getSellerId())
                        .putExtra("adId",order.getProductId()));
            }
        });
        holder.tvConfirmPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference orderReference=  FirebaseDatabase.getInstance().getReference().child("orders").child(order.getId());
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", context.getString(R.string.received));
                updates.put("pickedOn",String.valueOf(System.currentTimeMillis()));

                orderReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.vgConfirm.setVisibility(View.VISIBLE);
                        holder.tvConfirmDelivery.setVisibility(View.GONE);
                        holder.tvContactSeller.setVisibility(View.GONE);
                        holder.tvConfirmPickup.setVisibility(View.GONE);
                        holder.tvRate.setVisibility(View.VISIBLE);
                        holder.tvInstructions.setVisibility(View.GONE);
                    }
                });
                getUserFcm(order.getSellerId(),context.getString(R.string.item_received),context.getString(R.string.buyer_confirm_received),order.getProductId(),order.getTitle());
            }
        });

        holder.tvConfirmDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference orderReference=  FirebaseDatabase.getInstance().getReference().child("orders").child(order.getId());
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", context.getString(R.string.delivered));
                updates.put("deliveredOn",String.valueOf(System.currentTimeMillis()));

                orderReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.vgConfirm.setVisibility(View.VISIBLE);
                        holder.tvConfirmDelivery.setVisibility(View.GONE);
                        holder.tvContactSeller.setVisibility(View.GONE);
                        holder.tvConfirmPickup.setVisibility(View.GONE);
                        holder.tvRate.setVisibility(View.VISIBLE);
                        holder.tvInstructions.setVisibility(View.GONE);
                    }
                });
                //get fcm token and send notification
                getUserFcm(order.getSellerId(),context.getString(R.string.item_shipped),context.getString(R.string.confirm_delivery_product),order.getProductId(),order.getTitle());
            }
        });

        holder.tvRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRateButtonClicked(order);
            }
        });
        getOrderedBy(context,order.getSellerId(),holder.tvUser,holder.imgUser);

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle,tvQuantity,tvTotal,tvStatus,tvUser,tvOrderedOn,tvAddress,tvOrderId,tvInstructions,tvConfirmDelivery,tvConfirmPickup,tvContactSeller,tvRate;
        CircleImageView imgUser;
        LinearLayout vgConfirm;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvOrderedOn = itemView.findViewById(R.id.tv_ordered_on);
            imgUser = itemView.findViewById(R.id.img_user);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
            vgConfirm = itemView.findViewById(R.id.vg_confirm);
            tvConfirmDelivery  = itemView.findViewById(R.id.tv_confirm_delivery);
            tvConfirmPickup = itemView.findViewById(R.id.tv_confirm_pickup);
            tvContactSeller = itemView.findViewById(R.id.tv_contact_seller);
            tvRate = itemView.findViewById(R.id.tv_rate);


        }
    }

    void getOrderedBy(Context context, String uid, TextView postedBy, CircleImageView circleImageView
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
                    }else{
                        circleImageView.setImageResource(R.drawable.outline_account_circle_24);
                    }
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getUserFcm(String buyerId,String title, String message,String adId,String productTitle){
        FirebaseDatabase.getInstance().getReference().child("users").child(buyerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = new User();
                    user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                    try {
                        sendPushNotification(title,message,buyerId, user.getFcmToken(), context ,adId,productTitle);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void sendPushNotification(String title,String body,String userId,String fcmToken,Context context,String adId, String productTitle) throws JSONException {
        String accessToken = SharedPrefManager.getInstance(context).getAccessToken();

        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",fcmToken);

        JSONObject dataObject = new JSONObject();
        dataObject.put("adId",adId);
        dataObject.put("title",productTitle);
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/user:"+adId+"_"+productTitle);
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
        RequestQueue queue = Volley.newRequestQueue(context);

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

    public interface RateButtonClickListener{
        void onRateButtonClicked(Order order);
    }
}
