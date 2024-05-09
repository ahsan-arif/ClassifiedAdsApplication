package com.android.classifiedapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.classifiedapp.ActivityMyAds;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.MyAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyListingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyListingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyListingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyListingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    int position;
    Context context;
    public static MyListingsFragment newInstance(String param1, String param2) {
        MyListingsFragment fragment = new MyListingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MyListingsFragment(int position){
        this.position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    ArrayList<Ad> ads;
    TextView tvNoItem;
    RecyclerView rvAds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_my_listings, container, false);
        tvNoItem = view.findViewById(R.id.tv_no_item);
        rvAds = view.findViewById(R.id.rv_ads);
        if (position==0)
        getMyApprovedListings(FirebaseAuth.getInstance().getCurrentUser().getUid());
        else if (position==1)
            getPendingApprovalAds(true,FirebaseAuth.getInstance().getCurrentUser().getUid());
        else
            getRequireUpdateAds(true,FirebaseAuth.getInstance().getCurrentUser().getUid());
        return view;
    }

    void getMyApprovedListings(String currentUserId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads");
        Query query = databaseReference.orderByChild("postedBy").equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (ad.getStatus().equals(getString(R.string.approved)))
                            ads.add(ad);
                    }
                }
                if (!ads.isEmpty()){
                    tvNoItem.setVisibility(View.GONE);
                    setMyListingsAdapter();
                }else{
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getRequireUpdateAds(boolean isNotApprovedAd,String currentUserId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads");
        Query query = databaseReference.orderByChild("postedBy").equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (ad.getStatus().equals(getString(R.string.require_update)))
                            ads.add(ad);
                    }
                }
                if (!ads.isEmpty()){
                    tvNoItem.setVisibility(View.GONE);
                    setMyListingsAdapter(isNotApprovedAd);
                }else{
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getPendingApprovalAds(boolean unApprovedAd,String currentUserId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads");
        Query query = databaseReference.orderByChild("postedBy").equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        if (ad.getStatus().equals(getString(R.string.pending_approval)))
                            ads.add(ad);
                    }
                }
                if (!ads.isEmpty()){
                    tvNoItem.setVisibility(View.GONE);
                    setMyListingsAdapter(unApprovedAd);
                }else{
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setMyListingsAdapter(){
        rvAds.setAdapter(new MyAdsAdapter(ads, context));
        rvAds.setLayoutManager(new LinearLayoutManager(context));
    }

    void setMyListingsAdapter(boolean isApproved){
        rvAds.setAdapter(new MyAdsAdapter(ads, context,isApproved));
        rvAds.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }


}