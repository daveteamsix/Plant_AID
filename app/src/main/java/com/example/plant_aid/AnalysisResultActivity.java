package com.example.plant_aid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class AnalysisResultActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);

        // Initialize shared Preferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        NavigationHelper.setupBottomNavigation(this, R.id.home_nav_bar);

        // Get analysis result and image path from intent extras
        String imagePath = getIntent().getStringExtra("imagePath");
        String analysisResultFilePath = getIntent().getStringExtra("analysisResult");

        // Display analysis result
        String analysisResult = readAnalysisResultFromFile(analysisResultFilePath);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(analysisResult);

        // Display small copy of the associated image
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);
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
/*public class AnalysisResultActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);

        // Initialize shared Preferences

        NavigationHelper.setupBottomNavigation(this, R.id.home_nav_bar);

        // Get analysis result and image path from intent extras
        String result = getIntent().getStringExtra("result");
        String imagePath = getIntent().getStringExtra("imagePath");

        // Display analysis result
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);

        // Display small copy of the associated image
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);
    }
}*/


/*
  @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);
        NavigationHelper.setupBottomNavigation(this, R.id.home_nav_bar);

        // Get image path from intent extras
        String imagePath = getIntent().getStringExtra("imagePath");

        // Display small copy of the associated image using ImageListAdapter
        ImageView imageView = findViewById(R.id.imageView);
        ImageListAdapter.loadImage(imageView, imagePath);

        // Get analysis result from intent extras
        String result = getIntent().getStringExtra("result");

        // Display analysis result
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
 */

