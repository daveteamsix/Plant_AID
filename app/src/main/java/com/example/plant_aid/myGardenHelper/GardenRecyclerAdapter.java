package com.example.plant_aid.myGardenHelper;

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

import java.util.ArrayList;


public class GardenRecyclerAdapter extends RecyclerView.Adapter<GardenRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> imagePaths;
    private SharedPreferences sharedPreferences;

    private OnItemClickListener onitemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onitemClickListener = listener;
    }

    public GardenRecyclerAdapter(Context context, ArrayList<String> imagePaths, SharedPreferences sharedPreferences) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        Glide.with(context)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.imageView);

        // Set any additional information about the item
        holder.textView.setText("Description /TODO");

        holder.itemView.setOnClickListener(view -> {
            if (onitemClickListener != null) {
                onitemClickListener.onItemClick(position);
            }
        });
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
