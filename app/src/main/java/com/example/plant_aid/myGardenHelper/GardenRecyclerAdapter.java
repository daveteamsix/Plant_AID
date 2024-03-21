package com.example.plant_aid.myGardenHelper;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plant_aid.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Adapter class for the RecyclerView that displays garden images and their analysis results.
 * This adapter manages the data model containing the image paths and interacts with the RecyclerView
 * to display each image and its corresponding analysis result.
 *
 * <p>The adapter also supports click events on items through an OnItemClickListener, allowing the application
 * to respond when a user clicks on an item in the list.</p>
 */
public class GardenRecyclerAdapter extends RecyclerView.Adapter<GardenRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> imagePaths;
    private SharedPreferences sharedPreferences;

    private OnItemClickListener onitemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onitemClickListener = listener;
    }

    /**
     * Constructs a new GardenRecyclerAdapter.
     *
     * @param context The context, typically the activity, where the RecyclerView is being displayed.
     * @param imagePaths A list of image paths for the garden images to be displayed.
     * @param sharedPreferences The SharedPreferences instance for accessing stored analysis results.
     */
    public GardenRecyclerAdapter(Context context, ArrayList<String> imagePaths, SharedPreferences sharedPreferences) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_layout, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the contents
     * of the {@link ViewHolder#imageView} to reflect the image at the given position and sets the analysis
     * result text in {@link ViewHolder#textView}.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given
     *               position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        Glide.with(context)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.imageView);


        String analysisResultFilePath = sharedPreferences.getString(imagePath, "");
        String analysisResult = readAnalysisResultFromFile(analysisResultFilePath);
        holder.textView.setText(analysisResult);


        holder.itemView.setOnClickListener(view -> {
            if (onitemClickListener != null) {
                onitemClickListener.onItemClick(position);
            }
        });
    }
    /**
     * Reads the analysis result from a file specified by the filePath.
     *
     * @param filePath The path of the file containing the analysis result.
     * @return A string representing the content of the file.
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

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImageView);
            textView = itemView.findViewById(R.id.itemTextView);
        }
    }
}
