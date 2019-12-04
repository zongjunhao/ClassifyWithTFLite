package com.zjh.classifywithtflite.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.activities.ImageManageActivity;
import com.zjh.classifywithtflite.constant.Constant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<Image> imageList;
    private ImageManageActivity activity;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    static class ViewHolder extends RecyclerView.ViewHolder {
        View imageView;
        ImageView imageImage;
        TextView imageName;
        TextView imageTime;
        Button deleteImage;

        ViewHolder(View view) {
            super(view);
            imageView = view;
            imageImage = view.findViewById(R.id.itemImage);
            imageName = view.findViewById(R.id.itemName);
            imageTime = view.findViewById(R.id.itemTime);
            deleteImage = view.findViewById(R.id.deleteItem);
        }
    }

    public ImageAdapter(List<Image> imageList, ImageManageActivity activity) {
        this.imageList = imageList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.imageView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Image image = imageList.get(position);
            Toast.makeText(v.getContext(), image.getId() + " " + image.getName(), Toast.LENGTH_SHORT).show();
        });
        holder.deleteImage.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Image image = imageList.get(position);
            Toast.makeText(v.getContext(), "delete " + image.getId(), Toast.LENGTH_SHORT).show();
            activity.deleteImage(image.getId());
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Image image = imageList.get(position);
        Glide.with(activity)
                .load(Constant.REQUEST_URL + image.getPath())
                .placeholder(R.drawable.loading)
                .into(holder.imageImage);
        holder.imageName.setText(image.getName());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String time = sdf.format(image.getUpdatetime());
        holder.imageTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
