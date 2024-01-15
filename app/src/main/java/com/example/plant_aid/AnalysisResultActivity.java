package com.example.plant_aid;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;

public class AnalysisResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);

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
}

