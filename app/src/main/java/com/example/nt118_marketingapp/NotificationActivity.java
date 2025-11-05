package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.nt118_marketingapp.model.Notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationList;
    private List<Notification> notifications;
    private DatabaseReference notificationRef;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationList = findViewById(R.id.notificationList);
        notifications = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        notificationRef = FirebaseDatabase.getInstance().getReference("Notification");

        loadNotificationsFromFirebase();
        setupBottomNavigation();
    }

    /** ✅ Đọc dữ liệu Notification từ Firebase */
    private void loadNotificationsFromFirebase() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(this, "Không xác định được người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notification n = child.getValue(Notification.class);
                    if (n == null) continue;

                    // Lọc đúng userId
                    if (currentUserId.equals(n.getUserId())) {
                        n.setId(child.getKey()); // gắn ID của thông báo
                        notifications.add(n);
                    }
                }

                // Sắp xếp theo thời gian giảm dần
                Collections.sort(notifications, (n1, n2) -> n2.getCreatedTime().compareTo(n1.getCreatedTime()));

                if (notifications.isEmpty()) {
                    Toast.makeText(NotificationActivity.this, "Không có thông báo nào.", Toast.LENGTH_SHORT).show();
                }

                displayNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ✅ Hiển thị danh sách thông báo */
    private void displayNotifications() {
        LayoutInflater inflater = LayoutInflater.from(this);
        notificationList.removeAllViews();

        for (Notification n : notifications) {
            View itemView = inflater.inflate(R.layout.item_notification, notificationList, false);

            ImageView icon = itemView.findViewById(R.id.imgNotificationIcon);
            TextView title = itemView.findViewById(R.id.tvNotificationTitle);
            TextView message = itemView.findViewById(R.id.tvNotificationMessage);
            TextView time = itemView.findViewById(R.id.tvNotificationTime);

            // Chọn icon theo Type
            int iconRes;
            switch (n.getType()) {
                case "Approval":
                    iconRes = R.drawable.ic_approve;
                    break;
                case "Task":
                    iconRes = R.drawable.ic_task;
                    break;
                default:
                    iconRes = R.drawable.ic_notification;
                    break;
            }

            icon.setImageResource(iconRes);
            title.setText(n.getType().equals("Approval") ? "Duyệt nội dung" : "Công việc");
            message.setText(n.getMessage());

            // Chuyển format thời gian đẹp
            String formattedTime = n.getCreatedTime().replace("T", " ");
            time.setText(formattedTime);

            // Giao diện đã đọc / chưa đọc
            if (n.isRead()) {
                title.setTypeface(null, android.graphics.Typeface.NORMAL);
                title.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
                itemView.setAlpha(0.7f);
            } else {
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                title.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                itemView.setAlpha(1f);
            }

            // Sự kiện click
            itemView.setOnClickListener(v -> {
                // Đánh dấu đã đọc trong Firebase
                if (!n.isRead()) {
                    notificationRef.child(n.getId()).child("IsRead").setValue(true);
                    n.setRead(true);
                }

                // Mở chi tiết
                Intent intent = new Intent(NotificationActivity.this, NotificationDetailActivity.class);
                intent.putExtra("notificationId", n.getId());
                intent.putExtra("title", n.getType().equals("Approval") ? "Duyệt nội dung" : "Công việc");
                intent.putExtra("message", n.getMessage());
                intent.putExtra("time", formattedTime);
                intent.putExtra("icon", iconRes);
                startActivity(intent);

                displayNotifications();
            });

            notificationList.addView(itemView);
        }
    }

    /** ✅ Điều hướng Bottom Navigation */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_notification);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            } else if (itemId == R.id.navigation_contentmanagement) {
                startActivity(new Intent(getApplicationContext(), ContentListActivity.class));
            } else if (itemId == R.id.navigation_approve) {
                startActivity(new Intent(getApplicationContext(), ReviewContentActivity.class));
            } else if (itemId == R.id.navigation_usermanagement) {
                startActivity(new Intent(getApplicationContext(), UsermanagerActivity.class));
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(getApplicationContext(), Profile.class));
            } else {
                return false;
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}
