package com.example.plant_aid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.home_nav_bar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        // Set click listeners for the icons
        ImageView cameraIcon = findViewById(R.id.cameraIcon);
        ImageView myGardenIcon = findViewById(R.id.myGardenIcon);


        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraActivity();
            }
        });

        myGardenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyGardenActivity();
            }
        });

    }

    private void openCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


    private void openMyGardenActivity() {
        Intent intent = new Intent(this, MyGardenActivity.class);
        startActivity(intent);
    }
}