package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    RecyclerView recyclerAssigned, recyclerApproved, recyclerRejected, recyclerAproveAdmin;
    ImageView imgReport;
    private BottomNavigationView bottomNavigationView;

    // Thông tin người dùng hiện tại (nhận từ SignInActivity)
    private String userId, fullName, roleName, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Nhận dữ liệu người dùng từ Intent (SignInActivity gửi sang)
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        fullName = intent.getStringExtra("fullName");
        roleName = intent.getStringExtra("roleName");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        imgReport = findViewById(R.id.imgReport);
        recyclerAssigned = findViewById(R.id.recyclerAssigned);
        recyclerApproved = findViewById(R.id.recyclerApproved);
        recyclerRejected = findViewById(R.id.recyclerRejected);
        recyclerAproveAdmin = findViewById(R.id.recyclerWaitAprove);

        setupRecycler(recyclerAssigned, getAssignedPosts());
        setupRecycler(recyclerApproved, getApprovedPosts());
        setupRecycler(recyclerRejected, getRejectedPosts());
        setupRecycler(recyclerAproveAdmin, getAproveAdminPosts());

        imgReport.setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(DashboardActivity.this, ReportActivity.class);
            attachUserData(intent1);
            startActivity(intent1);
        });

        // Cấu hình bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Ẩn tab nếu không phải admin
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent nextIntent = null;

            if (itemId == R.id.navigation_home) {
                nextIntent = new Intent(getApplicationContext(), DashboardActivity.class);

            } else if (itemId == R.id.navigation_contentmanagement) {
                nextIntent = new Intent(getApplicationContext(), ContentListActivity.class);

            } else if (itemId == R.id.navigation_approve) {
                nextIntent = new Intent(getApplicationContext(), ReviewContentActivity.class);

            } else if (itemId == R.id.navigation_usermanagement) {
                nextIntent = new Intent(getApplicationContext(), UsermanagerActivity.class);

            } else if (itemId == R.id.navigation_notification) {
                nextIntent = new Intent(getApplicationContext(), NotificationActivity.class);

            } else if (itemId == R.id.navigation_profile) {
                nextIntent = new Intent(getApplicationContext(), Profile.class);
            }

            if (nextIntent != null) {
                attachUserData(nextIntent); // thêm userId và info vào tất cả Intent
                startActivity(nextIntent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    // Hàm tiện ích: gắn dữ liệu người dùng vào Intent
    private void attachUserData(Intent intent) {
        intent.putExtra("userId", userId);
        intent.putExtra("fullName", fullName);
        intent.putExtra("roleName", roleName);
        intent.putExtra("phone", phone);
        intent.putExtra("email", email);
    }

    private void setupRecycler(RecyclerView recyclerView, List<Post> posts) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new PostAdapter(posts));
    }

    private List<Post> getAssignedPosts() {
        List<Post> list = new ArrayList<>();
        list.add(new Post("Kế hoạch Marketing Mùa Hè 2024", "Nguyễn Văn A", "Hạn: 30/07", "Được giao"));
        list.add(new Post("Phân tích Xu hướng thị trường", "Trịnh Thảo", "Hạn: 31/07", "Được giao"));
        return list;
    }

    private List<Post> getApprovedPosts() {
        List<Post> list = new ArrayList<>();
        list.add(new Post("Case Study: Tăng trưởng 20%", "Nguyễn Văn A", "Hạn: 29/07", "Đã duyệt"));
        list.add(new Post("Báo cáo thị trường Q2/2024", "Trần Duy", "Hạn: 29/07", "Đã duyệt"));
        return list;
    }

    private List<Post> getRejectedPosts() {
        List<Post> list = new ArrayList<>();
        list.add(new Post("Review sản phẩm X", "Phạm Thị D", "Hạn: 29/07", "Từ chối"));
        return list;
    }
    private List<Post> getAproveAdminPosts() {
        List<Post> list = new ArrayList<>();
        list.add(new Post("Review sản phẩm X", "Phạm Thị D", "Hạn: 29/07", "Chờ duyệt"));
        return list;
    }
}