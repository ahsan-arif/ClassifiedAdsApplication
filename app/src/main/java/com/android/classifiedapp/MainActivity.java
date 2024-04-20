package com.android.classifiedapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

//import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
TextView tvSignup;
LinearLayout btnGoogleSignIn;
    private static final int RC_SIGN_IN = 9001;
    private ActivityResultLauncher<Intent> signInLauncher;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSignup = findViewById(R.id.tv_signup);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        FirebaseApp.initializeApp(getApplicationContext());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(this, gso);
        SpannableString spannableString = new SpannableString("Signup or login with email");

        // Create ClickableSpan for "Signup"
        ClickableSpan signupClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Add your signup functionality here
                Toast.makeText(MainActivity.this, "Signup Clicked", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, "Login Clicked", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
signIn(gso);
            }
        });

    }
    private void signIn(GoogleSignInOptions gso) {
        Intent signInIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Toast.makeText(this, "Sign in success: " + account.getEmail(), Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getMessage());
            Toast.makeText(this, "Sign in failed "+e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
}

