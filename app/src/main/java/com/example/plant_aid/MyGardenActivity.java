package com.example.plant_aid;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class MyGardenActivity extends AppCompatActivity {

    private ListView gardenListView;
    private ImageListAdapter imageListAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);

        NavigationHelper.setupBottomNavigation(this, R.id.my_garden_nav_bar);

        gardenListView = findViewById(R.id.gardenListView);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        // Initialize the custom adapter
        ArrayList<String> imagePaths = getImagePathsFromPrefs();
        imageListAdapter = new ImageListAdapter(this, imagePaths, sharedPreferences);
        gardenListView.setAdapter(imageListAdapter);

        // Set a click listener for the images in the list
        gardenListView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedImagePath = (String) imageListAdapter.getItem(position);
            openAnalysisResult(selectedImagePath);
        });
    }

    private ArrayList<String> getImagePathsFromPrefs() {
        Set<String> imagePathSet = sharedPreferences.getStringSet("imagePaths", new HashSet<>());
        return new ArrayList<>(imagePathSet);
    }

    private void openAnalysisResult(String selectedImagePath) {
        String analysisResultFilePath = sharedPreferences.getString(selectedImagePath, "");
        String analysisResult = readAnalysisResultFromFile(analysisResultFilePath);

        // Start AnalysisResultActivity with the selected image path and analysis result
        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra("imagePath", selectedImagePath);
        intent.putExtra("analysisResult", analysisResultFilePath);
        startActivity(intent);
    }

    private String readAnalysisResultFromFile(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
/*public class MyGardenActivity extends AppCompatActivity {

    private ListView gardenListView;
    private ImageListAdapter imageListAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);

        NavigationHelper.setupBottomNavigation(this, R.id.my_garden_nav_bar);

        gardenListView = findViewById(R.id.gardenListView);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        // Initialize the custom adapter
        ArrayList<String> imagePaths = getImagePathsFromPrefs();
        imageListAdapter = new ImageListAdapter(this, imagePaths, sharedPreferences);
        gardenListView.setAdapter(imageListAdapter);

        // Set a click listener for the images in the list
        gardenListView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedImagePath = (String) imageListAdapter.getItem(position);
            openAnalysisResult(selectedImagePath);
        });
    }


    private ArrayList<String> getImagePathsFromPrefs() {
        Set<String> imagePathSet = sharedPreferences.getStringSet("imagePaths", new HashSet<>());
        return new ArrayList<>(imagePathSet);
    }

    private void openAnalysisResult(String selectedImagePath) {
        String analysisResult = getAnalysisResultFromPrefs(selectedImagePath);

        // Start AnalysisResultActivity with the selected image path and analysis result
        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra("imagePath", selectedImagePath);
        intent.putExtra("analysisResult", analysisResult);
        startActivity(intent);
    }

    private String getAnalysisResultFromPrefs(String selectedImagePath) {
        // Retrieve analysis result based on the image path
        return sharedPreferences.getString(selectedImagePath, "");
    }
}*/
