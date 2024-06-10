package com.android.classifiedapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.classifiedapp.models.PlatformPrefs;
import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {
    ImageView imgBack;
    TextView btnCreateAccount;
    TextInputEditText etName;
    TextInputEditText etEmail;
    TextInputEditText etPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200)); // Replace R.color.your_status_bar_color with your desired color resource
        }
        setContentView(R.layout.activity_sign_up);
        imgBack = findViewById(R.id.img_back);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString();
                if (!email.isEmpty()){
                    if (email.contains("hotmail")|| email.contains("gmail")||email.contains("live")||email.contains("outlook")){
                        btnCreateAccount.setEnabled(true);
                        btnCreateAccount.setBackgroundResource(R.drawable.btn_sign_in_opts);
                    }else{
                        LogUtils.e("In else");
                        etEmail.setError(getString(R.string.only_google_allowed));
                        btnCreateAccount.setEnabled(false);
                        btnCreateAccount.setBackgroundResource(R.drawable.bg_report_btn);
                    }
                    LogUtils.e(email);
                }
            }
        });
        etPassword = findViewById(R.id.et_password);

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (!password.isEmpty()){
                    validateInput(password,etPassword);
                }
            }
        });
        boolean isEmailSignIn = getIntent().getBooleanExtra("isEmailSignIn",false);
        if (isEmailSignIn){
            String email = getIntent().getStringExtra("email");
            String name = getIntent().getStringExtra("fname");

            etEmail.setText(email);
            etName.setText(name);
            etEmail.setEnabled(false);
            if (name!=null)
                etName.setEnabled(false);
        }
        //FirebaseApp.initializeApp(SignUp.this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();


                if (etName.getText().toString().trim().isEmpty()){
                    etName.setError(getString(R.string.cannot_be_empty));
                    return;
                }

                if (email.isEmpty()){
                    etEmail.setError(getString(R.string.cannot_be_empty));
                    return;
                }if (password.isEmpty()){
                    etPassword.setError(getString(R.string.cannot_be_empty));
                    return;
                }
                if (!email.contains("hotmail")|| !email.contains("google")||!email.contains("live")||!email.contains("outlook")){
                    etEmail.setError(getString(R.string.only_google_allowed));
                }

                ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
                progressDialog.setCancelable(false);
                progressDialog.setTitle(getString(R.string.please_wait));
                progressDialog.setMessage(getString(R.string.creating_account));
                progressDialog.show();


                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Account creation successful
                            // Handle success scenario (e.g., navigate to home screen)
                            FirebaseDatabase.getInstance().getReference().child("platform_prefs").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        PlatformPrefs prefs = snapshot.getValue(PlatformPrefs.class);
                                        String userId = mAuth.getUid();
                                        Log.e("userId ",userId);
                                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().getDatabase().getReference("users").child(userId);
                                        User user = new User();
                                        user.setEmail(email);
                                        user.setName(etName.getText().toString().trim());
                                        user.setRole("user");
                                        user.setPremiumUser(false);
                                        user.setFreeAdsAvailable(prefs.getFreeAdsCount());
                                        user.setFreeMessagesAvailable(prefs.getFreeMessagesCount());
                                        user.setMaximumOrdersAvailable(prefs.getMaximumOrdersAllowed());
                                        long currentTimeMillis = System.currentTimeMillis();

                                        // Create a Calendar object and set it to the current time
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(currentTimeMillis);
                                        // Add one month to the calendar
                                        calendar.add(Calendar.MONTH, 1);
                                        // Get the new time in milliseconds
                                        long newTimeMillis = calendar.getTimeInMillis();
                                        user.setBenefitsExpiry(newTimeMillis);
                                        databaseRef.setValue(user);
                                        Toast.makeText(getApplicationContext(), "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUp.this, Home.class));
                                        finish();
                                    }else{
                                        String userId = mAuth.getUid();
                                        Log.e("userId ",userId);
                                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().getDatabase().getReference("users").child(userId);
                                        User user = new User();
                                        user.setEmail(email);
                                        user.setName(etName.getText().toString().trim());
                                        user.setRole("user");
                                        user.setPremiumUser(false);
                                        databaseRef.setValue(user);
                                        Toast.makeText(getApplicationContext(), "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUp.this, Home.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            // Account creation failed
                            // Handle failure scenario (e.g., show error message)
                            Exception e = task.getException();
                            Toast.makeText(getApplicationContext(), "Account creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void validateInput(String input,TextInputEditText etPassword) {
        if (input.length()<10){
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setBackgroundResource(R.drawable.bg_report_btn);
            etPassword.setError(getString(R.string.password_must_be_10));
        }else{
            if (!containsNumber(input)){
                etPassword.setError(getString(R.string.password_1_number));
                btnCreateAccount.setEnabled(false);
                btnCreateAccount.setBackgroundResource(R.drawable.bg_report_btn);
            }else if (!containsCapitalLetter(input)){
                etPassword.setError(getString(R.string.password_1_character));
                btnCreateAccount.setEnabled(false);
                btnCreateAccount.setBackgroundResource(R.drawable.bg_report_btn);
            }else{
                etPassword.setError(null);
                btnCreateAccount.setEnabled(true);
                btnCreateAccount.setBackgroundResource(R.drawable.btn_sign_in_opts);
                //ToastUtils.showShort("Valid password");
            }
        }
    }

    private boolean containsCapitalLetter(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNumber(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

}