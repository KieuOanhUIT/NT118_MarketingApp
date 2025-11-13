package com.example.nt118_marketingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nt118_marketingapp.model.Content;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách content trong bottom sheet popup
 */
public class ContentCalendarPopupAdapter extends RecyclerView.Adapter<ContentCalendarPopupAdapter.ViewHolder> {

    private List<Content> contentList;
    private Context context;
    private OnContentClickListener listener;

    public interface OnContentClickListener {
        void onContentClick(Content content);
    }

    public ContentCalendarPopupAdapter(Context context, OnContentClickListener listener) {
        this.context = context;
        this.contentList = new ArrayList<>();
        this.listener = listener;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_content_calendar_popup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Content content = contentList.get(position);
        holder.bind(content);
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContentTitle;
        TextView tvContentStatus;
        TextView tvContentTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContentTitle = itemView.findViewById(R.id.tvContentTitle);
            tvContentStatus = itemView.findViewById(R.id.tvContentStatus);
            tvContentTime = itemView.findViewById(R.id.tvContentTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onContentClick(contentList.get(position));
                }
            });
        }

        void bind(Content content) {
            // Set title
            tvContentTitle.setText(content.getTitle() != null ? content.getTitle() : "Untitled");

            // Set status with color
            String status = content.getStatus() != null ? content.getStatus() : "To do";
            tvContentStatus.setText(status);
            tvContentStatus.setBackgroundResource(getStatusBackground(status));

            // Set time (extract time from createdTime)
            String time = extractTime(content.getCreatedTime());
            tvContentTime.setText(time);
        }

        /**
         * Extract time from "dd/MM/yyyy HH:mm" format
         */
        private String extractTime(String createdTime) {
            if (createdTime == null || createdTime.isEmpty()) {
                return "";
            }
            try {
                String[] parts = createdTime.split(" ");
                if (parts.length > 1) {
                    return parts[1]; // HH:mm
                }
            } catch (Exception e) {
                // Ignore
            }
            return "";
        }

        /**
         * Get background drawable for status
         */
        private int getStatusBackground(String status) {
            switch (status.toLowerCase()) {
                case "to do":
                    return R.drawable.bg_status_todo;
                case "in progress":
                    return R.drawable.bg_status_in_progress;
                case "done":
                    return R.drawable.bg_status_done;
                case "accepted":
                    return R.drawable.bg_status_accepted;
                case "reject":
                    return R.drawable.bg_status_reject;
                case "scheduled":
                    return R.drawable.bg_status_scheduled;
                case "published":
                    return R.drawable.bg_status_published;
                case "locked":
                    return R.drawable.bg_status_locked;
                default:
                    return R.drawable.bg_status_todo;
            }
        }
    }
}
