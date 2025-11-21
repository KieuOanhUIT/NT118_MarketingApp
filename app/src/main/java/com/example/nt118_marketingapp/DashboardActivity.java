package com.example.nt118_marketingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DashboardActivity extends AppCompatActivity {
    // khai báo các list
    List<Post> listAssigned, listApproved, listRejected, listAproveAdmin;

    // khai báo các adapter
    PostAdapter adapterAssigned, adapterApproved, adapterRejected, adapterAproveAdmin;


    // khai báo các recycler View
    RecyclerView recyclerAssigned, recyclerApproved, recyclerRejected, recyclerAproveAdmin;

    // khai báo các biến aprove, deadline, reject
    TextView tvDeadline, tvApproved, tvRejected,tvapproveAdmin;
    TextView tvFullName;

    ImageView imgReport;

    // khai báo Firebase real time trong java
    DatabaseReference database;

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
        tvDeadline = findViewById(R.id.tvDeadline);
        tvApproved = findViewById(R.id.tvApproved);
        tvRejected = findViewById(R.id.tvRejected);
        tvapproveAdmin = findViewById(R.id.aproveAdmin);



        // set layout cho recycle view dạng ngang (horizontal)
        recyclerAssigned.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerApproved.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerRejected.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerAproveAdmin.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        // khởi tạo các list
        listAssigned = new ArrayList<>();
        listApproved = new ArrayList<>();
        listRejected = new ArrayList<>();
        listAproveAdmin = new ArrayList<>();

        // khởi tạo adapter
        adapterAssigned = new PostAdapter(listAssigned);
        adapterApproved = new PostAdapter(listApproved);
        adapterRejected = new PostAdapter(listRejected);
        adapterAproveAdmin = new PostAdapter(listAproveAdmin);



        // set adapter cho recycler view
        recyclerAssigned.setAdapter(adapterAssigned);
        recyclerApproved.setAdapter(adapterApproved);
        recyclerRejected.setAdapter(adapterRejected);
        recyclerAproveAdmin.setAdapter(adapterAproveAdmin);


        // Khởi tạo database Fire base
        database = FirebaseDatabase.getInstance().getReference();

        // Lấy dữ liệu cho các Recycle view
        getAssignedPost(recyclerAssigned);
        getApprovedPosts(recyclerApproved);
        getRejectedPosts(recyclerRejected);
        getAproveAdminPosts(recyclerAproveAdmin);


        imgReport.setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(DashboardActivity.this, ReportActivity.class);
            attachUserData(intent1);
            startActivity(intent1);
        });

        // ẩn bài chờ duyệt nếu ko phải admin
        if (!"Admin".equalsIgnoreCase(roleName)) {
            recyclerAproveAdmin.setVisibility(View.GONE);
            tvapproveAdmin.setVisibility(View.GONE);

        }


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


    private void getAssignedPost(RecyclerView recyclerView) {
        database.child("Content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // clear list
                    listAssigned.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String Status = dataSnapshot.child("Status").getValue(String.class);
                        String userID = dataSnapshot.child("UserId").getValue(String.class);
                        if ("To do".equals(Status) && userID != null && userID.equals(userId)) {
                            String Title = dataSnapshot.child("Title").getValue(String.class);
                            String PublishedTime = dataSnapshot.child("CreatedTime").getValue(String.class);
                            String ContendId = dataSnapshot.getKey();


                            // Lấy FullName từ collection "User"
                            database.child("User").child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    String fullName = "";
                                    if (userSnapshot.exists()) {
                                        fullName = userSnapshot.child("FullName").getValue(String.class);
                                        Log.d("FirebaseDebug", "👤 Lấy được FullName: " + fullName);
                                        Log.d("FirebaseDebug", "👤 Lấy được Contentid: " + ContendId);
                                        listAssigned.add(new Post(ContendId, Title, fullName, PublishedTime, "Được giao"));
                                        adapterAssigned.notifyDataSetChanged();
                                        tvDeadline.setText(String.valueOf(listAssigned.size()));
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getApprovedPosts(RecyclerView recyclerView) {
        database.child("Content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // clear list
                    listApproved.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String Status = dataSnapshot.child("Status").getValue(String.class);
                        String userID = dataSnapshot.child("UserId").getValue(String.class);
                        if ("Approved".equals(Status) && userID != null && userID.equals(userId)) {
                            String Title = dataSnapshot.child("Title").getValue(String.class);
                            String PublishedTime = dataSnapshot.child("CreatedTime").getValue(String.class);
                            String ContendId = dataSnapshot.getKey();

                            // Lấy FullName từ collection "User"
                            database.child("User").child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    String fullName = "";
                                    if (userSnapshot.exists()) {
                                        fullName = userSnapshot.child("FullName").getValue(String.class);
                                        Log.d("FirebaseDebug", "👤 Lấy được FullName: " + fullName);
                                        Log.d("FirebaseDebug", "👤 Lấy được Contentid: " + ContendId);

                                        listApproved.add(new Post(ContendId, Title, fullName, PublishedTime, "Đã duyệt"));
                                        adapterApproved.notifyDataSetChanged();
                                        tvApproved.setText(String.valueOf(listApproved.size()));
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getRejectedPosts(RecyclerView recyclerView) {
        database.child("Content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // clear list
                    listRejected.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String Status = dataSnapshot.child("Status").getValue(String.class);
                        String userID = dataSnapshot.child("UserId").getValue(String.class);
                        if ("Rejected".equals(Status) && userID != null && userID.equals(userId)) {
                            String Title = dataSnapshot.child("Title").getValue(String.class);
                            String PublishedTime = dataSnapshot.child("CreatedTime").getValue(String.class);
                            String ContendId =dataSnapshot.getKey();

                            // Lấy FullName từ collection "User"
                            database.child("User").child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    String fullName = "";
                                    if (userSnapshot.exists()) {
                                        fullName = userSnapshot.child("FullName").getValue(String.class);
                                        Log.d("FirebaseDebug", "👤 Lấy được FullName: " + fullName);
                                        Log.d("FirebaseDebug", "👤 Lấy được Contentid: " + ContendId);

                                        listRejected.add(new Post(ContendId, Title, fullName, PublishedTime, "Từ chối"));
                                        adapterRejected.notifyDataSetChanged();
                                        tvRejected.setText(String.valueOf(listRejected.size()));

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void getAproveAdminPosts(RecyclerView recyclerView) {
//        database.child("Content").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()) {
//                    // clear list
//                    listAproveAdmin.clear();
//
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        String Status = dataSnapshot.child("Status").getValue(String.class);
//                        String userID = dataSnapshot.child("UserId").getValue(String.class);
//                        if ("Done".equals(Status) && userID != null && userID.equals(userId)) {
//                            String Title = dataSnapshot.child("Title").getValue(String.class);
//                             String PublishedTime = dataSnapshot.child("PublishedTime").getValue(String.class);
//                             String ContendId = dataSnapshot.getKey();
//
//                            // Lấy FullName từ collection "User"
//                            database.child("User").child(userID).addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
//                                    String fullName = "";
//                                    if (userSnapshot.exists()) {
//                                        fullName = userSnapshot.child("FullName").getValue(String.class);
//                                        Log.d("FirebaseDebug", "👤 Lấy được FullName: " + fullName);
//                                        Log.d("FirebaseDebug", "👤 Lấy được Contentid: " + ContendId);
//
//                                        listAproveAdmin.add(new Post( ContendId, Title, fullName, PublishedTime, "Chờ duyệt"));
//                                        adapterAproveAdmin.notifyDataSetChanged();
//                                    }
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//
//                        }
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void getAproveAdminPosts(RecyclerView recyclerView) {
        database.child("Content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // Clear list trước khi thêm dữ liệu mới
                    listAproveAdmin.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String status = dataSnapshot.child("Status").getValue(String.class);
                        // Bỏ kiểm tra userID
                        if ("Done".equals(status)) {
                            String title = dataSnapshot.child("Title").getValue(String.class);
                            String publishedTime = dataSnapshot.child("CreatedTime").getValue(String.class);
                            String contentId = dataSnapshot.getKey();
                            String userID = dataSnapshot.child("UserId").getValue(String.class);

                            // Lấy FullName từ collection "User"
                            if (userID != null) {
                                database.child("User").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        String fullName = "";
                                        if (userSnapshot.exists()) {
                                            fullName = userSnapshot.child("FullName").getValue(String.class);
                                        }

                                        // Thêm bài vào list
                                        listAproveAdmin.add(new Post(contentId, title, fullName, publishedTime, "Chờ duyệt"));
                                        adapterAproveAdmin.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("FirebaseDebug", "Lỗi khi lấy User: " + error.getMessage());
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseDebug", "Lỗi khi lấy Content: " + error.getMessage());
            }
        });
    }


}