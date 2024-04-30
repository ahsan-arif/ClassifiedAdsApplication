package com.android.classifiedapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.android.classifiedapp.adapters.ImagePagerAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityAdDetails extends AppCompatActivity {
    ViewPager2 pagerImages;
    TabLayout tabsImg;
    TextView tvTitle,tvPrice;
    TextView tvDescription,tvShipping,tvName;
    CircleImageView imgUser;
    ImageView imgLike,imgBack;
    ImageView imgChat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ad_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseUser fIrebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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

        Ad ad = getIntent().getParcelableExtra("ad");
        ImagePagerAdapter adapter = new ImagePagerAdapter(this,ad.getUrls());
        pagerImages.setAdapter(adapter);

        // Setup TabLayout with ViewPager
        new TabLayoutMediator(tabsImg, pagerImages, (tab, position) -> {
            // You can set custom tab view here if needed, for now, just add empty text
            tab.setText("");
        }).attach();

        tvTitle.setText(ad.getTitle());
        tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
        tvDescription.setText(ad.getDescription());
        if (ad.isShippingAvailable()){
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
                startActivity(new Intent(ActivityAdDetails.this,ActivityChat.class).putExtra("sellerId",ad.getPostedBy()));
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
                            // LogUtils.e(snapshot);
                            //   for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = new User();
                            user.setEmail(snapshot.child("email").getValue(String.class));
                            // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                            user.setName(snapshot.child("name").getValue(String.class));
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

    void createLinkWithMeta(Ad ad, TextView btnShare) {
        btnShare.setClickable(false);
        btnShare.setEnabled(false);
        String mediaUrl;
        mediaUrl = ad.getUrls().get(0);

        FirebaseDynamicLinks.getInstance().createDynamicLink()
               // .setLink(Uri.parse("https://maweshi.co/ad-view:" + ad.getId()))
                .setDomainUriPrefix("https://mallmaweshi.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.mallMaweshi.android")
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
                                .setTitle("Yeh janwar dekho")
                                .setImageUrl(Uri.parse(mediaUrl))
                                .setDescription("Ab janwar khareedo aur becho ghar bhete Mall Maweshi App ke sath.")
                                .build())
                .buildShortDynamicLink().addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        //shareUrl(String.valueOf(shortDynamicLink.getShortLink()), btnShare);
                        /*if (!post.getUser().getUserId().equals(currentUserId)){
                            try {
                                sendPushNotification(post.getUser().getFcmToken(),post.getId(),newShare,adShared);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }*/

                    }
                });
        //dynamicLink.getUri();
        //LogUtils.e(dynamicLink.getUri());
        //shareUrl(dynamicLink.getUri().toString());
    }
}