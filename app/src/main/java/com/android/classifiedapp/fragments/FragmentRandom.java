package com.android.classifiedapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.classifiedapp.ActivityAdDetails;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentRandom#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentRandom extends Fragment implements AdsAdapter.OnAdClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView rvAds;
    TextView tvNoListing;
Context context;
ProgressBar progressCircular;

    TextView tvTitle,tvPrice,tvPostedOn,tvPostedBy,tvAddress,tvFeatured;
    ImageView imgProduct,imgLike;
    CircleImageView imgUser;
    CardView cardProduct;
    TextView tvTryAgain;
    TextView tvCategory;
    public FragmentRandom() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentRandom.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentRandom newInstance(String param1, String param2) {
        FragmentRandom fragment = new FragmentRandom();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_random, container, false);
        tvNoListing = view.findViewById(R.id.tv_no_listing);
        progressCircular = view.findViewById(R.id.progress_circular);
        tvPrice = view.findViewById(R.id.tv_price);
        tvTitle = view.findViewById(R.id.tv_title);
        imgProduct = view.findViewById(R.id.img_product);
        tvPostedOn = view.findViewById(R.id.tv_postedOn);
        tvPostedBy = view.findViewById(R.id.tv_postedBy);
        imgUser = view.findViewById(R.id.img_user);
        imgLike = view.findViewById(R.id.img_like);
        tvAddress = view.findViewById(R.id.tv_address);
        tvFeatured = view.findViewById(R.id.tv_featured);
        cardProduct = view.findViewById(R.id.card_product);
        tvTryAgain = view.findViewById(R.id.tv_try_again);
        tvCategory = view.findViewById(R.id.tv_category);
        getAds();

        tvTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAds();
            }
        });
        return view;
    }
    void getAds(){
        progressCircular.setVisibility(View.VISIBLE);
        cardProduct.setVisibility(View.GONE);
        tvNoListing.setVisibility(View.GONE);
        tvTryAgain.setVisibility(View.GONE);
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad.getStatus().equals(getString(R.string.approved)))
                                ads.add(ad);
                        }

                        Ad randomAd = getRandomElement(ads);
                        if (randomAd!=null){
                            progressCircular.setVisibility(View.GONE);
                            cardProduct.setVisibility(View.VISIBLE);
                            tvTryAgain.setVisibility(View.VISIBLE);
                            tvNoListing.setVisibility(View.GONE);
                          setUpAd(randomAd);
                        }else{
                            getAds();
                        }
                       
                    }catch (Exception e){
                        progressCircular.setVisibility(View.GONE);
                        cardProduct.setVisibility(View.GONE);
                        tvNoListing.setVisibility(View.VISIBLE);
                        tvTryAgain.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                }else{
                    cardProduct.setVisibility(View.GONE);
                    progressCircular.setVisibility(View.GONE);
                    tvNoListing.setVisibility(View.VISIBLE);
                    tvTryAgain.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                cardProduct.setVisibility(View.GONE);
                progressCircular.setVisibility(View.GONE);
                tvNoListing.setVisibility(View.VISIBLE);
                tvTryAgain.setVisibility(View.VISIBLE);
            }
        });
    }

    void setAdsAdapter(ArrayList<Ad> ads){
        progressCircular.setVisibility(View.GONE);
        if (ads!=null){
            tvNoListing.setVisibility(View.VISIBLE);
        }
        tvNoListing.setVisibility(View.GONE);
        rvAds.setLayoutManager(new LinearLayoutManager(context));
        rvAds.setAdapter(new AdsAdapter(ads,context,this));
    }

    @Override
    public void onLikeClicked(Ad ad, ImageView imageView) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static <T> T getRandomElement(ArrayList<T> list) {
        // Check if the list is not empty
        if (list != null && !list.isEmpty()) {
            // Generate a random index within the bounds of the list
            Random random = new Random();
            int randomIndex = random.nextInt(list.size());

            // Retrieve the element at the random index
            return list.get(randomIndex);
        } else {
            return null;
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

    void setUpAd(Ad ad){
        FirebaseUser fIrebaseUser =FirebaseAuth.getInstance().getCurrentUser();
        tvTitle.setText(ad.getTitle());
        tvPrice.setText(ad.getCurrency()+" "+ad.getPrice());
        tvAddress.setText(ad.getAddress());
        Glide.with(context).load(ad.getUrls().get(0)).into(imgProduct);
        String posted = context.getString(R.string.posted);
        long timestamp = Long.parseLong(ad.getPostedOn());
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
        tvPostedOn.setText(posted+" "+ago);

        getPostedBy(context,ad.getPostedBy(),tvPostedBy,imgUser);
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

        if (ad.getFeatured().equals("1")){
            if (ad.getExpiresOn()<now){
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
                ad.setFeatured("0");
                ad.setFeaturedOn(0);
                ad.setExpiresOn(0);
                databaseReference.setValue(ad);
            }
        }

        if (ad.getFeatured().equals("1")){
            tvFeatured.setVisibility(View.VISIBLE);
        }else{
            tvFeatured.setVisibility(View.GONE);
        }

        imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLike(ad,fIrebaseUser.getUid(),imgLike);
            }
        });

        cardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivityAdDetails.class).putExtra("ad",ad));
            }
        });

        getCategory(ad.getCategoryId());
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

    void getCategory(String categoryId){
        FirebaseDatabase.getInstance().getReference().child("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        Category category = new Category();
                        category.setId(snapshot.child("id").getValue(String.class));
                        category.setName(snapshot.child("name").getValue(String.class));
                        category.setId(snapshot.child("id").getValue(String.class));
                        tvCategory.setText(category.getName());
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
}