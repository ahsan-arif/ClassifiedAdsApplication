package com.android.classifiedapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.adapters.CurrencyAdapter;
import com.android.classifiedapp.adapters.FilterCategoryAdapter;
import com.android.classifiedapp.adapters.FilterSubcategoriesAdapter;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.android.classifiedapp.models.SubCategory;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActivitySelectFilters extends AppCompatActivity implements FilterCategoryAdapter.FilterCategoriesAdapterListener, FilterSubcategoriesAdapter.FilterSubcategoriesAdapterListener {
    ImageView imgBack;
    AutoCompleteTextView ddCurrency;
    ArrayList<Currency> currencies;
    Currency selectedCurrency;
    ArrayList<Category> categories;
    RecyclerView rvCategories;
    RecyclerView rvSubcategories;
    Category selectedCategory;
    SubCategory selectedSubCategory;
    LinearLayout vgSubcategory;
    List<Place.Field> fields;
    TextView tvFilterPlace;
    ActivityResultLauncher<Intent> placesIntent;
    Double latitude,longitude;
    String address;
    TextView tvApplyFilters;
    EditText etFrom;
    EditText etTo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200)); // Replace R.color.your_status_bar_color with your desired color resource
        }
        setContentView(R.layout.activity_select_filters);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgBack = findViewById(R.id.img_back);
        ddCurrency = findViewById(R.id.dd_currency);
        rvCategories = findViewById(R.id.rv_categories);
        rvSubcategories = findViewById(R.id.rv_subcategories);
        vgSubcategory = findViewById(R.id.vg_subcategory);
        tvFilterPlace = findViewById(R.id.tv_filter_place);
        tvApplyFilters = findViewById(R.id.tv_apply_filters);
        etFrom = findViewById(R.id.et_from);
        etTo = findViewById(R.id.et_to);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Places.initialize(this, getString(R.string.places_api_key), Locale.US);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        getCurrencies();
        getCategories();

        placesIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    Place place  = Autocomplete.getPlaceFromIntent(data);
                    latitude= place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    address = place.getAddress();
                    LogUtils.e(place);
                    tvFilterPlace.setText(place.getAddress());
                }
            }
        });

        tvFilterPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("ClickableViewAccessibility") Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .build(ActivitySelectFilters.this);

                placesIntent.launch(intent);
            }
        });

        tvApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySelectFilters.this,ActivityFilteredAds.class);
                if (!etFrom.getText().toString().isEmpty()){
                    intent.putExtra("from",Double.parseDouble(etFrom.getText().toString()));
                }
                if (!etTo.getText().toString().isEmpty()){
                    intent.putExtra("to",Double.parseDouble(etTo.getText().toString()));
                }
                if (latitude!=null){
                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                }
                if (selectedCategory!=null){
                    intent.putExtra("category",selectedCategory);
                }
                if (selectedSubCategory!=null){
                    intent.putExtra("subcategory",selectedSubCategory);
                }
                if (selectedCurrency!=null){
                    intent.putExtra("currency",selectedCurrency);
                }
                startActivity(intent);
            }
        });
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
                CurrencyAdapter currencyAdapter = new CurrencyAdapter(ActivitySelectFilters.this,R.layout.item_currency,currencies);
                ddCurrency.setAdapter(currencyAdapter);
                ddCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Object item = parent.getItemAtPosition(position);
                        if (item instanceof Currency){
                            Currency currency = (Currency) item;
                            selectedCurrency = currency;
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void getCategories(){
        ProgressDialog progressDialog = new ProgressDialog(ActivitySelectFilters.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.fetching_categories));
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()){
                    categories = new ArrayList<>();
                    for(DataSnapshot categoryShot :snapshot.getChildren()){
                        Category category = new Category();
                        category.setId(categoryShot.child("id").getValue(String.class));
                        category.setName(categoryShot.child("name").getValue(String.class));
                        category.setId(categoryShot.child("id").getValue(String.class));

                        List<SubCategory> subCategories = new ArrayList<>();
                        if (categoryShot.hasChild("subCategories")){
                            for (DataSnapshot ds : categoryShot.child("subCategories").getChildren()){
                                SubCategory subCategory = ds.getValue(SubCategory.class);
                                subCategories.add(subCategory);
                                // LogUtils.e(subCategory.getName());
                            }
                        }

                        category.setSubCategories(subCategories);
                        categories.add(category);
                        setCategoriesAdapter(categories);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }

    void setCategoriesAdapter(ArrayList<Category> categories){
        rvCategories.setAdapter(new FilterCategoryAdapter(ActivitySelectFilters.this,categories,this));
        rvCategories.setLayoutManager(new LinearLayoutManager(ActivitySelectFilters.this,LinearLayoutManager.HORIZONTAL,false));
    }

    @Override
    public void onCategorySelected(Category category) {
        selectedCategory =category;
        ToastUtils.showShort(selectedCategory.getName()+" selected");
        if (!category.getSubCategories().isEmpty()){
            vgSubcategory.setVisibility(View.VISIBLE);
            rvSubcategories.setAdapter(new FilterSubcategoriesAdapter(ActivitySelectFilters.this,category.getSubCategories(),this));
            rvSubcategories.setLayoutManager(new LinearLayoutManager(ActivitySelectFilters.this,LinearLayoutManager.HORIZONTAL,false));
        }else{
            selectedSubCategory = null;
            vgSubcategory.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSubcategorySelected(SubCategory subCategory) {
        selectedSubCategory = subCategory;
        ToastUtils.showShort(subCategory.getName()+" selected");
    }
}