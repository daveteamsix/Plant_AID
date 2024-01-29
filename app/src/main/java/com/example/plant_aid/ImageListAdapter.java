package com.example.plant_aid;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;


public class ImageListAdapter extends BaseAdapter {

    private Context context;
    private List<String> imagePaths;
    private SharedPreferences sharedPreferences;
    private LayoutInflater inflater;

    public ImageListAdapter(Context context, List<String> imagePaths, SharedPreferences sharedPreferences) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.sharedPreferences = sharedPreferences;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_image, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.textViewAnalysisResult = convertView.findViewById(R.id.textViewAnalysisResult);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Load the image from the path
        String imagePath = imagePaths.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        holder.imageView.setImageBitmap(bitmap);

        // Load the analysis result from the file
        String analysisResultFilePath = sharedPreferences.getString(imagePath, "");
        String analysisResult = readAnalysisResultFromFile(analysisResultFilePath);
        holder.textViewAnalysisResult.setText(analysisResult);

        return convertView;
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

    static class ViewHolder {
        ImageView imageView;
        TextView textViewAnalysisResult;
    }
}


/*public class ImageListAdapter extends BaseAdapter {

    private Context context;
    private List<String> imagePaths;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;

    public ImageListAdapter(Context context, List<String> imagePaths, SharedPreferences sharedPreferences) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.sharedPreferences = sharedPreferences;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_image, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textViewAnalysisResult = view.findViewById(R.id.textViewAnalysisResult);

        // Load the image from the path
        String imagePath = imagePaths.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);

        // Load the analysis result from SharedPreferences
        String analysisResult = sharedPreferences.getString(imagePath, "No analysis result found");
        textViewAnalysisResult.setText(analysisResult);

        return view;
    }

    // Clear the adapter's data
    public void clear() {
        imagePaths.clear();
        notifyDataSetChanged();
    }

    // Add all image paths to the adapter's data
    public void addAll(List<String> paths) {
        imagePaths.addAll(paths);
        notifyDataSetChanged();
    }
}*/

