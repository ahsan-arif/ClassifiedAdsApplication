package com.android.classifiedapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.classifiedapp.ActivityMyAds;
import com.android.classifiedapp.ActivityMyOrders;
import com.android.classifiedapp.ActivityMyWishlist;
import com.android.classifiedapp.ActivityVerifyLogin;
import com.android.classifiedapp.MainActivity;
import com.android.classifiedapp.R;
import com.android.classifiedapp.adapters.MyAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.android.classifiedapp.models.PlatformPrefs;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProfile extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextInputEditText etName,etEmail;
    TextView tvLogout;
    FloatingActionButton fabUpdateProfileImg;

    Uri image1Uri;
    ActivityResultLauncher<Intent> imagePickerLauncher1;

    ProgressBar progressCircular;
    CircleImageView imgProfile;
    CardView cardWishlist;
    CardView cardMyListings,cardMyOrders;

    TextView tvDeleteAccount,tvCancelMembership;

    Context context;

    TextView tvMemberTill;

    CardView cardPremium;
    CardView cardGoPremium;
    TextView tvBuyPro;

    public FragmentProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentProfile newInstance(String param1, String param2) {
        FragmentProfile fragment = new FragmentProfile();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        tvLogout = view.findViewById(R.id.tv_logout);
        fabUpdateProfileImg = view.findViewById(R.id.fab_update_profile_img);
        progressCircular = view.findViewById(R.id.progress_circular);
        imgProfile = view.findViewById(R.id.img_profile);
        cardWishlist = view.findViewById(R.id.card_wishlist);
        cardMyListings = view.findViewById(R.id.card_my_listings);
        tvDeleteAccount = view.findViewById(R.id.tv_delete_account);
        cardMyOrders = view.findViewById(R.id.card_my_orders);
        tvCancelMembership = view.findViewById(R.id.tv_cancel_membership);
        tvMemberTill = view.findViewById(R.id.tv_member_till);
        cardPremium = view.findViewById(R.id.card_premium);
        cardGoPremium = view.findViewById(R.id.card_go_premium);
        tvBuyPro = view.findViewById(R.id.tv_buy_pro);

        FirebaseDatabase.getInstance().getReference().child("platform_prefs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PlatformPrefs prefs = snapshot.getValue(PlatformPrefs.class);
                    tvBuyPro.setText(getString(R.string.buy_premium)+" "+getString(R.string.in_just)+" EUR "+"0.99");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getUserDetails(user.getUid());
        //etName.setText(user.getDisplayName());
        etEmail.setText(user.getEmail());

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(context, MainActivity.class));
                getActivity().finish();
            }
        });

        imagePickerLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image1Uri=data.getData();
                    uploadImage(FirebaseAuth.getInstance().getCurrentUser().getUid(),image1Uri);
                    //image1.setImageBitmap(img);

                    LogUtils.e(o.getData());
                }
            }
        });

        fabUpdateProfileImg.setOnClickListener(new View.OnClickListener() {
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

        cardWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivityMyWishlist.class));
            }
        });
        cardMyListings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivityMyAds.class));
            }
        });
        tvDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
        cardMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ActivityMyOrders.class));
            }
        });
        cardGoPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Map<String, Object> updates = new HashMap<>();
                updates.put("premiumUser", true);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                // Add one month to the calendar
                calendar.add(Calendar.MONTH, 1);
                // Get the new time in milliseconds
                long newTimeMillis = calendar.getTimeInMillis();
                updates.put("benefitsExpiry",newTimeMillis);
                databaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            cardGoPremium.setVisibility(View.GONE);
                            cardPremium.setVisibility(View.VISIBLE);

                            Date date = new Date(newTimeMillis);

                            // Format the date
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            String formattedDate = sdf.format(date);
                            LogUtils.e("benefits updated");
                        }else{
                            LogUtils.e("failed to update benefits");
                        }
                    }
                });*/
                showGoPremiumSheet();
            }
        });
        tvCancelMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("platform_prefs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            PlatformPrefs prefs =snapshot.getValue(PlatformPrefs.class);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            // Create a Map to hold the child updates
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("premiumUser", false);
                            updates.put("maximumOrdersAvailable",prefs.getMaximumOrdersAllowed() );
                            updates.put("freeMessagesAvailable",prefs.getFreeMessagesCount());
                            updates.put("freeAdsAvailable",prefs.getFreeAdsCount());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            // Add one month to the calendar
                            calendar.add(Calendar.MONTH, 1);
                            // Get the new time in milliseconds
                            long newTimeMillis = calendar.getTimeInMillis();
                            updates.put("benefitsExpiry",newTimeMillis);

                            databaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        cardGoPremium.setVisibility(View.VISIBLE);
                                        cardPremium.setVisibility(View.GONE);
                                        LogUtils.e("benefits updated");
                                    }else{
                                        LogUtils.e("failed to update benefits");
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return view;
    }

    void getUserDetails(String userId){
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()){
                        User user = snapshot.getValue(User.class);

                        if (user!=null){
                            etName.setText(user.getName());
                            if (user.getProfileImage()!=null){
                                Glide.with(context).load(user.getProfileImage()).into(imgProfile);
                            }
                            if (user.isPremiumUser()){
                                long now = System.currentTimeMillis();
                                if (now>user.getBenefitsExpiry()){
                                    cardGoPremium.setVisibility(View.VISIBLE);
                                    cardPremium.setVisibility(View.GONE);
                                }else{
                                    cardGoPremium.setVisibility(View.GONE);
                                    cardPremium.setVisibility(View.VISIBLE);
                                    Date date = new Date(user.getBenefitsExpiry());

                                    // Format the date
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                    String formattedDate = sdf.format(date);

                                    tvMemberTill.setText(context.getString(R.string.expires_on)+" "+formattedDate);
                                }

                            }else{
                                cardPremium.setVisibility(View.GONE);
                                cardGoPremium.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

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
            LogUtils.e(targetSdk);
            // Android 13 (API level 33) and above - use READ_MEDIA_IMAGES
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else if (targetSdk >= Build.VERSION_CODES.R) {
            LogUtils.e(targetSdk);
            // Android 12 (API level 31) and up to 12L (level 32) - use READ_EXTERNAL_STORAGE
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            LogUtils.e(targetSdk);
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

    void uploadImage(String userId, Uri uri) {
        fabUpdateProfileImg.setVisibility(View.GONE);
        progressCircular.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId).child("image"+ userId);

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fabUpdateProfileImg.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        Glide.with(context).load(downloadUrl).into(imgProfile);
                        //set image url in user object
                        updaterUserImage(downloadUrl,userId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fabUpdateProfileImg.setVisibility(View.VISIBLE);
                        progressCircular.setVisibility(View.GONE);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fabUpdateProfileImg.setVisibility(View.VISIBLE);
                progressCircular.setVisibility(View.GONE);

            }
        });
    }

    void updaterUserImage(String imgUrl,String currentUserId){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("profileImage");
        databaseReference.setValue(imgUrl);
    }

    void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(getString(R.string.are_you_sure));

        builder.setMessage(getString(R.string.do_you_really));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ToastUtils.showShort("yes");
                startActivity(new Intent(context, ActivityVerifyLogin.class)
                        .putExtra("email",FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .putExtra("isDeleteProfile",true));
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    void showGoPremiumSheet(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_pay_for_featured_ad);
        TextView tvTitle = bottomSheetDialog.findViewById(R.id.tv_title);
        TextView tvSubtitle = bottomSheetDialog.findViewById(R.id.tv_subtitle);

        tvTitle.setText(getString(R.string.buy_premium));
        tvSubtitle.setText(getString(R.string.buy_premium)+" "+getString(R.string.in_just)+" EUR "+"0.99");
        PaymentButtonContainer paymentButtonContainer = bottomSheetDialog.findViewById(R.id.payment_button_container);

        paymentButtonContainer.setup( new CreateOrder() {
            @Override
            public void create(@NotNull CreateOrderActions createOrderActions) {
                LogUtils.e("create: ");
                ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                purchaseUnits.add(
                        new PurchaseUnit.Builder()
                                .amount(
                                        new Amount.Builder()
                                                .currencyCode(CurrencyCode.USD)
                                                .value("0.99")
                                                .build()
                                )
                                .build()
                );
                OrderRequest order = new OrderRequest(
                        OrderIntent.CAPTURE,
                        new AppContext.Builder()
                                .userAction(UserAction.PAY_NOW)
                                .build(),
                        purchaseUnits
                );
                createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
            }
        }, new OnApprove() {
            @Override
            public void onApprove(@NotNull Approval approval) {
                approval.getOrderActions().capture(new OnCaptureComplete() {
                    @Override
                    public void onCaptureComplete(@NotNull CaptureOrderResult result) {
                        LogUtils.e(String.format("CaptureOrderResult: %s", result));
                        ToastUtils.showShort( "Successful", Toast.LENGTH_SHORT);
                        bottomSheetDialog.dismiss();

                        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("premiumUser", true);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        // Add one month to the calendar
                        calendar.add(Calendar.MONTH, 1);
                        // Get the new time in milliseconds
                        long newTimeMillis = calendar.getTimeInMillis();
                        updates.put("benefitsExpiry",newTimeMillis);
                        databaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    cardGoPremium.setVisibility(View.GONE);
                                    cardPremium.setVisibility(View.VISIBLE);

                                    Date date = new Date(newTimeMillis);

                                    // Format the date
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                    String formattedDate = sdf.format(date);
                                    LogUtils.e("benefits updated");
                                }else{
                                    LogUtils.e("failed to update benefits");
                                }
                            }
                        });
                    }
                });
            }
        });

        bottomSheetDialog.show();
    }
}