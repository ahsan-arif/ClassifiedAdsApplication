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

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityVerifyLogin extends AppCompatActivity {
    TextInputEditText etPassword;
    TextView tvHaveAccount,tvProvidePassword;
    TextView btnLogin,tvForgot;
    String email;
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
                LogUtils.e("btn pressed");
                if( email.isEmpty()){
                    ToastUtils.showShort(getString(R.string.invalid_email));
                }
                String password = etPassword.getText().toString();

                if (password.trim().isEmpty()){
                    etPassword.setError("Cannot be empty");
                    return;
                }
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
                                    // Login successful, navigate to main activity
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    startActivity(new Intent(ActivityVerifyLogin.this,Home.class));
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
}