package com.android.classifiedapp;

import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.android.classifiedapp.adapters.ImagePagerAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Order;
import com.android.classifiedapp.models.Rating;
import com.android.classifiedapp.models.Report;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityAdDetails extends AppCompatActivity {
    ViewPager2 pagerImages;
    TabLayout tabsImg;
    TextView tvTitle,tvPrice;
    TextView tvDescription,tvShipping,tvName;
    CircleImageView imgUser;
    ImageView imgLike,imgBack,imgShare;
    ImageView imgChat;
    TextView tvReportListing;
    GoogleMap googleMap1;
    String accessToken;
    TextView tvBuy;
    Ad ad;

    List<Place.Field> fields;
    ActivityResultLauncher<Intent> placesIntent;
    Double latitude,longitude;
    String address;
    TextInputEditText etLocation;
    RatingBar ratingbar;

    User postedByUser;

    TextView tvViewOrders;

    int ordersAvailable;
    boolean isPremiumUser;
    long benefitsExpiry;
    RatingBar ratingbarProduct;

    String adId;
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
        setContentView(R.layout.activity_ad_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        LogUtils.e(fIrebaseUser.getUid());
        pagerImages = findViewById(R.id.pager_images);
        tabsImg = findViewById(R.id.tabs_img);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvShipping = findViewById(R.id.tv_shipping);
        tvName = findViewById(R.id.tv_name);
        imgUser = findViewById(R.id.img_user);
        imgLike = findViewById(R.id.img_like);
        imgBack = findViewById(R.id.img_back);
        imgChat = findViewById(R.id.img_chat);
        imgShare = findViewById(R.id.img_share);
        tvReportListing = findViewById(R.id.tv_report_listing);
        tvBuy = findViewById(R.id.tv_buy);
        ratingbar = findViewById(R.id.ratingbar);
        tvViewOrders = findViewById(R.id.tv_view_orders);
        ratingbarProduct = findViewById(R.id.ratingbar_product);

        ad = getIntent().getParcelableExtra("ad");
        String adId2 = getIntent().getStringExtra("adId");
        Bundle extras = getIntent().getExtras();
        if (ad==null && extras!=null){
            LogUtils.e(extras);
            String adId = extras.getString("id");
            this.adId = adId;
            getListing(adId);
            LogUtils.e(extras);
        }
        if (adId2!=null){
            getListing(adId2);
        }

        accessToken = SharedPrefManager.getInstance(ActivityAdDetails.this).getAccessToken();
        if (ad!=null){
            if (fIrebaseUser.getUid().equals(ad.getPostedBy())){
                tvReportListing.setVisibility(View.GONE);
                tvBuy.setVisibility(View.GONE);
                tvViewOrders.setVisibility(View.VISIBLE);
            }
            if (fIrebaseUser.getUid().equals(ad.getPostedBy())){
                imgChat.setVisibility(View.GONE);
            }
            ImagePagerAdapter adapter = new ImagePagerAdapter(this,ad.getUrls(),false);
            pagerImages.setAdapter(adapter);
            adId = ad.getId();
            if (!ad.getPostedBy().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                addInHistory(adId);
                addInInteractedCategories(ad.getCategoryId());
            }
        }


        Places.initialize(ActivityAdDetails.this, getString(R.string.places_api_key), Locale.US);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        placesIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    Place place  = Autocomplete.getPlaceFromIntent(data);
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    address = place.getAddress();
                    etLocation.setText(place.getAddress());
                }
            }
        });

        if (ad!=null){
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
            // Initialize the map
            mapFragment.getMapAsync(googleMap -> {
                this.googleMap1 = googleMap;

                // Add a marker at a specific location
                LatLng markerLocation = new LatLng(ad.getLatitude(), ad.getLongitude()); // San Francisco, CA
                googleMap.addMarker(new MarkerOptions().position(markerLocation).title(ad.getTitle()));

                // Move the camera to the marker location and zoom in
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 12));
            });

            // Setup TabLayout with ViewPager
            new TabLayoutMediator(tabsImg, pagerImages, (tab, position) -> {
                // You can set custom tab view here if needed, for now, just add empty text
                tab.setText("");
            }).attach();

            tvTitle.setText(ad.getTitle());
            tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
            tvDescription.setText(ad.getDescription());
            if (ad.isShippingAvailable()){
                if ( ad.getShippingPayer().equals(getString(R.string.seller))){
                    tvShipping.setText(getString(R.string.free_shipping));
                }else
                    tvShipping.setText(getString(R.string.can_be_shipped));
            }else{
                tvShipping.setText(getString(R.string.can__not_be_shipped));
            }

            if (ad.getLikedByUsers()!=null){
                if (!ad.getLikedByUsers().isEmpty()){
                    if (ad.getLikedByUsers().contains(fIrebaseUser.getUid())){
                        imgLike.setImageResource(R.drawable.heart_red);
                    }else{
                        imgLike.setImageResource(R.drawable.heart);
                    }
                }else{
                    imgLike.setImageResource(R.drawable.heart);
                }
            }else{
                imgLike.setImageResource(R.drawable.heart);
            }
            getPostedBy(this,ad.getPostedBy(),tvName,imgUser);
        }

        imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLike(ad,fIrebaseUser.getUid(),imgLike);
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityAdDetails.this,ActivityChat.class).putExtra("sellerId",ad.getPostedBy()).putExtra("adId",ad.getId()));
            }
        });
        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("Please wait generating shareable link");
                createLinkWithMeta(ad,imgShare);
            }
        });

        tvReportListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog(ad.getId());
            }
        });

        tvBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                LogUtils.e(ordersAvailable);
                LogUtils.e(isPremiumUser);
                if (isPremiumUser){
                    if (benefitsExpiry >= now){
                        showBuySheet();
                    }
                }
                else{
                    if (ordersAvailable>0){
                        showBuySheet();
                    }else{
                        ToastUtils.showShort(getString(R.string.cannot_accept_orders));
                    }
                }
            }
        });
        if (ad!=null){
            if (ad.getQuantity() ==0){
                tvBuy.setText(getString(R.string.out_of_stock));
                tvBuy.setTextColor(getColor(R.color.red));
                tvBuy.setBackgroundResource(R.drawable.bg_report_btn);
                tvBuy.setOnClickListener(null);
            }
        }

        tvViewOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityAdDetails.this,ActivityViewOrders.class).putExtra("adId",ad.getId()).putExtra("title",ad.getTitle()));
            }
        });
    }

    void getPostedBy(Context context, String uid, TextView postedBy, CircleImageView circleImageView
    ){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    LogUtils.e(uid);
                    // LogUtils.e(snapshot);
                    //   for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = new User();
                    user.setEmail(snapshot.child("email").getValue(String.class));
                    // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                    user.setName(snapshot.child("name").getValue(String.class));
                    user.setFcmToken(snapshot.child("fcmToken").getValue(String.class));
                    if (snapshot.hasChild("maximumOrdersAvailable")){
                        ordersAvailable = snapshot.child("maximumOrdersAvailable").getValue(Integer.class);
                    }
                    if (snapshot.hasChild("premiumUser")){
                        isPremiumUser = snapshot.child("premiumUser").getValue(Boolean.class);
                    }
                    LogUtils.e(ordersAvailable);
                    if (snapshot.hasChild("benefitsExpiry")){
                        benefitsExpiry = snapshot.child("benefitsExpiry").getValue(Long.class);
                    }


                    postedBy.setText(user.getName());
                    if (snapshot.hasChild("profileImage")){
                        user.setProfileImage(snapshot.child("profileImage").getValue(String.class));
                        Glide.with(context).load(user.getProfileImage()).into(circleImageView);
                    }else{
                        circleImageView.setImageResource(R.drawable.outline_account_circle_24);
                    }
                    postedByUser = user;

                    getSellerRating(uid);
                    // }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void toggleLike(Ad ad, String currentUserId,ImageView imageView) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId()).child("likedByUsers");
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    List<String> likedByUsers = new ArrayList<>();
                    likedByUsers.add(currentUserId);
                    postRef.setValue(likedByUsers);
                    ad.setLikedByUsers(likedByUsers);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void createLinkWithMeta(Ad ad, ImageView btnShare) {
        btnShare.setClickable(false);
        btnShare.setEnabled(false);
        String mediaUrl;
        mediaUrl = ad.getUrls().get(0);

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://classifiedadsapplication.page.link/ad-view:" + ad.getId()))
                .setDomainUriPrefix("https://classifiedadsapplication.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.android.classifiedapp")
                                .setMinimumVersion(1)
                                .build())
                /*.setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())*/
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(ad.getTitle())
                                .setImageUrl(Uri.parse(mediaUrl))
                                .setDescription(ad.getDescription())
                                .build())
                .buildShortDynamicLink().addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        LogUtils.e(shortDynamicLink.getShortLink());
                        shareUrl(String.valueOf(shortDynamicLink.getShortLink()), btnShare);

                        /*if (!post.getUser().getUserId().equals(currentUserId)){
                            try {
                                sendPushNotification(post.getUser().getFcmToken(),post.getId(),newShare,adShared);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }*/

                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LogUtils.e(e.getMessage());
                        LogUtils.e(e);
                    }
                });
        //dynamicLink.getUri();
        //LogUtils.e(dynamicLink.getUri());
        //shareUrl(dynamicLink.getUri().toString());
    }
    void shareUrl(String url, ImageView btnShare) {
        btnShare.setEnabled(true);
        btnShare.setClickable(true);
        JSONObject props = new JSONObject();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        //  sendIntent.putExtra(Intent.EXTRA_TEXT, Constants.BASE_URL_FRONTEND + post.getId());
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Share Ad");
        sendIntent.setType("text/plain");
        // Show the Sharesheet
        startActivity(Intent.createChooser(sendIntent, null));
    }

    void showReportDialog(String adId){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_report_listing, null);
        dialogBuilder.setView(dialogView);
        //TextView tvReportListing = dialogView.findViewById(R.id.tv_report_listing);
        final String[] reason = new String[1];

        RadioGroup rgReport = dialogView.findViewById(R.id.rg_report);

        dialogBuilder.setNegativeButton(getString(R.string.report_item), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //report ad
                ToastUtils.showShort("report");
                if (!reason[0].isEmpty()){
                    reportAd(adId,reason[0]);
                    finish();
                }
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.cancel), null);

        rgReport.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton =group.findViewById(checkedId);
                reason[0] = radioButton.getText().toString();
                ToastUtils.showShort(reason[0]);
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle(getString(R.string.report_item));
        alertDialog.setMessage(getString(R.string.please_tell_us));
        alertDialog.show();
    }

    void reportAd(String adId,String reason){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(adId).child("reports");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<Report> reports = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Report report = dataSnapshot.getValue(Report.class);
                        reports.add(report);
                    }
                    Report report = new Report();
                    report.setReportedBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    report.setReason(reason);
                    report.setReportedOn(String.valueOf(System.currentTimeMillis()));
                    reports.add(report);
                    databaseReference.setValue(reports);
                    ToastUtils.showShort(getString(R.string.feedback_recorded));
                }else{
                    List<Report> reports = new ArrayList<>();
                    Report report = new Report();
                    report.setReportedBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    report.setReason(reason);
                    report.setReportedOn(String.valueOf(System.currentTimeMillis()));
                    reports.add(report);
                    databaseReference.setValue(reports);
                    ToastUtils.showShort(getString(R.string.feedback_recorded));
                }

                updateAdStatus(adId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void updateAdStatus(String adId){
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).child("status").setValue(getString(R.string.pending_approval));
        getAdminFcm(adId);
    }
    void getAdminFcm(String adId){
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<String> fcmTokens = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        if (user.getRole()!=null){
                            if (user.getRole().equals("admin"))
                                fcmTokens.add(user.getFcmToken());
                        }

                    }
                    for (String token : fcmTokens){
                        try {
                            sendPushNotification(token,getString(R.string.update),getString(R.string.ad_reported),adId);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void sendPushNotification(String toFcmToken,String title,String body,String adId) throws JSONException {
        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",toFcmToken);

        JSONObject dataObject = new JSONObject();
        dataObject.put("id",adId);
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/reportedAdId:"+adId);

        messageObject.put("data",dataObject);

        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action","com.example.classifiedadsappadmin.ActivityAdDetails");

        androidObject.put("notification",activityNotificationObject);
        messageObject.put("android",androidObject);

        JSONObject finalObject = new JSONObject();
        finalObject.put("message",messageObject);
        //finalObject.put("data",dataObject);
        LogUtils.json(finalObject);

// Create a new RequestQueue
        RequestQueue queue = Volley.newRequestQueue(ActivityAdDetails.this);

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

    void showBuySheet(){
        BottomSheetDialog buyDialog = new BottomSheetDialog(ActivityAdDetails.this);
        buyDialog.setContentView(R.layout.dialog_buy);

        TextView tvItemsAvailable = buyDialog.findViewById(R.id.tv_items_available);

        etLocation = buyDialog.findViewById(R.id.et_location);
        TextInputEditText etQuantity = buyDialog.findViewById(R.id.et_quantity);
        TextView tvPay = buyDialog.findViewById(R.id.tv_pay);
        getQuantity(tvItemsAvailable,tvPay);

        if (!ad.isShippingAvailable()){
            etLocation.setVisibility(View.GONE);
        }

        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("ClickableViewAccessibility") Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(ActivityAdDetails.this);
                placesIntent.launch(intent);
            }
        });
        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etQuantity.getText().toString().isEmpty()){
                    if (Integer.parseInt(etQuantity.getText().toString())<=ad.getQuantity()){
                        String location;
                        if (!ad.isShippingAvailable()){
                            location="";
                        }else{
                            location=  etLocation.getText().toString();
                            if (location.isEmpty()){
                                etLocation.setError(getString(R.string.cannot_be_empty));
                                return;
                            }
                        }
                        startActivity(new Intent(ActivityAdDetails.this, ActivityCheckout.class)
                                .putExtra("fcmToken",postedByUser.getFcmToken())
                                .putExtra("quantity",etQuantity.getText().toString())
                                .putExtra("location",location)
                                .putExtra("isPremiumUser",isPremiumUser)
                                .putExtra("ordersAvailable",ordersAvailable)
                                .putExtra("ad",ad)
                        );
                        buyDialog.dismiss();
                    }else{
                        etQuantity.setError(getString(R.string.cannot_buy_more));
                    }
                }else{
                    etQuantity.setError(getString(R.string.cannot_be_empty));
                }

            }
        });
        buyDialog.show();

    }

    void getSellerRating(String uid){

        DatabaseReference sellerRef = FirebaseDatabase.getInstance().getReference().child("ratings");
        sellerRef.orderByChild("sellerId").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        float commulativeRating=0;
                        float productRating = 0;
                        int totalRecords=0;
                        List<Rating> ratings = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Rating rating = dataSnapshot.getValue(Rating.class);
                            ratings.add(rating);
                            commulativeRating = commulativeRating+rating.getSellerRating();
                            if (rating.getProductId().equals(ad.getId())){
                                productRating = productRating+rating.getProductRating();
                                totalRecords++;
                            }
                        }
                        float averageRating = commulativeRating / ratings.size();
                        float averageProductRating = productRating/totalRecords;
                        ratingbarProduct.setRating(averageProductRating);
                        ratingbar.setRating(averageRating);
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

    void getQuantity(TextView tvItemsAvailable,TextView proceedToPayment){
        FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId()).child("quantity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int quantity = snapshot.getValue(Integer.class);
                tvItemsAvailable.setText(getString(R.string.items_available)+" "+quantity);

                if (quantity ==0){
                    tvBuy.setText(getString(R.string.out_of_stock));
                    tvBuy.setTextColor(getColor(R.color.red));
                    tvBuy.setBackgroundResource(R.drawable.bg_report_btn);
                    tvBuy.setOnClickListener(null);

                    proceedToPayment.setText(getString(R.string.out_of_stock));
                    proceedToPayment.setTextColor(getColor(R.color.red));
                    proceedToPayment.setBackgroundResource(R.drawable.bg_report_btn);
                    proceedToPayment.setOnClickListener(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void sendPushNotification(String title,String body) throws JSONException {
        JSONObject messageObject = new JSONObject();
        // messageObject.put("token",fcmToken);

        JSONObject notificationObject =new JSONObject();
        notificationObject.put("body",body);
        notificationObject.put("title",title);

        messageObject.put("notification",notificationObject);
        messageObject.put("token",postedByUser.getFcmToken());

        JSONObject dataObject = new JSONObject();
        dataObject.put("id",ad.getId());
        dataObject.put("deepLink","https://classifiedadsapplication.page.link/adId:"+ad.getId());

        messageObject.put("data",dataObject);

        JSONObject androidObject = new JSONObject();
        JSONObject activityNotificationObject = new JSONObject();
        activityNotificationObject.put("click_action","com.android.classifiedapp.ActivityAdDetails");

        androidObject.put("notification",activityNotificationObject);
        messageObject.put("android",androidObject);

        JSONObject finalObject = new JSONObject();
        finalObject.put("message",messageObject);
        //finalObject.put("data",dataObject);
        LogUtils.json(finalObject);

// Create a new RequestQueue
        RequestQueue queue = Volley.newRequestQueue(ActivityAdDetails.this);

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

    void getListing(String adId){
        ProgressDialog progressDialog = new ProgressDialog(ActivityAdDetails.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_ad));
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()){
                    FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    ad = snapshot.getValue(Ad.class);
                    if (ad!=null){
                        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
                        // Initialize the map
                        mapFragment.getMapAsync(googleMap -> {
                            googleMap1 = googleMap;

                            // Add a marker at a specific location
                            LatLng markerLocation = new LatLng(ad.getLatitude(), ad.getLongitude()); // San Francisco, CA
                            googleMap.addMarker(new MarkerOptions().position(markerLocation).title(ad.getTitle()));

                            // Move the camera to the marker location and zoom in
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 12));
                        });


                        tvTitle.setText(ad.getTitle());
                        tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
                        tvDescription.setText(ad.getDescription());
                        if (ad.isShippingAvailable()){
                            if ( ad.getShippingPayer().equals(getString(R.string.seller))){
                                tvShipping.setText(getString(R.string.free_shipping));
                            }else
                                tvShipping.setText(getString(R.string.can_be_shipped));
                        }else{
                            tvShipping.setText(getString(R.string.can__not_be_shipped));
                        }

                        if (ad.getLikedByUsers()!=null){
                            if (!ad.getLikedByUsers().isEmpty()){
                                if (ad.getLikedByUsers().contains(fIrebaseUser.getUid())){
                                    imgLike.setImageResource(R.drawable.heart_red);
                                }else{
                                    imgLike.setImageResource(R.drawable.heart);
                                }
                            }else{
                                imgLike.setImageResource(R.drawable.heart);
                            }
                        }else{
                            imgLike.setImageResource(R.drawable.heart);
                        }
                        if (fIrebaseUser.getUid().equals(ad.getPostedBy())){
                            tvReportListing.setVisibility(View.GONE);
                            tvBuy.setVisibility(View.GONE);
                            tvViewOrders.setVisibility(View.VISIBLE);
                            imgChat.setVisibility(View.GONE);
                        } else{
                            addInHistory(ad.getId());
                            addInInteractedCategories(ad.getCategoryId());
                        }
                        ImagePagerAdapter adapter = new ImagePagerAdapter(ActivityAdDetails.this,ad.getUrls(),false);
                        pagerImages.setAdapter(adapter);
                        // Setup TabLayout with ViewPager
                        new TabLayoutMediator(tabsImg, pagerImages, (tab, position) -> {
                            // You can set custom tab view here if needed, for now, just add empty text
                            tab.setText("");
                        }).attach();
                        getPostedBy(ActivityAdDetails.this,ad.getPostedBy(),tvName,imgUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }

    void addInHistory(String adId){
        String cUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
      DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child("users").child(cUid).child("recentlyVisited").child(adId);
      Map<String, Object> update = new HashMap<>();
      update.put("visitedOn",System.currentTimeMillis());
      databaseReference.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
         if (task.isSuccessful()){
             LogUtils.e("successful");
         }else{
             LogUtils.e("unsuccessful");
         }
          }
      });
    }

    void addInInteractedCategories(String categoryId){
        String cUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child("users").child(cUid).child("interests").child(categoryId);
        Map<String, Object> update = new HashMap<>();
        update.put("visitedOn",System.currentTimeMillis());
        databaseReference.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    LogUtils.e("successful");
                }else{
                    LogUtils.e("unsuccessful");
                }
            }
        });
    }
}