package com.android.classifiedapp;

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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

//import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
TextView tvSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSignup = findViewById(R.id.tv_signup);
        FirebaseApp.initializeApp(getApplicationContext());
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

    }
}

