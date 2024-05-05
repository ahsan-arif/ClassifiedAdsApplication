package com.android.classifiedapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.classifiedapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        etPassword = findViewById(R.id.et_password);
        boolean isEmailSignIn = getIntent().getBooleanExtra("isEmailSignIn",false);
        if (isEmailSignIn){
            String email = getIntent().getStringExtra("email");
            String name = getIntent().getStringExtra("fname");

            etEmail.setText(email);
            etName.setText(name);
            etEmail.setEnabled(false);
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

                if (email.isEmpty()){
                    etEmail.setError(getString(R.string.cannot_be_empty));
                    return;
                }if (password.isEmpty()){
                    etPassword.setError(getString(R.string.cannot_be_empty));
                    return;
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
                            String userId = mAuth.getUid();
                            Log.e("userId ",userId);
                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().getDatabase().getReference("users").child(userId);
                            User user = new User();
                            user.setEmail(email);
                            user.setName(etName.getText().toString().trim());
                            databaseRef.setValue(user);
                            Toast.makeText(getApplicationContext(), "Account created!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUp.this, Home.class));
                            finish();
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

}