package com.example.plant_aid;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationHelper {

    private static int lastSelectedItemId = -1;

    public static void setupBottomNavigation(AppCompatActivity currentActivity, int selectedItemId) {
        BottomNavigationView bottomNavigationView = currentActivity.findViewById(R.id.bottom_nav_bar);
        FloatingActionButton fab = currentActivity.findViewById(R.id.camera_nav_bar);
        fab.setImageTintList(null);

        fab.setOnClickListener(view -> handleFabClick(currentActivity, bottomNavigationView));

        bottomNavigationView.setOnItemSelectedListener(item -> handleNavigation(item, currentActivity));

        // Initialize lastSelectedItemId only if it's the first time
        if (lastSelectedItemId == -1) {
            lastSelectedItemId = selectedItemId;
        }

        // Set the selected item after setting up the listener
        if (currentActivity.getClass() != AnalysisResultActivity.class) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }
    }

    private static void handleFabClick(AppCompatActivity currentActivity, BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setSelectedItemId(R.id.camera_nav_bar_item);
        if (currentActivity.getClass() != CameraActivity.class) {
            startNewActivity(currentActivity, CameraActivity.class);
        }
    }

    private static boolean handleNavigation(MenuItem item, AppCompatActivity currentActivity) {
        int itemId = item.getItemId();

        // Check if the selected item is already the current item
        if (itemId == lastSelectedItemId) {
            return true;  // Do nothing, as the user is already on the selected item
        }

        if (itemId == R.id.home_nav_bar) {
            startNewActivity(currentActivity, MainActivity.class);
        }
        else if (itemId == R.id.my_garden_nav_bar) {
            startNewActivity(currentActivity, MyGardenActivity.class);
        }

        lastSelectedItemId = itemId;  // Update the last selected item ID
        return true;
    }

    private static void startNewActivity(AppCompatActivity currentActivity, Class<?> destinationActivity) {
        Intent intent = new Intent(currentActivity, destinationActivity);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        currentActivity.finish();
    }
}
