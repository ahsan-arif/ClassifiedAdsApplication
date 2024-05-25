package com.android.classifiedapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.models.User;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityLogin extends AppCompatActivity {
TextInputEditText etEmail,etPassword;
TextView tvHaveAccount;
TextView btnLogin,tvForgot;
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
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tvForgot = findViewById(R.id.tv_forgot);
        btnLogin = findViewById(R.id.btn_login);
        tvHaveAccount = findViewById(R.id.tv_have_account);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e("btn pressed");
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (email.trim().isEmpty()){
                    etEmail.setError("Cannot be empty");
                    return;
                }
                if (password.trim().isEmpty()){
                    etPassword.setError("Cannot be empty");
                    return;
                }
                ProgressDialog progressDialog = new ProgressDialog(ActivityLogin.this);
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
                                    // Login successful, navigate to main activity
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    getUserDetails(user.getUid());
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
            }
        });
        tvHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this,MainActivity.class));
                finish();
            }
        });

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this,ActivityForgotPassword.class));
            }
        });


    }
    void getUserDetails(String uid){
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user =snapshot.getValue(User.class);
                    if (user.getRole().equals("user")){
                        startActivity(new Intent(ActivityLogin.this,Home.class));
                    }else{
                        ToastUtils.showShort(getString(R.string.admin_cannot));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}