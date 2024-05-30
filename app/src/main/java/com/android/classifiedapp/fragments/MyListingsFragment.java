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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.classifiedapp.ActivityMyAds;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.MyAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyListingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyListingsFragment extends Fragment implements MyAdsAdapter.PaymentButtonClickListener {

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
    MyAdsAdapter adapter;
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
    ProgressBar progressCircular;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_my_listings, container, false);
        tvNoItem = view.findViewById(R.id.tv_no_item);
        rvAds = view.findViewById(R.id.rv_ads);
        progressCircular = view.findViewById(R.id.progress_circular);
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
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()){
                    try {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            LogUtils.e(ad.getTitle());
                            if (ad.getStatus().equals(getString(R.string.approved)))
                                ads.add(ad);
                        }
                        if (!ads.isEmpty()){
                            progressCircular.setVisibility(View.GONE);
                            tvNoItem.setVisibility(View.GONE);
                            setMyListingsAdapter();
                        }else{
                            progressCircular.setVisibility(View.GONE);
                            tvNoItem.setVisibility(View.VISIBLE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    progressCircular.setVisibility(View.GONE);
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);
                ToastUtils.showShort(error.getMessage());
            }
        });
    }

    void getRequireUpdateAds(boolean isNotApprovedAd,String currentUserId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads");
        Query query = databaseReference.orderByChild("postedBy").equalTo(currentUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ads = new ArrayList<>();
                if (snapshot.exists()){
                    try {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad.getStatus().equals(getString(R.string.require_update)))
                                ads.add(ad);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if (!ads.isEmpty()){
                    progressCircular.setVisibility(View.GONE);
                    tvNoItem.setVisibility(View.GONE);
                    setMyListingsAdapter(isNotApprovedAd);
                }else{
                    progressCircular.setVisibility(View.GONE);
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
                    try {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Ad ad = dataSnapshot.getValue(Ad.class);
                            if (ad!=null){
                                if (ad.getStatus()!=null){
                                    if (ad.getStatus().equals(getString(R.string.pending_approval)))
                                        ads.add(ad);
                                }
                            }
                        }
                    }catch (Exception e){
                       e.printStackTrace();
                    }
                }
                if (!ads.isEmpty()){
                    progressCircular.setVisibility(View.GONE);
                    tvNoItem.setVisibility(View.GONE);
                    setMyListingsAdapter(unApprovedAd);
                }else{
                    progressCircular.setVisibility(View.GONE);
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressCircular.setVisibility(View.GONE);
            }
        });
    }

    void setMyListingsAdapter(){
        rvAds.setAdapter(new MyAdsAdapter(ads, context,getActivity().getApplication(),this));
        rvAds.setLayoutManager(new LinearLayoutManager(context));
    }

    void setMyListingsAdapter(boolean isApproved){
        adapter = new MyAdsAdapter(ads, context,isApproved,getActivity().getApplication(),this);
        rvAds.setAdapter(adapter);
        rvAds.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (position==0)
            getMyApprovedListings(FirebaseAuth.getInstance().getCurrentUser().getUid());
        else if (position==1)
            getPendingApprovalAds(true,FirebaseAuth.getInstance().getCurrentUser().getUid());
        else
            getRequireUpdateAds(true,FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public void onButtonClicked(Ad ad, TextView tvMakeFeatured, TextView tvFeatured,int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
        ad.setFeatured("1");
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        // Calculate 24 hrs in milliseconds (1440 minutes * 60 seconds * 1000 milliseconds)
        long twentyFourHours = 1440 * 60 * 1000; //24hrs
        // Add 5 minutes to the current time
        long newTimeMillis = currentTimeMillis + twentyFourHours;
        ad.setFeaturedOn(currentTimeMillis);
        ad.setExpiresOn(newTimeMillis);
        databaseReference.setValue(ad);
        tvFeatured.setVisibility(View.VISIBLE);
        tvFeatured.setVisibility(View.GONE);
    }
}