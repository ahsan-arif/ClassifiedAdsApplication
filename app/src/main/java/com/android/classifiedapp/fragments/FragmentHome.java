package com.android.classifiedapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        vgFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivitySelectFilters.class));
            }
        });
        getCategories();
        getAds();
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

    @Override
    public void onResume() {
        super.onResume();
        getAds();
    }
}