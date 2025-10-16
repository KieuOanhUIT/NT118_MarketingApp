package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

// Model thông báo — để gọn trong file cũng được
class Notification {
    private final String title;
    private final String message;
    private final String time;
    private final int iconResId;
    private boolean isRead;

    public Notification(String title, String message, String time, int iconResId, boolean isRead) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.iconResId = iconResId;
        this.isRead = isRead;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getIconResId() { return iconResId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationList;
    private List<Notification> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationList = findViewById(R.id.notificationList);

        generateSampleNotifications();
        displayNotifications();
    }

    // ✅ Dữ liệu mẫu
    private void generateSampleNotifications() {
        notifications = new ArrayList<>();

        notifications.add(new Notification(
                "Deadline sắp đến!",
                "Bài đăng Facebook cần lên lịch lúc 14:00 hôm nay.",
                "10:30 - 16/10/2025",
                R.drawable.ic_deadline,
                false
        ));

        notifications.add(new Notification(
                "Bị từ chối",
                "Nội dung bài viết bị reject bởi quản lý. Vui lòng chỉnh sửa lại.",
                "09:00 - 16/10/2025",
                R.drawable.ic_reject,
                true
        ));

        notifications.add(new Notification(
                "Đến giờ đăng bài",
                "Bài Instagram sắp đến giờ đăng.",
                "13:50 - 16/10/2025",
                R.drawable.ic_post,
                false
        ));
    }

    // Hiển thị danh sách thông báo
    private void displayNotifications() {
        LayoutInflater inflater = LayoutInflater.from(this);
        notificationList.removeAllViews();

        for (Notification n : notifications) {
            View itemView = inflater.inflate(R.layout.item_notification, notificationList, false);

            ImageView icon = itemView.findViewById(R.id.imgNotificationIcon);
            TextView title = itemView.findViewById(R.id.tvNotificationTitle);
            TextView message = itemView.findViewById(R.id.tvNotificationMessage);
            TextView time = itemView.findViewById(R.id.tvNotificationTime);

            icon.setImageResource(n.getIconResId());
            title.setText(n.getTitle());
            message.setText(n.getMessage());
            time.setText(n.getTime());

            // Hiệu ứng đã đọc / chưa đọc
            if (n.isRead()) {
                title.setTypeface(null, android.graphics.Typeface.NORMAL);
                title.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
                itemView.setAlpha(0.7f);
            } else {
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                title.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                itemView.setAlpha(1f);
            }

            // Sự kiện click mở chi tiết
            itemView.setOnClickListener(v -> {
                n.setRead(true); // đánh dấu đã đọc

                Intent intent = new Intent(NotificationActivity.this, NotificationDetailActivity.class);
                intent.putExtra("title", n.getTitle());
                intent.putExtra("message", n.getMessage());
                intent.putExtra("time", n.getTime());
                intent.putExtra("icon", n.getIconResId());
                startActivity(intent);

                displayNotifications(); // refresh lại danh sách
            });

            notificationList.addView(itemView);
        }
    }
}
