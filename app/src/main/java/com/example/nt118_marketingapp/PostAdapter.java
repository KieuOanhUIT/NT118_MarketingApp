package com.example.nt118_marketingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;


    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvAuthor.setText(post.getFullName());
        holder.tvDeadline.setText(post.getPublishedTime());
        holder.tvStatus.setText(post.getStatus());


        // 👉 Khi click vào item - chuyển sang trang xem content
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ContentManageActivity.class);
            intent.putExtra("CONTENT_ID", post.getContentId()); // Sử dụng CONTENT_ID để load từ Firebase
            intent.putExtra("EDIT_MODE", false); // Mặc định ở chế độ xem, không chỉnh sửa
            v.getContext().startActivity(intent);
        });



        GradientDrawable bgShape = (GradientDrawable) holder.tvStatus.getBackground();
        switch (post.getStatus()) {
            case "Đã duyệt":
                bgShape.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSecondary));
                holder.tvStatus.setTextColor(Color.WHITE);
                break;
            case "Từ chối":
                bgShape.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.textPrimary));
                holder.tvStatus.setTextColor(Color.WHITE);
                break;

            case "Chờ duyệt":
                bgShape.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
                holder.tvStatus.setTextColor(Color.WHITE);
                break;

            case "Được giao":
                bgShape.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background));
                holder.tvStatus.setTextColor(Color.BLACK);
                // set viền đen
                bgShape.setStroke(1, Color.BLACK);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvDeadline, tvStatus;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
