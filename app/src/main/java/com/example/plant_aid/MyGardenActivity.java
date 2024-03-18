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


/**
 * Represents the "My Garden" activity within the application, showcasing a list of images representing
 * the user's garden. This activity extends {@link AppCompatActivity} and utilizes a {@link RecyclerView}
 * to display the images in a list format.
 *
 * <p>Within this activity, users can view their garden's images that have been saved previously. Each image
 * in the list can be clicked to view an analysis result associated with that image.</p>
 *
 * <p>The activity also handles retrieving image paths from shared preferences, initializing the RecyclerView
 * and its adapter, and setting up a click listener for items within the RecyclerView.</p>
 */
public class MyGardenActivity extends AppCompatActivity {

    private RecyclerView gardenRecyclerView;
    private GardenRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;

    /**
     * Called when the activity is starting. This is where initialization of the activity's UI and
     * components takes place. It sets the content view, initializes the RecyclerView and its adapter,
     * and sets up the bottom navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). <b>Note:</b> Otherwise it is null.
     */
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

    /**
     * Retrieves the list of image paths from shared preferences.
     *
     * @return An ArrayList containing the image paths stored in shared preferences.
     */
    private ArrayList<String> getImagePathsFromPrefs() {
        Set<String> imagePathSet = sharedPreferences.getStringSet("imagePaths", new HashSet<>());
        return new ArrayList<>(imagePathSet);
    }

    /**
     * Opens the AnalysisResultActivity for the selected image path. This method prepares and starts the
     * AnalysisResultActivity with the selected image path and its analysis result.
     *
     * @param selectedImagePath The path of the image selected by the user.
     */
    private void openAnalysisResult(String selectedImagePath) {
        String analysisResultFilePath = sharedPreferences.getString(selectedImagePath, "");
        String analysisResult = readAnalysisResultFromFile(analysisResultFilePath);

        // Start AnalysisResultActivity with the selected image path and analysis result
        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra("imagePath", selectedImagePath);
        intent.putExtra("analysisResult", analysisResultFilePath);
        startActivity(intent);
    }


    /**
     * Reads the analysis result from a file specified by the file path.
     *
     * @param filePath The path of the file containing the analysis result.
     * @return A string representing the content of the analysis result file.
     */
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

