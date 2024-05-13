package com.android.classifiedapp.adapters;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.ActivityAdDetails;
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

    boolean isMyOrders;

    public OrdersAdapter(Context context, ArrayList<Order> orders,boolean isMyOrders) {
        this.context = context;
        this.orders = orders;
        this.productTitle = productTitle;
        this.isMyOrders = isMyOrders;
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

        if (order.getSellerId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.tvConfirmOrder.setVisibility(View.VISIBLE);
        }else{
            holder.tvConfirmOrder.setVisibility(View.GONE);
        }

        long timestamp = Long.parseLong(order.getPlaceOn());
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.tvOrderedOn.setText(ago);

        if (order.getAddress()!=null){
            holder.tvAddress.setText(order.getAddress());
            holder.tvAddress.setVisibility(View.VISIBLE);
        }else{
            holder.tvAddress.setVisibility(View.GONE);
        }

        if (order.getStatus().equals(context.getString(R.string.order_confirmed))){
            holder.tvConfirmOrder.setVisibility(View.GONE);
        }else{
            holder.tvConfirmOrder.setVisibility(View.VISIBLE);
        }

        holder.tvConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e(" productId "+order.getProductId()+"order id "+order.getId());
              DatabaseReference oderReference=  FirebaseDatabase.getInstance().getReference().child("ads").child(order.getProductId()).child("orders").child(order.getId()).child("status");
              oderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                      if (snapshot.exists()){
                          oderReference.setValue(context.getString(R.string.order_confirmed));
                      }else{
                          ToastUtils.showShort("Ref not found");
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
              });
              DatabaseReference buyerReference=  FirebaseDatabase.getInstance().getReference().child("users").child(order.getBuyerId()).child("orders").child(order.getId()).child("status");
              buyerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                      if (snapshot.exists()){
                          buyerReference.setValue(context.getString(R.string.order_confirmed));
                      }else{
                          ToastUtils.showShort("User Ref not found");
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
              });

              getUserFcm(order.getBuyerId());
            }
        });
        getOrderedBy(context,order.getBuyerId(),holder.tvUser,holder.imgUser);

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle,tvQuantity,tvTotal,tvStatus,tvUser,tvOrderedOn,tvAddress,tvConfirmOrder;
        CircleImageView imgUser;
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
            tvConfirmOrder = itemView.findViewById(R.id.tv_confirm_order);

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
                    }
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getUserFcm(String buyerId){
        FirebaseDatabase.getInstance().getReference().child("users").child(buyerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = new User();
                    user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                    try {
                        sendPushNotification(context.getString(R.string.order_confirmed),context.getString(R.string.your_order_confirmed),buyerId, user.getFcmToken(), context );
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

    void sendPushNotification(String title,String body,String userId,String fcmToken,Context context) throws JSONException {
        String accessToken = SharedPrefManager.getInstance(context).getAccessToken();

        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",fcmToken);

        JSONObject dataObject = new JSONObject();
        dataObject.put("id",userId);
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/user:"+userId);

        messageObject.put("data",dataObject);

        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action","com.android.classifiedapp.ActivityMyOrders");

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
}
