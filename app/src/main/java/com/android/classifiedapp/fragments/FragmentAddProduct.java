package com.android.classifiedapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.CurrencyAdapter;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAddProduct#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAddProduct extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    AutoCompleteTextView ddCurrency;
    ArrayList<Currency> currencies;
    TextInputEditText etLocation;

    ActivityResultLauncher<Intent> placesIntent;
    List<Place.Field> fields;

    public FragmentAddProduct() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAddProduct.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAddProduct newInstance(String param1, String param2) {
        FragmentAddProduct fragment = new FragmentAddProduct();
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
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        ddCurrency = view.findViewById(R.id.dd_currency);
        etLocation = view.findViewById(R.id.et_location);
        Places.initialize(getContext(), getString(R.string.places_api_key), Locale.US);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        placesIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    Place place  = Autocomplete.getPlaceFromIntent(data);
                    LogUtils.e(place);
                    etLocation.setText(place.getAddress());
                }
            }
        });

        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        etLocation.setOnTouchListener((v, event) -> {
            @SuppressLint("ClickableViewAccessibility") Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(getContext());
            placesIntent.launch(intent);
            return true;
        });
        getCurrencies();

        return view;
    }

    void getCurrencies(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("currencies");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currencies = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Currency currency = dataSnapshot.getValue(Currency.class);
                    currencies.add(currency);
                }
                CurrencyAdapter currencyAdapter = new CurrencyAdapter(getContext(),R.layout.item_currency,currencies);
                ddCurrency.setAdapter(currencyAdapter);
                ddCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Object item = parent.getItemAtPosition(position);
                        if (item instanceof Currency){
                            Currency currency = (Currency) item;

                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6969){
            if (resultCode == RESULT_OK){
                LogUtils.e(data.getData());
            }
        }
    }
}