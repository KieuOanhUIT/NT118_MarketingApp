package com.example.nt118_marketingapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nt118_marketingapp.model.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationList;
    private List<Notification> notifications = new ArrayList<>();
    private DatabaseReference notificationRef;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;

    // User data
    private String userId, fullName, roleName, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        fullName = intent.getStringExtra("fullName");
        roleName = intent.getStringExtra("roleName");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        notificationList = findViewById(R.id.notificationList);
        auth = FirebaseAuth.getInstance();
        notificationRef = FirebaseDatabase.getInstance().getReference("Notification");

        setupBottomNavigation();
        loadNotificationsFromFirebase();
    }

    // ================== LOAD DATA ==================
    private void loadNotificationsFromFirebase() {
        String currentUserId = userId != null
                ? userId
                : (auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null);

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
                    if (n != null && currentUserId.equals(n.getUserId())) {
                        n.setId(child.getKey());
                        notifications.add(n);
                    }
                }

                // Mới nhất lên đầu
                Collections.sort(notifications,
                        (a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()));

                displayNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this,
                        "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== DISPLAY ==================
    private void displayNotifications() {
        notificationList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (notifications.isEmpty()) {
            Toast.makeText(this, "Không có thông báo nào.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Notification n : notifications) {
            View itemView = inflater.inflate(
                    R.layout.item_notification, notificationList, false);

            ImageView icon = itemView.findViewById(R.id.imgNotificationIcon);
            TextView title = itemView.findViewById(R.id.tvNotificationTitle);
            TextView message = itemView.findViewById(R.id.tvNotificationMessage);
            TextView time = itemView.findViewById(R.id.tvNotificationTime);

            // Bind icon + title theo TYPE
            bindNotificationUI(n, icon, title);

            message.setText(n.getMessage());
            time.setText(formatTime(n.getCreatedTime()));

            // Read / Unread
            if (n.isRead()) {
                title.setTypeface(null, Typeface.NORMAL);
                title.setTextColor(
                        ContextCompat.getColor(this, R.color.textPrimary));
                itemView.setAlpha(0.7f);
            } else {
                title.setTypeface(null, Typeface.BOLD);
                title.setTextColor(
                        ContextCompat.getColor(this, R.color.colorPrimary));
            }

            itemView.setOnClickListener(v -> {
                if (!n.isRead()) {
                    notificationRef.child(n.getId())
                            .child("IsRead").setValue(true);
                }

                Intent i = new Intent(this, NotificationDetailActivity.class);
                i.putExtra("notificationId", n.getId());
                startActivity(i);
            });

            notificationList.addView(itemView);
        }
    }

    // ================== ICON + TITLE ==================
    private void bindNotificationUI(Notification n,
                                    ImageView icon,
                                    TextView title) {

        icon.clearColorFilter();
        String type = n.getType();

        if (type == null) {
            icon.setImageResource(R.drawable.ic_notification);
            title.setText("Thông báo");
            return;
        }

        switch (type) {
            case "Approval":
                icon.setImageResource(R.drawable.ic_approve);
                title.setText("Nội dung đã được duyệt");
                break;

            case "Rejection":
                icon.setImageResource(R.drawable.ic_reject);
                title.setText("Nội dung bị từ chối");
                break;

            case "Deadline":
                icon.setImageResource(R.drawable.ic_deadline);
                title.setText("Deadline sắp đến");
                break;

            case "Task":
                icon.setImageResource(R.drawable.ic_task);
                icon.setColorFilter(
                        ContextCompat.getColor(this, R.color.colorSecondary));
                title.setText("Công việc");
                break;

            default:
                icon.setImageResource(R.drawable.ic_notification);
                title.setText("Thông báo");
                break;
        }
    }

    // ================== TIME FORMAT ==================
    private String formatTime(String time) {
        if (time == null) return "-";
        try {
            return time.substring(0, 16).replace("T", " ");
        } catch (Exception e) {
            return time;
        }
    }

    // ================== BOTTOM NAV ==================
    private void attachUserData(Intent intent) {
        intent.putExtra("userId", userId);
        intent.putExtra("fullName", fullName);
        intent.putExtra("roleName", roleName);
        intent.putExtra("phone", phone);
        intent.putExtra("email", email);
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_notification);

        // Phân quyền
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu()
                    .findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu()
                    .findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                Intent i = new Intent(this, DashboardActivity.class);
                attachUserData(i);
                startActivity(i);
            } else if (id == R.id.navigation_contentmanagement) {
                Intent i = new Intent(this, ContentListActivity.class);
                attachUserData(i);
                startActivity(i);
            } else if (id == R.id.navigation_approve) {
                Intent i = new Intent(this, ReviewContentActivity.class);
                attachUserData(i);
                startActivity(i);
            } else if (id == R.id.navigation_usermanagement) {
                Intent i = new Intent(this, UsermanagerActivity.class);
                attachUserData(i);
                startActivity(i);
            } else if (id == R.id.navigation_profile) {
                Intent i = new Intent(this, Profile.class);
                attachUserData(i);
                startActivity(i);
            }
            return true;
        });
    }
}
