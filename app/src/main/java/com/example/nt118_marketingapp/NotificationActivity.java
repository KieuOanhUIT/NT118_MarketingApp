package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nt118_marketingapp.model.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private void loadNotificationsFromFirebase() {
        String currentUserId = userId != null ? userId : (auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null);
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

                // Sắp xếp mới nhất lên đầu
                Collections.sort(notifications, (a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()));
                displayNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNotifications() {
        notificationList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (notifications.isEmpty()) {
            Toast.makeText(this, "Không có thông báo nào.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Notification n : notifications) {
            View itemView = inflater.inflate(R.layout.item_notification, notificationList, false);

            ImageView icon = itemView.findViewById(R.id.imgNotificationIcon);
            TextView title = itemView.findViewById(R.id.tvNotificationTitle);
            TextView message = itemView.findViewById(R.id.tvNotificationMessage);
            TextView time = itemView.findViewById(R.id.tvNotificationTime);

            icon.setImageResource(n.getType().equals("Approval") ? R.drawable.ic_approve : R.drawable.ic_task);
            title.setText(n.getType().equals("Approval") ? "Duyệt nội dung" : "Công việc");
            message.setText(n.getMessage());
            time.setText(n.getCreatedTime().substring(0, 16).replace("T", " "));

            if (n.isRead()) {
                title.setTypeface(null, android.graphics.Typeface.NORMAL);
                title.setTextColor(getColor(R.color.textPrimary));
                itemView.setAlpha(0.7f);
            } else {
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                title.setTextColor(getColor(R.color.colorPrimary));
            }

            itemView.setOnClickListener(v -> {
                if (!n.isRead()) {
                    notificationRef.child(n.getId()).child("IsRead").setValue(true);
                }
                Intent i = new Intent(this, NotificationDetailActivity.class);
                i.putExtra("notificationId", n.getId());
                i.putExtra("title", n.getType().equals("Approval") ? "Duyệt nội dung" : "Công việc");
                i.putExtra("message", n.getMessage());
                i.putExtra("time", n.getCreatedTime().substring(0, 16).replace("T", " "));
                startActivity(i);
            });

            notificationList.addView(itemView);
        }
    }

    // ================== PHÂN QUYỀN BOTTOM NAV ==================
    private void applyNavVisibility() {
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }
    }

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
        
        // Ẩn tab dành cho Admin nếu không phải Admin
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_contentmanagement) {
                Intent intent = new Intent(getApplicationContext(), ContentListActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_approve) {
                Intent intent = new Intent(getApplicationContext(), ReviewContentActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_usermanagement) {
                Intent intent = new Intent(getApplicationContext(), UsermanagerActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_notification) {
                return true;
            } else if (id == R.id.navigation_profile) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                attachUserData(intent);
                startActivity(intent);
            }
            return true;
        });
    }
}
