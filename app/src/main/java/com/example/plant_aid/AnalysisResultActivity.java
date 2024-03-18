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

/**
 * Activity for displaying the analysis results of a selected image within the application.
 * This activity extends {@link AppCompatActivity} and is responsible for presenting the user
 * with detailed results of an image analysis. The analysis result is retrieved from a file
 * specified by the file path passed through the intent that started this activity.
 *
 * <p>The activity also displays a small thumbnail of the analyzed image alongside the textual
 * analysis results, providing a visual reference for the user.</p>
 *
 */
public class AnalysisResultActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    /**
     * Called when the activity is starting. This method sets up the activity's layout,
     * initializes shared preferences, sets up bottom navigation, and retrieves and displays
     * the analysis result and image. It extracts the file path and image path from the intent
     * that started the activity, reads the analysis result from the file, and updates the UI
     * components to show the result and image.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). <b>Note:</b> Otherwise it is null.
     */
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

    /**
     * Reads the analysis result from a file specified by the file path. This method opens the file,
     * reads its contents, and returns the result as a string. It is used to retrieve the textual analysis
     * result that is displayed to the user.
     *
     * @param filePath The path of the file containing the analysis result.
     * @return A string representing the content of the analysis result file, or an empty string if an error occurs.
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

