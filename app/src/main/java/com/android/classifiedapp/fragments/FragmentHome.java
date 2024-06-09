package com.android.classifiedapp.fragments;

import static com.android.classifiedapp.utilities.Constants.calculateDistance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.classifiedapp.ActivitySelectFilters;
import com.android.classifiedapp.ActivityViewedRecommendedProducts;
import com.android.classifiedapp.Home;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.adapters.GroupedAdsAdapter;
import com.android.classifiedapp.adapters.HomeCategoriesAdapter;
import com.android.classifiedapp.adapters.RecentlyViewedAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.GroupedItem;
import com.android.classifiedapp.models.ViewedAd;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment implements AdsAdapter.OnAdClickListener {
    HomeCategoriesAdapter homeCategoriesAdapter;
    RecyclerView rvCategories,rv_ads;
    LinearLayout vgFilters;
    TextView tvNoListing;

    Context context;
    ProgressBar progressCircular;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double roundedLatitude;
    Double roundedLongitude;
    TextView tvSeeAllRecent;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ArrayList<Ad> recentAds;
    LinearLayout vgRecentlyViewed;
    RecyclerView rvRecentlyViewedAds;

    LinearLayout vgRecommendedItems;
    TextView tvSeeAllRecommendations;
    RecyclerView rvRecommendedAds;

    DatabaseReference databaseReference;
    private List<GroupedItem> groupedItemList;
    GroupedAdsAdapter groupedAdsAdapter;

    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rvCategories = view.findViewById(R.id.rv_categories);
        rv_ads = view.findViewById(R.id.rv_ads);
        vgFilters = view.findViewById(R.id.vg_filters);
        tvNoListing = view.findViewById(R.id.tv_no_listing);
        progressCircular = view.findViewById(R.id.progress_circular);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        vgRecentlyViewed = view.findViewById(R.id.vg_recently_viewed);
        rvRecentlyViewedAds = view.findViewById(R.id.rv_recently_viewed_ads);
        tvSeeAllRecent = view.findViewById(R.id.tv_see_all_recent);
        vgRecommendedItems = view.findViewById(R.id.vg_recommended_items);
        tvSeeAllRecommendations = view.findViewById(R.id.tv_see_all_recommendations);
        rvRecommendedAds = view.findViewById(R.id.rv_recommended_ads);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        groupedItemList = new ArrayList<>();
        rv_ads.setNestedScrollingEnabled(false);
        groupedAdsAdapter = new GroupedAdsAdapter(groupedItemList,context);
        rv_ads.setLayoutManager(new LinearLayoutManager(context));
        rv_ads.setAdapter(groupedAdsAdapter);

        vgFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivitySelectFilters.class));
            }
        });
        getCategories();
        //getAds();
        getLastKnownLocation();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getViewedAdIds(uid);
        getInteractedCategories(uid);
        return view;
    }

    void getCategories(){
        FirebaseDatabase.getInstance().getReference().child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()){
                    Category category = new Category();
                    category.setId(s.child("id").getValue(String.class));
                    category.setName(s.child("name").getValue(String.class));
                    category.setId(s.child("id").getValue(String.class));
                    category.setImageUrl(s.child("imageUrl").getValue(String.class));

                    categories.add(category);
                }
                homeCategoriesAdapter = new HomeCategoriesAdapter(categories, context);
                rvCategories.setAdapter(homeCategoriesAdapter);
                rvCategories.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getAds(){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    if (snapshot.exists()){
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad!=null){
                                if (ad.getStatus()!=null){
                                    if (ad.getStatus().equals(getString(R.string.approved))){
                                        ads.add(ad);
                                        tvNoListing.setVisibility(View.GONE);
                                    }
                                }
                            }

                        }
                        setAdsAdapter(ads);

                    }else{
                        progressCircular.setVisibility(View.GONE);
                        rv_ads.setVisibility(View.GONE);
                        tvNoListing.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);

            }
        });
    }
    void getAds(Double latitude,Double longitude){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    if (snapshot.exists()){
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad!=null){
                                if (ad.getStatus()!=null){
                                    if (ad.getStatus().equals(getString(R.string.approved))){
                                        if (latitude>0){
                                            //LogUtils.e(latitude+" "+longitude);
                                            //LogUtils.e(ad.getLatitude(),ad.getLongitude());
                                            Double adLat = ad.getLatitude();
                                            Double adLong = ad.getLongitude();
                                            float[] results = new float[1]; // array to hold the result
                                            double distance = calculateDistance(latitude,longitude,adLat,adLong);
                                            // LogUtils.e(distance);
                                            if (distance<=500){
                                                ads.add(ad);
                                                tvNoListing.setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        setAdsAdapter(ads);

                    }else{
                        progressCircular.setVisibility(View.GONE);
                        rv_ads.setVisibility(View.GONE);
                        tvNoListing.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);

            }
        });
    }

    void setAdsAdapter(ArrayList<Ad> ads){
        progressCircular.setVisibility(View.GONE);
        if (ads.size()>0){
            rv_ads.setVisibility(View.VISIBLE);
            tvNoListing.setVisibility(View.GONE);
        }else {
            rv_ads.setVisibility(View.GONE);
            tvNoListing.setVisibility(View.VISIBLE);
        }

        rv_ads.setLayoutManager(new LinearLayoutManager(context));
        rv_ads.setAdapter(new AdsAdapter(ads,context,this));
    }

    @Override
    public void onLikeClicked(Ad ad, ImageView imageView) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    void getLastKnownLocation(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null){
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        try {
                            List<Address> addresses =geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
//                            LogUtils.e(location.getLatitude());
                            //                          LogUtils.e(location.getLongitude());
                            // Assuming addresses.get(0) is a valid Address object
                            Double latitude = addresses.get(0).getLatitude();
                            Double longitude = addresses.get(0).getLongitude();

                            BigDecimal latitudeBD = new BigDecimal(latitude).setScale(7, RoundingMode.HALF_UP);
                            BigDecimal longitudeBD = new BigDecimal(longitude).setScale(7, RoundingMode.HALF_UP);

                            roundedLatitude = latitudeBD.doubleValue();
                            roundedLongitude = longitudeBD.doubleValue();
                            // getAds(roundedLatitude,roundedLongitude);
                            getGroupedAdsAndCat(roundedLatitude,roundedLongitude);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }else{
            askPermissions();
        }
    }
    void askPermissions(){
        PermissionX.init(getActivity()).permissions().request(new RequestCallback() {
            @Override
            public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                if (b){
                    //startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
                    requestPermissionsBasedOnSdk();

                }else{
                    ToastUtils.showShort(context.getString(R.string.grant_all_to_see));
                    LogUtils.e(list1);
                }
            }
        });
    }

    private void requestPermissionsBasedOnSdk() {
        int targetSdk = Build.VERSION.SDK_INT;
        String[] permissions;

        if (targetSdk >= Build.VERSION_CODES.TIRAMISU) {
            LogUtils.e(targetSdk);
            // Android 13 (API level 33) and above - use READ_MEDIA_IMAGES
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.POST_NOTIFICATIONS};
        } else if (targetSdk >= Build.VERSION_CODES.R) {
            LogUtils.e(targetSdk);
            // Android 12 (API level 31) and up to 12L (level 32) - use READ_EXTERNAL_STORAGE
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            LogUtils.e(targetSdk);
            // Android 10 (API level 29) and below - use READ_EXTERNAL_STORAGE (hypothetical)
            // You likely wouldn't need to support such low API levels for requesting gallery access.
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        }

        PermissionX.init(this).permissions(permissions).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                if (allGranted) {
                    getLastKnownLocation();
                } else {
                    ToastUtils.showShort(context.getString(R.string.grant_all_to_see));
                    LogUtils.e(deniedList);
                    getAds();
                }
            }
        });
    }

    void getViewedAdIds(String uid){
        LogUtils.e(uid);
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recentlyVisited").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    LogUtils.e("datasnapshot exists");
                    LogUtils.e(snapshot);
                    LogUtils.e(snapshot.getChildren());
                    try {
                        recentAds = new ArrayList<>();
                        ArrayList<ViewedAd> viewedAds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                          /*  LogUtils.e(dataSnapshot);
                            ViewedAd viewedAd = new ViewedAd();
                            viewedAd.setViewedOn(dataSnapshot.child("visitedOn").getValue(Long.class));
                            viewedAd.setAdId(dataSnapshot.getKey());
                            LogUtils.e(dataSnapshot.getKey());
                            viewedAds.add(viewedAd);*/
                            String adId = dataSnapshot.getKey();
                            long timestamp = dataSnapshot.getValue(Long.class);
                            ViewedAd viewedAd = new ViewedAd();
                            viewedAd.setViewedOn(timestamp);
                            viewedAd.setAdId(adId);
                            viewedAds.add(viewedAd);
                        }
                        Collections.sort(viewedAds, new Comparator<ViewedAd>() {
                            @Override
                            public int compare(ViewedAd o1, ViewedAd o2) {
                                return Long.compare(o1.getViewedOn(),o2.getViewedOn());
                            }
                        });
                        getViewedAds(viewedAds);
                    }catch (Exception e){
                        LogUtils.e(e.getMessage());
                        e.printStackTrace();
                    }
                }else{
                    LogUtils.e("snapshot doesn't exist");
                    vgRecentlyViewed.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                vgRecentlyViewed.setVisibility(View.GONE);
            }
        });
    }

    void getViewedAds(ArrayList<ViewedAd> viewedAds){
        //fetch the details of ad by AdID
        for (ViewedAd viewedAd : viewedAds){
            getAd(viewedAd.getAdId(),viewedAds);
        }
    }
    void getAd(String adId,ArrayList<ViewedAd> viewedAds){
        FirebaseDatabase.getInstance().getReference().child("ads").child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        Ad ad = snapshot.getValue(Ad.class);
                        recentAds.add(ad);
                        LogUtils.e(recentAds.size(),viewedAds.size());
                        if (recentAds.size()==viewedAds.size()){
                            if (recentAds.size()<5){
                                tvSeeAllRecent.setVisibility(View.GONE);
                            }else{
                                tvSeeAllRecent.setVisibility(View.VISIBLE);
                                tvSeeAllRecent.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(context, ActivityViewedRecommendedProducts.class).putExtra("isRecommended",false));
                                    }
                                });
                            }
                            vgRecentlyViewed.setVisibility(View.VISIBLE);
                            RecentlyViewedAdsAdapter adsAdapter = new RecentlyViewedAdsAdapter(context,recentAds);
                            rvRecentlyViewedAds.setAdapter(adsAdapter);
                            rvRecentlyViewedAds.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));

                        }
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

    void getInteractedCategories(String uid){
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<ViewedAd> viewedAds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String categoryId = dataSnapshot.getKey();
                            long timestamp = dataSnapshot.getValue(Long.class);
                            ViewedAd viewedAd = new ViewedAd();
                            viewedAd.setViewedOn(timestamp);
                            viewedAd.setAdId(categoryId);
                            viewedAds.add(viewedAd);
                        }
                        Collections.sort(viewedAds, new Comparator<ViewedAd>() {
                            @Override
                            public int compare(ViewedAd o1, ViewedAd o2) {
                                return Long.compare(o1.getViewedOn(),o2.getViewedOn());
                            }
                        });
                        getAdsByCategory(viewedAds);
                    }catch (Exception e){
                        LogUtils.e(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getAdsByCategory(ArrayList<ViewedAd> interactedCategories){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        ArrayList<Ad> ads = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            for (ViewedAd interactedCat : interactedCategories){
                                if (ad.getCategoryId().equals(interactedCat.getAdId())){
                                    ads.add(ad);
                                }
                            }

                        }
                        if (ads.size()>0){
                            vgRecommendedItems.setVisibility(View.VISIBLE);
                            RecentlyViewedAdsAdapter adsAdapter = new RecentlyViewedAdsAdapter(context,ads);
                            rvRecommendedAds.setAdapter(adsAdapter);
                            rvRecommendedAds.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
                            if (ads.size()>5){
                                tvSeeAllRecommendations.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(context, ActivityViewedRecommendedProducts.class).putExtra("isRecommended",true));
                                    }
                                });
                                tvSeeAllRecommendations.setVisibility(View.VISIBLE);
                            }else{
                                tvSeeAllRecommendations.setVisibility(View.GONE);
                            }

                        }else{
                            vgRecommendedItems.setVisibility(View.GONE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    vgRecommendedItems.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //updated home view
    void getGroupedAdsAndCat(double roundedLatitude,double roundedLongitude){
        databaseReference.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        for (DataSnapshot s : snapshot.getChildren()){
                            Category category = new Category();
                            category.setId(s.child("id").getValue(String.class));
                            category.setName(s.child("name").getValue(String.class));
                            category.setId(s.child("id").getValue(String.class));
                            category.setImageUrl(s.child("imageUrl").getValue(String.class));
                            fetchAdsForCategory(category, roundedLatitude, roundedLongitude, new AdsCallback() {
                                @Override
                                public void onAdsFetched(Category category, ArrayList<Ad> ads) {
                                    if (!ads.isEmpty()){
                                        GroupedItem categoryItem = new GroupedItem(GroupedItem.TYPE_CATEGORY, category, ads);
                                        groupedItemList.add(categoryItem);
                                        progressCircular.setVisibility(View.GONE);
                                        tvNoListing.setVisibility(View.GONE);
                                        groupedAdsAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                        }
                        if (groupedItemList.isEmpty()){
                            progressCircular.setVisibility(View.GONE);
                            tvNoListing.setVisibility(View.VISIBLE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    progressCircular.setVisibility(View.GONE);
                    tvNoListing.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void fetchAdsForCategory(Category category ,double roundedLatitude,double roundedLongitude,AdsCallback callback){
        FirebaseDatabase.getInstance().getReference().child("ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Ad> ads = new ArrayList<>();
                if (snapshot.exists()){
                    try {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad.getCategoryId().equals(category.getId())){
                                double distance = calculateDistance(ad.getLatitude(),ad.getLongitude(),roundedLatitude,roundedLongitude);
                                if (distance<500)
                                    ads.add(ad);
                            }
                        }
                        callback.onAdsFetched(category,ads);
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

    private interface AdsCallback {
        void onAdsFetched(Category category, ArrayList<Ad> ads);
    }
}