package com.android.classifiedapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.classifiedapp.Home;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.adapters.HomeCategoriesAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.blankj.utilcode.util.LogUtils;
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

                    categories.add(category);
                }
                homeCategoriesAdapter = new HomeCategoriesAdapter(categories, getContext());
                rvCategories.setAdapter(homeCategoriesAdapter);
                rvCategories.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getAds(){
       FirebaseDatabase.getInstance().getReference().child("ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Ad> ads = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        ads.add(ad);
                    }
                   setAdsAdapter(ads);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setAdsAdapter(ArrayList<Ad> ads){
        rv_ads.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_ads.setAdapter(new AdsAdapter(ads,getContext(),this));
    }

    @Override
    public void onLikeClicked(Ad ad, ImageView imageView) {

    }
}