package com.android.classifiedapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.CategoriesRecyclerAdapter;
import com.android.classifiedapp.adapters.CurrencyAdapter;
import com.android.classifiedapp.adapters.SubcategoriesRecyclerAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.Category;
import com.android.classifiedapp.models.Currency;
import com.android.classifiedapp.models.SubCategory;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAddProduct#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAddProduct extends Fragment implements CategoriesRecyclerAdapter.OnCategorySelectedListener, SubcategoriesRecyclerAdapter.SubcategorySelectionListener {

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
    ImageView image1,image2,image3,delete1,delete2,delete3;

    boolean isImage1Selected,isImage2Selected,isImage3Selected;

    ActivityResultLauncher<Intent> placesIntent,imagePickerLauncher1,imagePickerLauncher2,imagePickerLauncher3;
    List<Place.Field> fields;
    Currency selectedCurrency;
    TextInputEditText etCategory,etSubCategory;

    ArrayList<Category> categories;

    BottomSheetDialog  categoriesSheet,subCategoriesSheet;
    CategoriesRecyclerAdapter categoriesRecyclerAdapter;
    SubcategoriesRecyclerAdapter subcategoriesRecyclerAdapter;
    Category selectedCategory;
    SubCategory selectedSubcategory;

    List<SubCategory> subCategories;
    TextInputLayout tiSubcat;
    Uri image1Uri,image2Uri,image3Uri;

    TextInputEditText etProductTitle,etDetails,etPrice;

    RadioGroup rgShipping,rgShippingPayer;
    LinearLayout vgShippingPayer;
    boolean isShippingAvailable;

    String shippingPayer;

    TextView btnCreateAd;

    User currentUser;

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
        image1 = view.findViewById(R.id.image_1);
        image2 = view.findViewById(R.id.image_2);
        image3 = view.findViewById(R.id.image_3);
        delete1 = view.findViewById(R.id.delete_1);
        delete2 = view.findViewById(R.id.delete_2);
        delete3 = view.findViewById(R.id.delete_3);
        etCategory = view.findViewById(R.id.et_category);
        etSubCategory = view.findViewById(R.id.et_sub_category);
        tiSubcat = view.findViewById(R.id.ti_subcat);
        rgShipping = view.findViewById(R.id.rg_shipping);
        rgShippingPayer = view.findViewById(R.id.rg_shipping_payer);
        vgShippingPayer = view.findViewById(R.id.vg_shipping_payer);
        etProductTitle = view.findViewById(R.id.et_product_title);
        etDetails = view.findViewById(R.id.et_details);
        etPrice = view.findViewById(R.id.et_price);
        btnCreateAd = view.findViewById(R.id.btn_create_ad);
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
        imagePickerLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image1Uri=data.getData();
                    Bitmap img= null;
                    try {
                        LogUtils.e("asd");
                        img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image1Uri);
                        image1.setImageBitmap(img);
                        delete1.setVisibility(View.VISIBLE);
                        isImage1Selected = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                    Bitmap img= null;
                    try {
                        img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image2Uri);
                        image2.setImageBitmap(img);
                        delete2.setVisibility(View.VISIBLE);
                        isImage2Selected = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                    Bitmap img= null;
                    try {
                        img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image3Uri);
                        image3.setImageBitmap(img);
                        delete3.setVisibility(View.VISIBLE);
                        isImage3Selected = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    LogUtils.e(o.getData());
                }
            }
        });
        etLocation.setOnTouchListener((v, event) -> {
            @SuppressLint("ClickableViewAccessibility") Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(getContext());
            placesIntent.launch(intent);
            return true;
        });
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(getActivity()).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        if (b){
                            //startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
                            requestPermissionsBasedOnSdk(imagePickerLauncher1);

                        }else{
                            ToastUtils.showShort("Grant all permission to proceed");
                            LogUtils.e(list1);
                        }
                    }
                });
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(getActivity()).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        requestPermissionsBasedOnSdk(imagePickerLauncher2);
                    }
                });
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(getActivity()).permissions().request(new RequestCallback() {
                    @Override
                    public void onResult(boolean b, @NonNull List<String> list, @NonNull List<String> list1) {
                        requestPermissionsBasedOnSdk(imagePickerLauncher3);
                    }
                });
            }
        });
        delete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image1.setImageResource(R.drawable.add_image);
                delete1.setVisibility(View.GONE);
            }
        });
        delete2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image2.setImageResource(R.drawable.add_image);
                delete2.setVisibility(View.GONE);
            }
        });
        delete3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image3.setImageResource(R.drawable.add_image);
                delete3.setVisibility(View.GONE);
            }
        });
        etCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoriesSheet();
            }
        });
        etSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubcategoriesSheet();
            }
        });
        rgShipping.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = group.findViewById(selectedRadioButtonId);
                String selectedText = selectedRadioButton.getText().toString();
                if (selectedText.equals(getString(R.string.no))){
                    vgShippingPayer.setVisibility(View.INVISIBLE);
                    isShippingAvailable = false;
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
        btnCreateAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImagesAndSavePost();
            }
        });
        getCurrencies();
        //getCurrentUser();
        getCategories();

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

        private void requestPermissionsBasedOnSdk(ActivityResultLauncher<Intent> activityResultLauncher) {
            int targetSdk = Build.VERSION.SDK_INT;
            String[] permissions;

            if (targetSdk >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (API level 33) and above - use READ_MEDIA_IMAGES
                    permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
            } else if (targetSdk >= Build.VERSION_CODES.R) {
                // Android 12 (API level 31) and up to 12L (level 32) - use READ_EXTERNAL_STORAGE
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                // Android 10 (API level 29) and below - use READ_EXTERNAL_STORAGE (hypothetical)
                // You likely wouldn't need to support such low API levels for requesting gallery access.
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            }

            PermissionX.init(this).permissions(permissions).request(new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                    if (allGranted) {
                        // All permissions granted, proceed with your action (e.g., open gallery)
                        String[] mimeTypes = {"image/png",
                                "image/jpg",
                                "image/jpeg"};
                        ImagePicker.with(getActivity())
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
                        ToastUtils.showShort("Grant all permissions to proceed");
                        LogUtils.e(deniedList);
                    }
                }
            });
        }

        void getCategories(){
            ProgressDialog progressDialog = new ProgressDialog(getContext());
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

                        for (DataSnapshot ds : categoryShot.child("subCategories").getChildren()){
                            SubCategory subCategory = ds.getValue(SubCategory.class);
                            subCategories.add(subCategory);
                           // LogUtils.e(subCategory.getName());
                        }
                        category.setSubCategories(subCategories);
                        categories.add(category);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
        }

        void showCategoriesSheet(){
            categoriesSheet = new BottomSheetDialog(getContext());
            categoriesSheet.setContentView(R.layout.bottom_sheet_dialog_categories_layout);
            TextView tvTitle = categoriesSheet.findViewById(R.id.tv_title);
            tvTitle.setText(R.string.select_category);
            ImageView imgClose = categoriesSheet.findViewById(R.id.img_close);
            RecyclerView rvCategories = categoriesSheet.findViewById(R.id.rv_categories);
             categoriesRecyclerAdapter = new CategoriesRecyclerAdapter(categories,getContext(),this);
             rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCategories.setAdapter(categoriesRecyclerAdapter);
            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoriesSheet.dismiss();
                }
            });
            categoriesSheet.show();

        }
    void showSubcategoriesSheet(){
        subCategoriesSheet = new BottomSheetDialog(getContext());
        subCategoriesSheet.setContentView(R.layout.bottom_sheet_dialog_categories_layout);
        TextView tvTitle = subCategoriesSheet.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.select_category);
        ImageView imgClose = subCategoriesSheet.findViewById(R.id.img_close);
        RecyclerView rvCategories = subCategoriesSheet.findViewById(R.id.rv_categories);
        subcategoriesRecyclerAdapter = new SubcategoriesRecyclerAdapter(subCategories,getContext(),this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(subcategoriesRecyclerAdapter);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCategoriesSheet.dismiss();
            }
        });
        subCategoriesSheet.show();

    }

    @Override
    public void onCategorySelected(Category category) {
        etCategory.setText(category.getName());
        selectedCategory = category;
        categoriesSheet.dismiss();
        if (category.getSubCategories().isEmpty()){
            tiSubcat.setVisibility(View.GONE);
        }else{
            tiSubcat.setVisibility(View.VISIBLE);
            subCategories = new ArrayList<>();
            subCategories = category.getSubCategories();
        }
    }

    @Override
    public void onSubcategorySelected(SubCategory subCategory) {
        selectedSubcategory = subCategory;
        etSubCategory.setText(subCategory.getName());
        subCategoriesSheet.dismiss();
    }

    void uploadImagesAndSavePost(){
        if (!validateForm()){
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.creating_ad));
        progressDialog.setCancelable(false);
        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").push();
        String adId = databaseReference.getKey();
        List<Uri> uris =new ArrayList<>();
        if (isImage1Selected){
            uris.add(image1Uri);
        }if (isImage2Selected){
            uris.add(image2Uri);
        }if (isImage3Selected){
            uris.add(image3Uri);
        }

        // Call uploadImages() method
        uploadImages(adId, uris, new OnAllUploadsFinishedListener() {
            @Override
            public void onAllUploadsFinished(List<String> downloadUrls) {
                progressDialog.dismiss();
                // All uploads are finished, handle the download URLs
                // Set the list of download URLs for the Ad object
                Ad ad = new Ad();
                ad.setId(adId);
                ad.setCategoryId(selectedCategory.getId());
                ad.setTitle(etProductTitle.getText().toString());
                ad.setCurrency(selectedCurrency.getCurrency());
                ad.setPrice(etPrice.getText().toString());
                ad.setPostedOn(String.valueOf(System.currentTimeMillis()));
                ad.setPostedBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
                if (selectedCategory!=null){
                    ad.setSubcategoryId(selectedSubcategory.getId());
                }
                ad.setUrls(downloadUrls);

                // Save the Ad object in the database
                // (Assuming you have a method to save Ad objects in the database)
                databaseReference.setValue(ad);
                ToastUtils.showShort(getString(R.string.ad_created));
               //saveAdToDatabase(ad);
            }
        });

    }

  /*  void uploadImage(String adId,Uri uri){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ad_images").child(adId).child("image"+ UUID.randomUUID().toString());
    storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                }
            });
        }
    });
    }*/

    public interface OnAllUploadsFinishedListener {
        void onAllUploadsFinished(List<String> downloadUrls);
    }
    void uploadImages(String adId, List<Uri> uris, final OnAllUploadsFinishedListener listener) {
        final List<String> downloadUrls = new ArrayList<>();
        final int totalUploads = uris.size();
        final int[] uploadsCompleted = {0};

        for (Uri uri : uris) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ad_images").child(adId).child("image"+ UUID.randomUUID().toString());

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            downloadUrls.add(downloadUrl);
                            uploadsCompleted[0]++;

                            // Check if all uploads are finished
                            if (uploadsCompleted[0] == totalUploads) {
                                // Notify listener when all uploads are finished
                                listener.onAllUploadsFinished(downloadUrls);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle any errors
                            // Since upload failed, count it as completed anyway
                            uploadsCompleted[0]++;

                            // Check if all uploads are finished
                            if (uploadsCompleted[0] == totalUploads) {
                                // Notify listener when all uploads are finished
                                listener.onAllUploadsFinished(downloadUrls);
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle any errors
                    // Since upload failed, count it as completed anyway
                    LogUtils.e(e.getMessage());
                    uploadsCompleted[0]++;

                    // Check if all uploads are finished
                    if (uploadsCompleted[0] == totalUploads) {
                        // Notify listener when all uploads are finished
                        listener.onAllUploadsFinished(downloadUrls);
                    }
                }
            });
        }
    }

    boolean validateForm(){
        String cannotBeEmpty = getString(R.string.cannot_be_empty);
        if (!isImage3Selected&&!isImage2Selected&&!isImage1Selected){
            return false;
        }else if (etPrice.getText().toString().isEmpty()){
            etPrice.setError(cannotBeEmpty);
            return false;
        }else if(selectedCurrency==null){
            ddCurrency.setError(cannotBeEmpty);
            return false;
        }else if (selectedCategory == null){
            etCategory.setError(cannotBeEmpty);
            return false;
        }
        return true;
    }

    void getCurrentUser(){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        LogUtils.e(userEmail);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = databaseReference.orderByChild("email").equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    LogUtils.e(snapshot);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        currentUser = new User();
                        currentUser.setEmail(dataSnapshot.child("email").getValue(String.class));
                       // LogUtils.e(dataSnapshot.child("email").getValue(String.class));
                        currentUser.setName(dataSnapshot.child("name").getValue(String.class));
                        if (dataSnapshot.hasChild("profileImage")){
                            currentUser.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}