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
import com.android.classifiedapp.Home;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.adapters.HomeCategoriesAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
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
import java.util.List;
import java.util.Locale;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

        vgFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivitySelectFilters.class));
            }
        });
        getCategories();
        //getAds();
        getLastKnownLocation();
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
                                            LogUtils.e(latitude+" "+longitude);
                                            LogUtils.e(ad.getLatitude(),ad.getLongitude());
                                            Double adLat = ad.getLatitude();
                                            Double adLong = ad.getLongitude();
                                            float[] results = new float[1]; // array to hold the result
                                            double distance = calculateDistance(latitude,longitude,adLat,adLong);
                                            LogUtils.e(distance);
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
                            LogUtils.e(location.getLatitude());
                            LogUtils.e(location.getLongitude());
                            // Assuming addresses.get(0) is a valid Address object
                            Double latitude = addresses.get(0).getLatitude();
                            Double longitude = addresses.get(0).getLongitude();

                            BigDecimal latitudeBD = new BigDecimal(latitude).setScale(7, RoundingMode.HALF_UP);
                            BigDecimal longitudeBD = new BigDecimal(longitude).setScale(7, RoundingMode.HALF_UP);

                             roundedLatitude = latitudeBD.doubleValue();
                             roundedLongitude = longitudeBD.doubleValue();
                            getAds(roundedLatitude,roundedLongitude);
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
                    ToastUtils.showShort("Grant all permission to proceed");
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
                    ToastUtils.showShort("Grant all permissions to proceed");
                    LogUtils.e(deniedList);
                    getAds();
                }
            }
        });
    }
}