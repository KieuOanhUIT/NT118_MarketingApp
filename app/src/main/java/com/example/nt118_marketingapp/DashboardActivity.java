package com.example.nt118_marketingapp;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    RecyclerView recyclerAssigned, recyclerApproved, recyclerRejected, recyclerAproveAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerAssigned = findViewById(R.id.recyclerAssigned);
        recyclerApproved = findViewById(R.id.recyclerApproved);
        recyclerRejected = findViewById(R.id.recyclerRejected);
        recyclerAproveAdmin = findViewById(R.id.recyclerWaitAprove);


        setupRecycler(recyclerAssigned, getAssignedPosts());
        setupRecycler(recyclerApproved, getApprovedPosts());
        setupRecycler(recyclerRejected, getRejectedPosts());
        setupRecycler(recyclerAproveAdmin, getAproveAdminPosts());
    }

    private void setupRecycler(RecyclerView recyclerView, List<Post> posts) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new PostAdapter(posts));
    }

    private List<Post> getAssignedPosts() {
        List<Post> list = new ArrayList<>();
        list.add(new Post("Kế hoạch Marketing Mùa Hè 2024", "Nguyễn Văn A", "Hạn: 30/07", "Đã giao"));
        list.add(new Post("Phân tích Xu hướng thị trường", "Trịnh Thảo", "Hạn: 31/07", "Đã giao"));
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

