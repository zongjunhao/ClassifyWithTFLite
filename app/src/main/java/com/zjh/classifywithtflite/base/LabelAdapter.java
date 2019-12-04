package com.zjh.classifywithtflite.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.activities.LabelManageActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.ViewHolder> {
    private List<Label> labelList;
    private LabelManageActivity activity;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    static class ViewHolder extends RecyclerView.ViewHolder {
        View labelView;
        ImageView labelImage;
        TextView labelName;
        TextView labelTime;
        Button deleteLabel;

        ViewHolder(View view) {
            super(view);
            labelView = view;
            labelImage = view.findViewById(R.id.itemImage);
            labelName = view.findViewById(R.id.itemName);
            labelTime = view.findViewById(R.id.itemTime);
            deleteLabel = view.findViewById(R.id.deleteItem);
        }
    }

    public LabelAdapter(List<Label> labelList, LabelManageActivity activity) {
        this.labelList = labelList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.labelView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Label label = labelList.get(position);
            Toast.makeText(v.getContext(), label.getId() + " " + label.getName(), Toast.LENGTH_SHORT).show();
            activity.viewImageByLabel(label.getId());
        });
        holder.deleteLabel.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Label label = labelList.get(position);
            Toast.makeText(v.getContext(), "delete " + label.getId(), Toast.LENGTH_SHORT).show();
            activity.deleteLabel(label.getId());
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Label label = labelList.get(position);
        holder.labelImage.setImageResource(R.drawable.file_icon);
        holder.labelName.setText(label.getName());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String time = sdf.format(label.getUpdatetime());
        holder.labelTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return labelList.size();
    }
}