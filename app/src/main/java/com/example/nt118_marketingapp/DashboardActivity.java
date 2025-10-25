package com.example.nt118_marketingapp;


import android.content.Intent;
import android.os.Bundle;
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
    // khai báo imageview imgReport
    ImageView imgReport;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        imgReport = findViewById(R.id.imgReport);
        recyclerAssigned = findViewById(R.id.recyclerAssigned);
        recyclerApproved = findViewById(R.id.recyclerApproved);
        recyclerRejected = findViewById(R.id.recyclerRejected);
        recyclerAproveAdmin = findViewById(R.id.recyclerWaitAprove);


        setupRecycler(recyclerAssigned, getAssignedPosts());
        setupRecycler(recyclerApproved, getApprovedPosts());
        setupRecycler(recyclerRejected, getRejectedPosts());
        setupRecycler(recyclerAproveAdmin, getAproveAdminPosts());

        imgReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });

        imgReport.setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DashboardActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_contentmanagement) {
                startActivity(new Intent(getApplicationContext(), ContentListActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_approve) {
                startActivity(new Intent(getApplicationContext(), ReviewContentActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_usermanagement) {
                startActivity(new Intent(getApplicationContext(), UsermanagerActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_notification) {
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_profile) {
                // Không cần start lại Activity hiện tại
                return true;
            }

            return false;
        });

        //click vào bất kì item nào của các recycler view -> chuyền sang file EditContentActivity.java

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

