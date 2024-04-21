package com.android.classifiedapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.classifiedapp.models.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        //insertData("Cars","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Fcar.svg?alt=media&token=a49f5eda-92bf-41a9-a6a6-64b75c7616ca");
        //insertData("Motorbikes","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Fmotorcycle.svg?alt=media&token=5f7a58d1-30bf-4cac-882a-b4a56d492eb5");
        //insertData("Real Estate","https://firebasestorage.googleapis.com/v0/b/ecommerceapp-65596.appspot.com/o/icons%2Freal%20estate.svg?alt=media&token=487a5548-dee3-4cec-a8db-dbaa25311995");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    void insertData(String name,String url){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("categories").push();
        String id= databaseReference.getKey();
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setImageUrl(url);
        databaseReference.setValue(category);
    }
}