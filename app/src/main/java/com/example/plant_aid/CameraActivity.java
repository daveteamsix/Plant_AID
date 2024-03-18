package com.example.plant_aid;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity which contains the Camera functionality, as well as the methods required for communicating with the backend server.
 *
 *  This class extends {@link AppCompatActivity} and manages the camera functionalities, including initializing the camera,
 *  capturing images, processing captured images, and handling permissions required for camera use. It leverages the CameraX
 *  library for camera operations, providing a more straightforward integration with camera hardware and reducing boilerplate code
 *  associated with camera APIs.
 *  Features and Responsibilities:
 *   - Request and check camera permissions.
 *   - Initialize and bind camera use cases such as preview and image capture.
 *   - Capture images with the camera and handle image saving.
 *   - Rotate and preprocess images before further processing or uploading.
 *   - Upload Images to the Backend and receive responses
 *   - Call and open the AnalysisResult screen
 *
 *   The activity relies on a {@link TextureView} for displaying the camera preview. It utilizes an {@link ExecutorService}
 *   for running camera operations on a background thread to ensure smooth UI operation. SharedPreferences are used for persistent
 *   storage of image paths and related data to facilitate easy access across the app sessions.
 *
 */
public class CameraActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private static final String TAG = "CameraActivity";

    public TextureView textureView;
    private ExecutorService cameraExecutor;

    private ImageCapture imageCapture;

    private ArrayList<String> imagePaths;

    private SharedPreferences sharedPreferences;

    // boolean flag to check if ImageCapture has been initialized (for testing)
    private boolean isImageCaptureInitialized = false;


    /**
     * Initializes the CameraActivity with necessary UI and functionalities.
     *
     * This method is called when the activity is starting. It sets the content view to the activity's layout,
     * initializes the {@link SharedPreferences} for storing application preferences, and sets up the UI components,
     * including the texture view for camera preview and the capture button with its click listener. It also initializes
     * the executor service for handling camera operations in the background.
     *
     * Additionally, this method checks if all necessary camera permissions have been granted. If so, it starts the camera;
     * otherwise, it requests the required permissions. The setup includes adjusting the texture view's height based on the
     * screen's width to maintain an aspect ratio of 4:3, which is commonly used for photos.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyGardenPrefs", Context.MODE_PRIVATE);

        NavigationHelper.setupBottomNavigation(this, R.id.camera_nav_bar);
        Log.d("CameraActivity", "onCreate called");

        Log.d("CameraActivity", "Before initializing textureView");
        textureView = findViewById(R.id.textureView);
        int screanWidth = getResources().getDisplayMetrics().widthPixels;
        int textureViewHeight = (int) (screanWidth * 4/3);
        ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
        layoutParams.height = textureViewHeight;
        textureView.setLayoutParams(layoutParams);



        // Initialize the executor for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Set up the capture button and its click listener
        ImageButton captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> {
            captureButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_small_big));
            findViewById(R.id.whiteOval).startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_white_oval));
            captureImage();});

        // Check camera permissions and start the camera if granted, otherwise request permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            Log.d("CameraActivity", "Requesting permissions");
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            Log.d("CameraActivity", "Requested permissions");
        }
    }



    /**
     * Captures an image using the current camera settings and saves it to the application's private storage.
     *
     * This method initiates an asynchronous image capture task. Upon capturing, the image is saved to a timestamp-named file
     * in the application's output directory. It handles both successful captures and errors.
     * For a successful capture, it performs additional image processing, such as rotation, and sends the image to a backend server or another processing method.
     * In case of an error during capture, the method logs the exception and notifies the user via a toast message.
     *
     * The successful capture path includes rotating the image by 90 degrees and then processing the image
     * through the {@code processAndSendImage} method, which must be implemented to handle the image further. Finally,
     * a toast message confirms the successful capture and processing of the image.
     *
     * Note: This method assumes {@link ImageCapture} {@code imageCapture} and an executor service {@code cameraExecutor}
     * have been initialized and are ready for use. It also uses {@code runOnUiThread} to display toast messages, ensuring
     * UI operations are performed on the main thread.
     *
     */
    private void captureImage() {
        // Create a timestamped file to save the captured image
        File photoFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis()) + ".jpg");

        // Configure the output options for ImageCapture
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();


        // Capture the image and handle success/failure
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Get the saved image file
                File savedImageFile = new File(Objects.requireNonNull(Objects.requireNonNull(outputFileResults.getSavedUri()).getPath()));

                // rotate the image
                rotateImage(savedImageFile.getAbsolutePath(), 90);

                // Process and send the image
                processAndSendImage(savedImageFile.getAbsolutePath());

                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Image captured successfully and sent to backend!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Image capture failed, handle the error
                exception.printStackTrace();


                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Image capture failed!", Toast.LENGTH_SHORT).show());
            }

        });
    }

    /**
     * Processes the specified image and sends it to a backend server for further analysis.
     *
     * This method comprises two primary steps: preprocessing the given image file located at {@code imagePath}
     * and then sending the preprocessed image to a designated backend server. The preprocessing includes
     * operations such as resizing, cropping, or format conversion to prepare the image for analysis.
     * After preprocessing, the processed image is sent to the backend server using the {@code sendImageToServer} method.
     *
     * @param imagePath The absolute path to the image file that needs to be processed and sent.
     *
     * @see #preprocessImage(String)
     * @see #sendImageToServer(File)
     */
    private void processAndSendImage(String imagePath) {
        File processedImageFile = preprocessImage(imagePath);
        sendImageToServer(processedImageFile);
    }

    /**
     * Sends the given image file to a backend server for analysis.
     *
     * This method constructs a multipart HTTP request to upload the image file to a predefined server URL.
     * Upon successful transmission, the server's response is expected to contain the analysis result,
     * which is then processed accordingly. Error handling is incorporated to manage failures in sending the image
     * or issues with receiving the analysis result from the server.
     *
     * @param imageFile The image file to be sent to the backend server for analysis.
     *
     * @see okhttp3.RequestBody
     * @see okhttp3.Request
     * @see okhttp3.OkHttpClient
     */
    private void sendImageToServer(File imageFile) {
        // Create a request body with the image file
        String filename = imageFile.getName();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", filename, RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        // Create the request with our backend URL
        Request request = new Request.Builder()
                .url("http://192.168.0.197:3000/analysePlantImage") // IP address of the backend server
                .post(requestBody)
                .build();

        // Execute the request
         OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
                .readTimeout(30, TimeUnit.SECONDS) // Set read timeout
                .build();

        Log.d(TAG, "Sending image to server: " + request.url());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                showSnackbar("Failed to send image for analysis.");

                // For testing
                // getting date
                Date date = new Date();
                date.getTime();
                String result = "This is a test result";


                //update MyGarden Activity with the new image
                String imagePath = imageFile.getAbsolutePath();
                updateMyGarden(imagePath, date.toString() + " " + result);

                openAnalysisResult(imagePath);

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        // Extract the analysis result from the response JSON
                        String result = Objects.requireNonNull(response.body()).string();

                        Log.d(TAG, "Analysis Result: " + result);

                        //update MyGarden Activity with the new image
                        String imagePath = imageFile.getAbsolutePath();
                        updateMyGarden(imagePath, result);
                        // Display the result and image in a new activity or dialog
                        openAnalysisResult(imagePath);

                    } catch (IOException e) {
                        e.printStackTrace();
                        showSnackbar("Error parsing analysis result.");

                    }
                } else {
                    showSnackbar("Error: " + response.code());

                    // For testing
                    String result = "Response was not successful.";
                    displayAnalysisResult(result, imageFile);
                }
            }
        });

    }

    /**
     * Updates the "MyGardenActivity" by saving the analysis result of an image and its path.
     *
     * This method performs two main tasks: first, it creates and writes the analysis result to a text file named
     * with a prefix 'analysis_' followed by the original image's filename. This file is saved in the app's private
     * file directory. Second, it saves both the path of the analyzed image and the path of the newly created analysis
     * text file to SharedPreferences for later retrieval.
     *
     * The method handles any IOException that occurs during the file writing process and logs it. The saving of paths
     * to SharedPreferences is facilitated by calling `saveImagePathToPrefs` and `saveAnalysisResultToPrefs` methods,
     * which must be implemented elsewhere in the class or application to handle the specifics of SharedPreferences storage.
     *
     * @param imagePath The absolute path of the image that was analyzed. This path is used to derive the filename for
     *                  the analysis result file and to store in SharedPreferences for future reference.
     * @param analysisResult The string containing the result of the image analysis. This result is written to a text
     *                       file and the file's path is saved along with the image path in SharedPreferences.
     *
     * @see #saveImagePathToPrefs(String)
     * @see #saveAnalysisResultToPrefs(String, String)
     */
 private void updateMyGarden(String imagePath, String analysisResult){
        String filename = "analysis_" + getFileNameFromPath(imagePath) + ".txt";
        File analysisFile = new File(getFilesDir(), filename);
        try{
            FileWriter writer = new FileWriter(analysisFile);
            writer.write(analysisResult);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveImagePathToPrefs(imagePath);
        saveAnalysisResultToPrefs(imagePath, analysisFile.getAbsolutePath());
 }



    /**
     * Extracts and returns the filename from the provided file path.
     *
     * This method constructs a {@link File} object using the given image path and then returns the name
     * of the file. It is used to isolate the file name from a full path.
     *
     * @param imagePath The full path to the file.
     * @return The name of the file extracted from the given path.
     */
    private String getFileNameFromPath(String imagePath) {
        File file = new File(imagePath);
        return file.getName();
    }

    /**
     * Saves the image path to SharedPreferences.
     *
     * This method retrieves a set of image paths from SharedPreferences, adds the new image path to this set,
     * and then saves the updated set back to SharedPreferences. This is used to keep track of all images
     * processed by the app.
     *
     * @param imagePath The absolute path of the image to be saved.
     */
    //update MyGarden Activity with the new image
    private void saveImagePathToPrefs(String imagePath) {
        Set<String> imagePathSet = sharedPreferences.getStringSet("imagePaths", new HashSet<>());
        imagePathSet.add(imagePath);
        sharedPreferences.edit().putStringSet("imagePaths", imagePathSet).apply();
    }

    /**
     * Saves the file path of an analysis result associated with an image to SharedPreferences.
     *
     * This method maps the image path to its corresponding analysis result file path in SharedPreferences.
     * It allows for easy retrieval of the result file path when needed, based on the image path.
     *
     * @param imagePath The absolute path of the image that was analyzed.
     * @param analysisResultFilePath The file path where the analysis result is stored.
     */
    private void saveAnalysisResultToPrefs(String imagePath, String analysisResultFilePath) {
        sharedPreferences.edit().putString(imagePath, analysisResultFilePath).apply();
    }

    /**
     * Displays the analysis result of an image in {@link AnalysisResultActivity}.
     *
     * This method creates an intent to start {@code AnalysisResultActivity}, passing it the analysis
     * result string and the image's file path. This allows the activity to display the image along with
     * its analysis result.
     *
     * @param result The analysis result to be displayed.
     * @param imageFile The file of the image that was analyzed.
     */
    public void displayAnalysisResult(String result, File imageFile) {
        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("imagePath", imageFile.getAbsolutePath());
        startActivity(intent);
    }

    /**
     * Opens {@link AnalysisResultActivity} to display the selected image and its analysis result.
     *
     * This method retrieves the analysis result file path for the given image path from SharedPreferences,
     * reads the analysis result from the file, and starts {@code AnalysisResultActivity} with the image
     * path and the read analysis result.
     *
     * @param selectedImagePath The path of the image whose analysis result is to be displayed.
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
     * Reads and returns the content of an analysis result file as a string.
     *
     * This method opens and reads the file at the given file path, appending each line of the file to
     * a {@link StringBuilder} to construct the complete analysis result string.
     *
     * @param filePath The path to the file containing the analysis result.
     * @return The complete analysis result as a string.
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

    /**
     * Displays a brief message to the user in the form of a Toast.
     *
     * This method ensures that the Toast message is displayed on the UI thread, making it safe to call
     * from background threads.
     *
     * @param message The text message to be shown in the Toast.
     */
    private void showSnackbar(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    /**
     * Generates a unique filename based on the current timestamp.
     *
     * This method creates a filename intended for saving new images. The filename is prefixed with "IMG_"
     * followed by a timestamp, ensuring that each filename is unique. This is particularly useful for differentiating
     * between images captured or processed in sequence.
     *
     * @return A string representing the generated filename.
     */
    private String generateFileName() {
        // Generate a timestamped file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "IMG_" + sdf.format(new Date());
    }


    /**
     * Processes the image located at the given path to fit within specified maximum dimensions.
     *
     * This method loads the image from the specified path, resizes it to maintain the aspect ratio while ensuring
     * the width and height do not exceed maximum set values, and saves the resized image to a new file. The new
     * dimensions are chosen to reduce the file size and dimension for uploading, while
     * maintaining the visual quality of the image. The processed image is saved with a "processed_" prefix in its
     * filename to differentiate it from the original.
     *
     * @param imagePath The path of the image file to be processed.
     * @return A File object pointing to the newly created file with the processed image.
     */
    private File preprocessImage(String imagePath) {
        // Load the captured image
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        // Define the maximum width and height
        int maxWidth = 800;
        int maxHeight = 600;

        // Get the original dimensions of the image
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate the aspect ratio
        float aspectRatio = (float) width / height;

        // Calculate new width and height to fit within the maximum dimensions while maintaining aspect ratio
        int newWidth = width;
        int newHeight = height;
        if (width > maxWidth || height > maxHeight) {
            if (aspectRatio > 1) {
                newWidth = maxWidth;
                newHeight = (int) (maxWidth / aspectRatio);
            } else {
                newHeight = maxHeight;
                newWidth = (int) (maxHeight * aspectRatio);
            }
        }

        // Resize the bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        // Create a new file to save the processed image
        String processedImagePath = getOutputDirectory() + File.separator + "processed_" + generateFileName() + ".jpg";
        File processedImageFile = new File(processedImagePath);

        try {
            // Save the processed image to the new file
            FileOutputStream fos = new FileOutputStream(processedImageFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Recycle the original and resized bitmaps to release memory
            bitmap.recycle();
            resizedBitmap.recycle();
        }

        return processedImageFile;
    }


    /**
     *  Method to get the output directory for saving captured images
     *
     * @return
     */
    private File getOutputDirectory() {
        File mediaDir = new File(getExternalMediaDirs()[0], "PlantAid");
        mediaDir.mkdirs();
        return mediaDir;
    }


    /**
     * Initializes the camera and sets up the preview and image capture functionalities.
     *
     * This method asynchronously retrieves an instance of {@link ProcessCameraProvider} and, upon successful
     * initialization, proceeds to bind the camera preview and image capture functionalities to the application's User Interface.
     * It utilizes a {@link ListenableFuture} to handle the asynchronous operation and sets up a listener to execute
     * the binding once the camera provider is available. The main executor is used to ensure the callback executes
     * on the main thread.
     */
    private void startCamera() {
        // Use a ListenableFuture to handle the asynchronous camera provider initialization
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Add a listener to the cameraProviderFuture to handle the completion of the camera provider initialization
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreviewAndImageCapture(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    /**
     * Binds the camera preview and image capture use cases to the camera provider.
     *
     * This method sets up the preview and image capture functionalities using the provided {@link ProcessCameraProvider}.
     * It defines the preview size based on the dimensions of a {@link TextureView} used for displaying the camera feed,
     * and configures the {@link ImageCapture} use case with necessary options. The camera is then bound to these use cases
     * with a specified {@link CameraSelector}, ensuring the selected camera (e.g., back-facing camera) is used for the preview
     * and capture.
     *
     * @param cameraProvider The camera provider to which the use cases will be bound. This provider is responsible for
     *                       managing the lifecycle of the camera use cases in accordance with the activity lifecycle.
     */
    private void bindPreviewAndImageCapture(ProcessCameraProvider cameraProvider) {

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        Rational aspectRatio = new Rational(viewWidth, viewHeight);

        Size targetResolution = new Size(textureView.getWidth(), textureView.getHeight());
        

        Preview preview = new Preview.Builder().setTargetResolution(targetResolution).build();


        // Setup ImageCapture use case
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                // You can add more configurations as needed
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(new Preview.SurfaceProvider() {
            @Override
            public void onSurfaceRequested(@NonNull SurfaceRequest request) {
                // Use the SurfaceTexture from the TextureView to create a Surface
                SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                Surface surface = new Surface(surfaceTexture);

                // Provide the Surface to the camera
                request.provideSurface(surface, Executors.newSingleThreadExecutor(), (result) -> {
                    //
                });
            }
        });

        try {
            // Unbind any previous use cases before rebinding
            cameraProvider.unbindAll();

            // Bind the camera to the lifecycle of the activity with the specified preview and camera selector
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the result of the permission request.
     *
     * This method is called after the user responds to the permission request dialog. If all required permissions
     * have been granted, the camera is started by calling {@code startCamera()}. If not, a Snackbar message is shown
     * to indicate that permissions were not granted, which is essential for the app's  functionality.
     *
     * @param requestCode  The request code passed in {@code requestPermissions(android.app.Activity, String[], int)}
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     {@code PackageManager.PERMISSION_GRANTED} or {@code PackageManager.PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                showSnackbar("Error, permission were not granted.");
            }
        }
    }


    /**
     * Called when the activity is becoming visible to the user.
     *
     * This method ensures that the {@link ImageCapture} use case is initialized if the {@link TextureView} is
     * already attached to the window, indicating that the UI is ready for the camera preview to be displayed.
     * It logs an error if the {@code textureView} is not ready, which is ensuring that
     * the app's UI is correctly set up before attempting to use camera functionalities.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (textureView != null && textureView.isAttachedToWindow()) {
            // Initialize the ImageCapture use case
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(textureView.getDisplay().getRotation())
                    .build();
        } else {
            Log.e("CameraActivity", "textureView is not ready in onStart");
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * At this point, your activity is at the top of the activity stack, with user input going to it.
     * Similar to {@code onStart()}, this method checks if the {@link TextureView} is ready and re-initializes
     * the {@link ImageCapture} use case accordingly. If the {@code textureView} is not ready, it attempts to
     * request the necessary camera permissions again. This ensures that the app correctly handles camera permissions
     * and initialization following the Android activity lifecycle, especially when returning from a paused state.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (textureView != null && textureView.isAttachedToWindow()) {
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(textureView.getDisplay().getRotation())
                    .build();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }



    /**
     * Checks if all required permissions have been granted for the app.
     *
     * @return {@code true} if all required permissions are granted, {@code false} otherwise.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rotates the image at the given file path by the specified degrees and saves the rotated image back to the file.
     *
     * This method loads the bitmap from the specified image path, applies a rotation transformation, and then saves
     * the rotated bitmap to the same file path. It is useful for correcting the orientation of an image based on sensor
     * data or user input.
     *
     * @param imagePath The file path of the image to rotate.
     * @param degrees   The number of degrees to rotate the image. Positive values rotate clockwise, while negative values rotate counterclockwise.
     */
    private void rotateImage(String imagePath, float degrees) {
        // Load the captured image
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        // Create a matrix for the rotation transformation
        Matrix matrix = new Matrix();

        // Set the rotation angle
        matrix.postRotate(degrees);

        // Apply the rotation to the bitmap
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);

        // Save the rotated bitmap back to the image file
        saveBitmapToFile(rotatedBitmap, imagePath);
    }

    /**
     * Saves a {@link Bitmap} object to a file at the specified path.
     *
     * This method attempts to open or create the file at the given path and compresses the bitmap into this file
     * using the JPEG format with a quality setting of 90. It is used to persist bitmap images to storage.
     *
     * @param bitmap   The bitmap to save to a file.
     * @param filePath The path where the bitmap should be saved.
     */
    private void saveBitmapToFile(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Called by the system to clean up resources before the activity is destroyed.
     *
     * This method shuts down the {@link ExecutorService} used for camera operations, ensuring that any background
     * tasks are properly terminated and resources are freed up. It is an important part of managing the app's
     * resources and preventing memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
