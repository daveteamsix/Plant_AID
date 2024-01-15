package com.example.plant_aid;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private static final String TAG = "CameraActivity";

    public TextureView textureView;
    private ExecutorService cameraExecutor;

    private ImageCapture imageCapture;

    // boolean flag to check if ImageCapture has been initialized
    private boolean isImageCaptureInitialized = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.d("CameraActivity", "onCreate called");

        Log.d("CameraActivity", "Before initializing textureView");
        textureView = findViewById(R.id.textureView);
        Log.d("CameraActivity", "After initializing textureView");


        // Initialize the executor for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Set up the capture button and its click listener
        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> captureImage());

        // Check camera permissions and start the camera if granted, otherwise request permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            Log.d("CameraActivity", "Requesting permissions");
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            Log.d("CameraActivity", "Requested permissions");
        }
    }



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

                // Process and send the image
                processAndSendImage(savedImageFile.getAbsolutePath());
                // For simplicity, let's just display a toast message
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Image captured successfully and sent to backend!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Image capture failed, handle the error
                exception.printStackTrace();

                // For simplicity, let's just display a toast message
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Image capture failed!", Toast.LENGTH_SHORT).show());
            }

        });
    }

    private void processAndSendImage(String imagePath) {
        // Step 1: Preprocess the image
        File processedImageFile = preprocessImage(imagePath);

        // Step 2: Send the processed image to the backend server
        sendImageToServer(processedImageFile);
    }

    private void sendImageToServer(File imageFile) {
        // Create a request body with the image file
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        // Create the request with our backend URL
        Request request = new Request.Builder()
                .url("localhost:3000/describeImage")
                .post(requestBody)
                .build();

        // Execute the request
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                showSnackbar("Failed to send image for analysis.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        // Extract the analysis result from the response JSON
                        String result = Objects.requireNonNull(response.body()).string();
                        Log.d(TAG, "Analysis Result: " + result);

                        // Display the result and image in a new activity or dialog
                        displayAnalysisResult(result, imageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showSnackbar("Error parsing analysis result.");
                    }
                } else {
                    showSnackbar("Error: " + response.code());
                }
            }
        });
    }
    public void displayAnalysisResult(String result, File imageFile) {
        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("imagePath", imageFile.getAbsolutePath());
        startActivity(intent);
    }

    private void showSnackbar(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private String generateFileName() {
        // Generate a timestamped file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "IMG_" + sdf.format(new Date());
    }


    private File preprocessImage(String imagePath) {
        // Load the captured image
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        // Check the original size of the image
        long originalSize = calculateFileSize(imagePath);

        // Define the maximum size (15 MB in bytes)
        long maxSizeBytes = 15 * 1024 * 1024;

        if (originalSize > maxSizeBytes) {
            // Calculate the new dimensions while maintaining the aspect ratio
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float aspectRatio = (float) width / height;

            // Calculate the new width and height to achieve the desired file size
            long newSize = (long) Math.sqrt(maxSizeBytes * aspectRatio);
            width = (int) (newSize * aspectRatio);
            height = (int) newSize;

            // Resize the bitmap
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        // Create a new file to save the processed image
        String processedImagePath = getOutputDirectory() + File.separator + "processed_" + generateFileName() + ".jpg";
        File processedImageFile = new File(processedImagePath);

        try {
            // Save the processed image to the new file
            FileOutputStream fos = new FileOutputStream(processedImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the processed image file
        return processedImageFile;
    }

    private long calculateFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }

    // Method to get the output directory for saving captured images
    private File getOutputDirectory() {
        File mediaDir = new File(getExternalMediaDirs()[0], "PlantAid");
        mediaDir.mkdirs();
        return mediaDir;
    }
    // Method to start the camera and set up the preview
    private void startCamera() {
        // Use a ListenableFuture to handle the asynchronous camera provider initialization
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Add a listener to the cameraProviderFuture to handle the completion of the camera provider initialization
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the initialized camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Bind the camera preview to the TextureView and setup ImageCapture
                bindPreviewAndImageCapture(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    // Method to bind the camera preview to the TextureView
    private void bindPreviewAndImageCapture(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

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
                    // Handle the result if needed
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


    // Override the onRequestPermissionsResult method to handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                // Display a toast message if some permissions are not granted
            }
        }
    }


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

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView != null && textureView.isAttachedToWindow()) {
            // Initialize the ImageCapture use case
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(textureView.getDisplay().getRotation())
                    .build();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }



    // Method to check if all required permissions are granted
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //  method to shut down the camera executor when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
