package com.example.plant_aid;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Utility class for setting up and handling navigation events from the bottom navigation bar
 * and the floating action button (FAB) within the app. This class provides static methods
 * to initialize and manage navigation actions, ensuring a consistent and efficient navigation
 * experience across different activities.
 *
 * <p>The class facilitates navigation between the main sections of the app, like the home screen,
 * the garden overview, and the camera function, by responding to user interactions with the
 * bottom navigation items and the camera FAB.</p>
 */
public class NavigationHelper {

    private static int lastSelectedItemId = -1;


    /**
     * Sets up the bottom navigation and floating action button (FAB) for the camera within the current activity.
     * It initializes event listeners for both navigation elements and manages the navigation flow based on user
     * interactions. This method also ensures the correct navigation item is highlighted as selected.
     *
     * @param currentActivity The currently active {@link AppCompatActivity} where the bottom navigation and FAB are set up.
     * @param selectedItemId The ID of the navigation item that should be marked as selected.
     */
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

    /**
     * Handles the click event on the floating action button (FAB) for the camera. This method navigates
     * to the {@link CameraActivity} unless the current activity is already the CameraActivity.
     *
     * @param currentActivity The currently active {@link AppCompatActivity}.
     * @param bottomNavigationView The {@link BottomNavigationView} instance of the current activity.
     */
    private static void handleFabClick(AppCompatActivity currentActivity, BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setSelectedItemId(R.id.camera_nav_bar_item);
        if (currentActivity.getClass() != CameraActivity.class) {
            startNewActivity(currentActivity, CameraActivity.class);
        }
    }


    /**
     * Handles navigation events from the bottom navigation menu. It checks if the selected item is different from
     * the last selected one and initiates navigation to the corresponding activity if necessary.
     *
     * @param item The selected {@link MenuItem} from the bottom navigation.
     * @param currentActivity The currently active {@link AppCompatActivity}.
     * @return true to display the item as the selected item.
     */
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

    /**
     * Initiates a transition to a new activity based on the specified destination activity class.
     * This method starts the new activity with a custom animation and finishes the current activity
     * to prevent back stack accumulation.
     *
     * @param currentActivity The currently active {@link AppCompatActivity} that will be finished.
     * @param destinationActivity The {@link Class} of the activity to start.
     */
    private static void startNewActivity(AppCompatActivity currentActivity, Class<?> destinationActivity) {
        Intent intent = new Intent(currentActivity, destinationActivity);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        currentActivity.finish();
    }
}
