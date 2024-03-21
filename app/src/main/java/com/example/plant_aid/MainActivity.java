package com.example.plant_aid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity class for the application, serving as the entry point to the user interface.
 * This class extends {@link AppCompatActivity} to provide a modern, compatible action bar.
 * It sets up the UI layout for the main screen and initializes necessary components such as
 * bottom navigation and shared preferences.
 *
 * <p>Within this activity, users can navigate through different sections of the app, like opening
 * the camera or viewing their garden, by interacting with the bottom navigation bar.</p>
 *
 * <p>Shared preferences are used to store and retrieve user settings or other relevant data across
 * app sessions, ensuring a personalized and consistent user experience.</p>
 */
public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling {@code setContentView(int)} to inflate the activity's UI, using {@code findViewById(int)}
     * to programmatically interact with widgets in the UI, setting up any static data to be displayed, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). <b>Note:</b> Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.home_nav_bar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);
    }

}