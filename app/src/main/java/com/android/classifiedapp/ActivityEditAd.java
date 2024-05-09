package com.android.classifiedapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.adapters.CategoriesRecyclerAdapter;
import com.android.classifiedapp.adapters.SubcategoriesRecyclerAdapter;
import com.android.classifiedapp.fragments.FragmentAddProduct;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.android.classifiedapp.models.SubCategory;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class ActivityEditAd extends AppCompatActivity {
    Ad ad;
    AutoCompleteTextView ddCurrency;
    TextInputEditText etLocation;
    ImageView image1,image2,image3,delete1,delete2,delete3;

    ActivityResultLauncher<Intent> placesIntent,imagePickerLauncher1,imagePickerLauncher2,imagePickerLauncher3;
    List<Place.Field> fields;
    TextInputEditText etCategory,etSubCategory;

    Category selectedCategory;
    SubCategory selectedSubcategory;

    TextInputLayout tiSubcat;
    Uri image1Uri,image2Uri,image3Uri;

    TextInputEditText etProductTitle,etDetails,etPrice;

    RadioGroup rgShipping,rgShippingPayer;
    LinearLayout vgShippingPayer;
    boolean isShippingAvailable;

    String shippingPayer;

    User currentUser;
    Double latitude,longitude;
    String address;
    List<String> adImageUrls;
    RadioButton rbYes,rbNo,rbMe,rbBuyer;
    ProgressBar progressImg1,progressImg2,progressImg3;
    TextView btnEditAd;
    ImageView imgBack;

    boolean isUnApprovedAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_ad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ad = getIntent().getParcelableExtra("ad");
        isUnApprovedAd = getIntent().getBooleanExtra("unApprovedAd",false);
        ddCurrency = findViewById(R.id.dd_currency);
        etLocation = findViewById(R.id.et_location);
        image1 = findViewById(R.id.image_1);
        image2 = findViewById(R.id.image_2);
        image3 = findViewById(R.id.image_3);
        delete1 = findViewById(R.id.delete_1);
        delete2 = findViewById(R.id.delete_2);
        delete3 = findViewById(R.id.delete_3);
        etCategory = findViewById(R.id.et_category);
        etSubCategory =findViewById(R.id.et_sub_category);
        tiSubcat = findViewById(R.id.ti_subcat);
        rgShipping = findViewById(R.id.rg_shipping);
        rgShippingPayer = findViewById(R.id.rg_shipping_payer);
        vgShippingPayer = findViewById(R.id.vg_shipping_payer);
        etProductTitle = findViewById(R.id.et_product_title);
        etDetails = findViewById(R.id.et_details);
        etPrice = findViewById(R.id.et_price);
        rbYes = findViewById(R.id.rb_yes);
        rbNo = findViewById(R.id.rb_no);
        rbMe = findViewById(R.id.rb_me);
        rbBuyer = findViewById(R.id.rb_buyer);
        latitude = ad.getLatitude();
        longitude = ad.getLongitude();
        address = ad.getAddress();
        progressImg1 = findViewById(R.id.progress_img_1);
        progressImg2 = findViewById(R.id.progress_img_2);
        progressImg3 = findViewById(R.id.progress_img_3);
        btnEditAd = findViewById(R.id.btn_edit_ad);
        imgBack = findViewById(R.id.img_back);
        Places.initialize(ActivityEditAd.this, getString(R.string.places_api_key), Locale.US);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
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
                    etLocation.setText(place.getAddress());
                }
            }
        });
        imagePickerLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image1Uri=data.getData();
                    if (!adImageUrls.isEmpty()){
                        deleteImage(adImageUrls.get(0),image1,image1Uri);
                    }
                    LogUtils.e(o.getData());
                }
            }
        });
        imagePickerLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image2Uri=data.getData();
                    if (adImageUrls.size()>=2){
                        deleteImage(adImageUrls.get(1),image2,image2Uri);
                    }else{
                        uploadImage(image2,image2Uri);
                    }
                    LogUtils.e(o.getData());
                }
            }
        });
        imagePickerLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image3Uri=data.getData();
                    if (adImageUrls.size()==3){
                        deleteImage(adImageUrls.get(2),image3,image3Uri);
                    }else{
                        uploadImage(image3,image3Uri);
                    }
                    LogUtils.e(o.getData());
                }
            }
        });
        etLocation.setOnClickListener((v) -> {
            @SuppressLint("ClickableViewAccessibility") Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(ActivityEditAd.this);
            placesIntent.launch(intent);
        });
        adImageUrls = ad.getUrls();
        if (adImageUrls.size()==3){
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(0)).into(image1);
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(1)).into(image2);
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(2)).into(image3);
        }else if (adImageUrls.size()==2){
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(0)).into(image1);
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(1)).into(image2);
        }else{
            Glide.with(ActivityEditAd.this).load(ad.getUrls().get(0)).into(image1);
        }
        etProductTitle.setText(ad.getTitle());
        etDetails.setText(ad.getDescription());
        etLocation.setText(ad.getAddress());
        etPrice.setText(ad.getPrice());
        ddCurrency.setText(ad.getCurrency());
        rgShipping.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = group.findViewById(selectedRadioButtonId);
                String selectedText = selectedRadioButton.getText().toString();
                if (selectedText.equals(getString(R.string.no))){
                    vgShippingPayer.setVisibility(View.INVISIBLE);
                    isShippingAvailable = false;
                    shippingPayer = null;
                }else{
                    vgShippingPayer.setVisibility(View.VISIBLE);
                    isShippingAvailable = true;
                }
                ToastUtils.showShort(selectedText);
            }
        });
        rgShippingPayer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = group.findViewById(selectedRadioButtonId);
                String selectedText = selectedRadioButton.getText().toString();
                if (selectedText.equals(getString(R.string.me))){
                    shippingPayer = getString(R.string.seller);
                }else{
                    shippingPayer=getString(R.string.buyer);
                }
            }
        });
        if (ad.isShippingAvailable()){
            rbYes.setChecked(true);
        }else{
            rbNo.setChecked(true);
        }
        if (ad.getShippingPayer()!=null){
            if (ad.getShippingPayer().equals(getString(R.string.me))){
                rbMe.setChecked(true);
            }else {
                rbBuyer.setChecked(true);
            }
        }

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(ActivityEditAd.this).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        requestPermissionsBasedOnSdk(imagePickerLauncher2);
                    }
                });
            }
        });
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(ActivityEditAd.this).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        if (b){
                            //startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
                            requestPermissionsBasedOnSdk(imagePickerLauncher1);

                        }else{
                            ToastUtils.showShort(getString(R.string.grant_all_permissions));
                            LogUtils.e(list1);
                        }
                    }
                });
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(ActivityEditAd.this).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        requestPermissionsBasedOnSdk(imagePickerLauncher3);
                    }
                });
            }
        });
        btnEditAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getCategoryDetails(ad.getCategoryId());

    }

    void getCategoryDetails(String categoryId){
        FirebaseDatabase.getInstance().getReference().child("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Category category = new Category();
                    category.setId(snapshot.child("id").getValue(String.class));
                    category.setName(snapshot.child("name").getValue(String.class));
                    category.setId(snapshot.child("id").getValue(String.class));

                    selectedCategory = category;

                    if (snapshot.hasChild("subCategories")){
                        for(DataSnapshot subCatShot : snapshot.child("subCategories").getChildren()){
                            SubCategory subCategory = subCatShot.getValue(SubCategory.class);
                            if (subCategory.getId().equals(ad.getSubcategoryId())){
                                etSubCategory.setText(subCategory.getName());
                                selectedSubcategory = subCategory;
                                tiSubcat.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    etCategory.setText(category.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void uploadImage(ImageView imageView,Uri imageUri){
        ProgressDialog progressDialog = new ProgressDialog(ActivityEditAd.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.uploading_image));
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ad_images").child(ad.getId()).child("image"+ UUID.randomUUID().toString());
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        String downloadUrl = uri.toString();
                        Glide.with(ActivityEditAd.this).load(downloadUrl).into(imageView);
                        adImageUrls.add(downloadUrl);
                        updateImageUrlsForPost(adImageUrls);
                    }
                });
            }
        });
    }
    void deleteImage(String oldUrl,ImageView imageView,Uri imageUri){
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                adImageUrls.remove(oldUrl);
                uploadImage(imageView,imageUri);
            }
        });
    }

    private void requestPermissionsBasedOnSdk(ActivityResultLauncher<Intent> activityResultLauncher) {
        int targetSdk = Build.VERSION.SDK_INT;
        String[] permissions;

        if (targetSdk >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API level 33) and above - use READ_MEDIA_IMAGES
            permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES};
        } else if (targetSdk >= Build.VERSION_CODES.R) {
            // Android 12 (API level 31) and up to 12L (level 32) - use READ_EXTERNAL_STORAGE
            permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            // Android 10 (API level 29) and below - use READ_EXTERNAL_STORAGE (hypothetical)
            // You likely wouldn't need to support such low API levels for requesting gallery access.
            permissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        PermissionX.init(this).permissions(permissions).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                if (allGranted) {
                    // All permissions granted, proceed with your action (e.g., open gallery)
                    String[] mimeTypes = {"image/png",
                            "image/jpg",
                            "image/jpeg"};
                    ImagePicker.with(ActivityEditAd.this)
                            .crop()
                            .galleryMimeTypes(mimeTypes)
                            .createIntent((Function1) new Function1() {

                                @Override
                                public Object invoke(Object o) {
                                    this.invoke((Intent) o);
                                    return Unit.INSTANCE;
                                }

                                public final void invoke(@NotNull Intent it) {
                                    Intrinsics.checkNotNullParameter(it, "it");
                                    activityResultLauncher.launch(it);
                                }
                            });

                } else {
                    ToastUtils.showShort(getString(R.string.grant_all_permissions));
                    LogUtils.e(deniedList);
                }
            }
        });
    }

    void updateImageUrlsForPost(List<String> adImageUrls){
        DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId()).child("urls");
        databaseReference.setValue(adImageUrls);
        ToastUtils.showShort(getString(R.string.image_updated));
    }

    boolean validateForm(){
        String cannotBeEmpty = getString(R.string.cannot_be_empty);
        if (etPrice.getText().toString().trim().isEmpty()){
            etPrice.setError(cannotBeEmpty);
            return false;
        }else if (selectedCategory == null){
            etCategory.setError(cannotBeEmpty);
            return false;
        }else if (latitude ==null){
            etLocation.setError(cannotBeEmpty);
            return false;
        }else if (etDetails.getText().toString().trim().isEmpty()){
            etDetails.setError(cannotBeEmpty);
            return false;
        }
        if (isShippingAvailable && shippingPayer==null){
            ToastUtils.showShort(getString(R.string.specify_shipping_payer));
            return false;
        }

/*        if (!selectedCategory.getSubCategories().isEmpty()){
            if (selectedSubcategory==null){
                ToastUtils.showShort(getString(R.string.please_select_subcategory));
                return false;
            }
        }*/
        return true;
    }

    void updatePost(){
        if (!validateForm()){
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child(ad.getId());
        // All uploads are finished, handle the download URLs
        // Set the list of download URLs for the Ad object
        ad.setTitle(etProductTitle.getText().toString().trim());
        ad.setPrice(etPrice.getText().toString().trim());
        ad.setUpdatedOn(String.valueOf(System.currentTimeMillis()));
        ad.setPostedBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if (isUnApprovedAd){
            ad.setStatus(getString(R.string.pending_approval));
        }else{
            ad.setStatus(getString(R.string.pending_approval));
        }
        ad.setAddress(address);
        ad.setLatitude(latitude);
        ad.setLongitude(longitude);
        if (isShippingAvailable){
            ad.setShippingPayer(shippingPayer);
        }else{
            ad.setShippingPayer(null);
        }
        ad.setShippingAvailable(isShippingAvailable);
        ad.setDescription(etDetails.getText().toString().trim());
        if (selectedCategory!=null){
            ad.setSubcategoryId(selectedSubcategory.getId());
        }

        // Save the Ad object in the database
        // (Assuming you have a method to save Ad objects in the database)
        databaseReference.setValue(ad);
        ToastUtils.showShort(getString(R.string.ad_updated));
        finish();
        //saveAdToDatabase(ad);
    }

}
