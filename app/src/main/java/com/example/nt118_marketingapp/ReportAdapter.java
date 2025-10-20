package com.example.nt118_marketingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    List<ReportItem> list;
    public ReportAdapter(List<ReportItem> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportItem item = list.get(position);
        holder.tvChannel.setText(item.getChannel());
        holder.tvMonth.setText(item.getMonth());
        holder.tvPosts.setText(String.valueOf(item.getPosts()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChannel, tvMonth, tvPosts;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChannel = itemView.findViewById(R.id.tvChannel);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvPosts = itemView.findViewById(R.id.tvPosts);
        }
    }
}
