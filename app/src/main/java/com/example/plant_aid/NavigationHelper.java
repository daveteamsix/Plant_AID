package com.example.plant_aid;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// NavigationHelper.java
public class NavigationHelper {

    private static int lastSelectedItemId = -1;

    public static void setupBottomNavigation(AppCompatActivity currentActivity, int selectedItemId) {
        BottomNavigationView bottomNavigationView = currentActivity.findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> handleNavigation(item, currentActivity));
    }

    private static boolean handleNavigation(MenuItem item, AppCompatActivity currentActivity) {
        int itemId = item.getItemId();

        // Check if the selected item is already the current item
        if (itemId == lastSelectedItemId) {
            return true;  // Do nothing, as the user is already on the selected item
        }

        if (itemId == R.id.home_nav_bar) {
            startNewActivity(currentActivity, MainActivity.class);
        } else if (itemId == R.id.camera_nav_bar) {
            startNewActivity(currentActivity, CameraActivity.class);
        } else if (itemId == R.id.my_garden_nav_bar) {
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
