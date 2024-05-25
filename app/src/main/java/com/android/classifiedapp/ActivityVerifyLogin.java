package com.android.classifiedapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.adapters.MyAdsAdapter;
import com.android.classifiedapp.models.Ad;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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

import java.util.ArrayList;
import java.util.List;

public class ActivityVerifyLogin extends AppCompatActivity {
    TextInputEditText etPassword;
    TextView tvHaveAccount,tvProvidePassword;
    TextView btnLogin,tvForgot;
    String email;
    boolean isDeleteProfile;
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
        setContentView(R.layout.activity_verify_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = getIntent().getStringExtra("email");
        isDeleteProfile = getIntent().getBooleanExtra("isDeleteProfile",false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        etPassword = findViewById(R.id.et_password);
        tvForgot = findViewById(R.id.tv_forgot);
        btnLogin = findViewById(R.id.btn_login);
        tvHaveAccount = findViewById(R.id.tv_have_account);
        tvProvidePassword = findViewById(R.id.tv_provide_password);

        tvProvidePassword.setText(getString(R.string.please_enter_your_password_to_login_to_the_system)+" "+email);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( email.isEmpty()){
                    ToastUtils.showShort(getString(R.string.invalid_email));
                }
                String password = etPassword.getText().toString();

                if (password.trim().isEmpty()){
                    etPassword.setError("Cannot be empty");
                    return;
                }
                if (!isDeleteProfile){
                    ProgressDialog progressDialog = new ProgressDialog(ActivityVerifyLogin.this);
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setMessage("Logging in");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent("com.android.classifiedapp.ACTION_LOGIN_SUCCESS");
                                        sendBroadcast(intent);
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent(ActivityVerifyLogin.this,Home.class));
                                        // Login successful, navigate to main activity

                                        // Update UI or perform actions based on successful login
                                    } else {
                                        // Login failed, handle error
                                        Exception e = task.getException();
                                        LogUtils.e(e.getMessage());
                                        ToastUtils.showShort(e.getMessage());
                                        // Display error message to the user
                                    }
                                    // Hide progress bar (optional)
                                }
                            });
                }else{
                    showAlert();
                }

            }
        });
        tvHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityVerifyLogin.this,MainActivity.class));
                finish();
            }
        });

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityVerifyLogin.this,ActivityForgotPassword.class));
            }
        });
    }

    void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityVerifyLogin.this);

        builder.setTitle(getString(R.string.are_you_sure));

        builder.setMessage(getString(R.string.do_you_really2));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 //ToastUtils.showShort("yes");
                 String password = etPassword.getText().toString().trim();
                 FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, password);
                ToastUtils.showShort(getString(R.string.authenticating));

                if (user!=null){
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                ToastUtils.showShort("Authentication successful");
                                getAdsForDeletion(user.getUid());
                            }else{
                                ToastUtils.showShort("Authentication unsuccessful");
                            }
                        }
                    });
                }else{
                    ToastUtils.showShort("Please login to proceed.");
                    startActivity(new Intent(ActivityVerifyLogin.this, MainActivity.class));
                    finish();
                }


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    void getAdsForDeletion(String currentUserId){
        ProgressDialog progressDialog = new ProgressDialog(ActivityVerifyLogin.this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.deleting_account));
        progressDialog.show();
        List<Ad> ads = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("ads").orderByChild("postedBy").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAds = (int) snapshot.getChildrenCount();
                    LogUtils.e(totalAds);
                    final int[] deletedAds = {0}; // Counter for deleted ads
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Ad ad = dataSnapshot.getValue(Ad.class);
                        ads.add(ad);
                    }

                    for  (Ad ad : ads){
                        removeAd(ad, new OnAdDeletedListener() {
                            @Override
                            public void onAdDeleted() {
                                deletedAds[0]++;
                                LogUtils.e(deletedAds ," total ads ",totalAds);
                                if (totalAds==deletedAds[0]){
                                    ToastUtils.showShort("All ads deleted");
                                    progressDialog.dismiss();
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                                    databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    startActivity(new Intent(ActivityVerifyLogin.this,MainActivity.class));
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }

                }else{
                    progressDialog.dismiss();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                    databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(ActivityVerifyLogin.this,MainActivity.class));
                                    finish();
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
progressDialog.dismiss();
ToastUtils.showShort(error.getMessage());
            }
        });

    }

    void removeAd(Ad ad,OnAdDeletedListener listener) {

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<String> imageUrls = ad.getUrls();

        // Recursive function to delete images
        deleteImagesRecursive(imageUrls, 0, new MyAdsAdapter.OnDeleteImageListener() {
            @Override
            public void onDeleteImageSuccess() {
                // All images deleted successfully, now remove ad from Realtime Database
                databaseRef.child("ads").child(ad.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onAdDeleted();
                        Toast.makeText(ActivityVerifyLogin.this, "Ad deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ActivityVerifyLogin.this, "Failed to delete ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDeleteImageFailure(String errorMessage) {
                Toast.makeText(ActivityVerifyLogin.this, "Failed to delete image: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void deleteImagesRecursive(List<String> imageUrls, int index, MyAdsAdapter.OnDeleteImageListener listener) {
        if (index >= imageUrls.size()) {
            // All images deleted successfully
            listener.onDeleteImageSuccess();
            return;
        }

        String imageUrl = imageUrls.get(index);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully, proceed to delete next image
                deleteImagesRecursive(imageUrls, index + 1, listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to delete image
                listener.onDeleteImageFailure(e.getMessage());
            }
        });
    }

    interface OnAdDeletedListener {
        void onAdDeleted();
    }
}