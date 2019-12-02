package com.zjh.classifywithtflite.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zjh.classifywithtflite.R;

import java.util.List;

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.ViewHolder> {
    private List<Label> labelList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView labelImage;
        TextView labelName;

        public ViewHolder(View view) {
            super(view);
            labelImage = view.findViewById(R.id.labelImage);
            labelName = view.findViewById(R.id.labelName);
        }
    }

    public LabelAdapter(List<Label> labelList){
        this.labelList = labelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.label_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Label label = labelList.get(position);
        holder.labelImage.setImageResource(label.getId());
        holder.labelName.setText(label.getName());
    }

    @Override
    public int getItemCount() {
        return labelList.size();
    }
}