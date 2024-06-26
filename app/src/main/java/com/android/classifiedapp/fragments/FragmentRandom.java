package com.android.classifiedapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.AdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
        rvAds = view.findViewById(R.id.rv_ads);
        tvNoListing = view.findViewById(R.id.tv_no_listing);
        progressCircular = view.findViewById(R.id.progress_circular);
        getAds();
        return view;
    }
    void getAds(){

        FirebaseDatabase.getInstance().getReference().child("ads").addValueEventListener(new ValueEventListener() {
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
                        setAdsAdapter(ads);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    progressCircular.setVisibility(View.GONE);
                    tvNoListing.setVisibility(View.VISIBLE);
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
}