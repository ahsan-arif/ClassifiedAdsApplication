package com.android.classifiedapp;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.classifiedapp.models.User;
import com.android.classifiedapp.utilities.SharedPrefManager;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

//import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
TextView tvSignup;
LinearLayout btnGoogleSignIn;
    GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 9001;
    private ActivityResultLauncher<Intent> signInLauncher;
    private static final String TAG = "MainActivity";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200)); // Replace R.color.your_status_bar_color with your desired color resource
        }
        setContentView(R.layout.activity_main);
        tvSignup = findViewById(R.id.tv_signup);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.places_api_key));
        }
        if (user!=null){
            LogUtils.e(user.getEmail());
            startActivity(new Intent(MainActivity.this,Home.class));
            finish();
        }/*else{
            ToastUtils.showShort("no user logged in");
        }*/
         gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);
        signInClient.signOut();
        SpannableString spannableString = new SpannableString(getString(R.string.signup_or_login));

        // Create ClickableSpan for "Signup"
        ClickableSpan signupClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Add your signup functionality here
                //Toast.makeText(MainActivity.this, "Signup Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getColor(R.color.black));
            }
        };

        // Create ClickableSpan for "Login"
        ClickableSpan loginClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Add your login functionality here
                //Toast.makeText(MainActivity.this, "Login Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,ActivityLogin.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getColor(R.color.black));
            }
        };

        // Set the clickable spans and styles
        spannableString.setSpan(signupClickableSpan, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(loginClickableSpan, 10, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 10, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to TextView
        tvSignup.setMovementMethod(LinkMovementMethod.getInstance());
        tvSignup.setText(spannableString);

        signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                handleSignInResult(result.getData());
            } else {
                Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
signIn(signInClient);
            }
        });

    }
    private void signIn(GoogleSignInClient signInClient) {
        Intent signInIntent = signInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            LogUtils.e(account.getEmail(),account.getId());
            Toast.makeText(this, getString(R.string.sign_in_success) + account.getEmail(), Toast.LENGTH_SHORT).show();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            databaseReference.orderByChild("email").equalTo(account.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        startActivity(new Intent(MainActivity.this,ActivityVerifyLogin.class).putExtra("email",account.getEmail()));
                    }else{
                        startActivity(new Intent(MainActivity.this,SignUp.class)
                                .putExtra("fname",account.getDisplayName())
                                .putExtra("email",account.getEmail())
                                .putExtra("isEmailSignIn",true));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
           /* FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        LogUtils.e(snapshot);
                        for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getEmail()==null){
                                LogUtils.e("here");
                                startActivity(new Intent(MainActivity.this,SignUp.class)
                                        .putExtra("fname",account.getDisplayName())
                                        .putExtra("email",account.getEmail())
                                        .putExtra("isEmailSignIn",true));
                            }else {
                                LogUtils.e("here");
                                if (user.getEmail().equals(account.getEmail()) && user.getRole().equals("user")){
                                    startActivity(new Intent(MainActivity.this,ActivityVerifyLogin.class).putExtra("email",account.getEmail()));
                                    return;
                                }else if (user.getEmail().equals(account.getEmail())&& user.getRole().equals("admin")){
                                    ToastUtils.showShort(getString(R.string.admin_cannot));
                                    GoogleSignInClient signInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                                    signInClient.signOut();
                                    return;
                                }
                            }
                        }


                    }else{
                        startActivity(new Intent(MainActivity.this,SignUp.class)
                                .putExtra("fname",account.getDisplayName())
                                .putExtra("email",account.getEmail())
                                .putExtra("isEmailSignIn",true));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getMessage());
            Toast.makeText(this, getString(R.string.sign_in_failed)+e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
}

