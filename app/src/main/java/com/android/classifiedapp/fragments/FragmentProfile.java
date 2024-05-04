package com.android.classifiedapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
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
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.classifiedapp.MainActivity;
import com.android.classifiedapp.R;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class FragmentProfile extends Fragment {

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getUserDetails(user.getUid());
        //etName.setText(user.getDisplayName());
        etEmail.setText(user.getEmail());

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
            }
        });

        imagePickerLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    image1Uri=data.getData();
                        LogUtils.e("asd");
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

        return view;
    }

    void getUserDetails(String userId){
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    etName.setText(user.getName());

                    if (user.getProfileImage()!=null){
                        Glide.with(getContext()).load(user.getProfileImage()).into(imgProfile);
                    }
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
                            Glide.with(getContext()).load(downloadUrl).into(imgProfile);
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
}