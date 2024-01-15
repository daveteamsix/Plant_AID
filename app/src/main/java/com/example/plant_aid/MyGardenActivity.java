package com.example.plant_aid;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MyGardenActivity extends AppCompatActivity {

    private ListView gardenListView;
    private ArrayAdapter<String> gardenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);

        NavigationHelper.setupBottomNavigation(this, R.id.my_garden_nav_bar);

        gardenListView = findViewById(R.id.gardenListView);

        gardenAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        gardenListView.setAdapter(gardenAdapter);


        ArrayList<String> imagePaths = getIntent().getStringArrayListExtra("imagePaths");
        if (imagePaths != null) {
            gardenAdapter.addAll(imagePaths);
            gardenAdapter.notifyDataSetChanged();
        }

    }


}