package com.example.plant_aid;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plant_aid.myGardenHelper.GardenRecyclerAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
public class MyGardenActivity extends AppCompatActivity {

    private RecyclerView gardenRecyclerView;
    private GardenRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);

        NavigationHelper.setupBottomNavigation(this, R.id.my_garden_nav_bar);

        gardenRecyclerView = findViewById(R.id.gardenRecyclerView);
        gardenRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        // Initialize the custom adapter
        ArrayList<String> imagePaths = getImagePathsFromPrefs();
        recyclerAdapter = new GardenRecyclerAdapter(this, imagePaths, sharedPreferences);

        // Set a click listener for the images in the list
        recyclerAdapter.setOnItemClickListener(position -> {
            String selectedImagePath = imagePaths.get(position);
            openAnalysisResult(selectedImagePath);
        });

        gardenRecyclerView.setAdapter(recyclerAdapter);
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
