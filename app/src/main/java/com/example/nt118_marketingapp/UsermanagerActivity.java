package com.example.nt118_marketingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.nt118_marketingapp.adapters.UserAdapter;
import com.example.nt118_marketingapp.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsermanagerActivity extends AppCompatActivity {

    private List<User> userList;
    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private FloatingActionButton btnAdd;
    private BottomNavigationView bottomNavigationView;
    
    // User data
    private String userId, fullName, roleName, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermanager);
        
        // Nhận thông tin người dùng từ Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        fullName = intent.getStringExtra("fullName");
        roleName = intent.getStringExtra("roleName");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        initViews();
        setupBottomNavigation(); // Phải gọi trước khi load dữ liệu

        userList = new ArrayList<>();
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(this, userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(int position) {
                User user = userList.get(position);
                AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, user, position, true,
                        (updatedUser, pos, isEdit, password) -> {
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(user.getUserId());
                            HashMap<String, Object> updatedMap = new HashMap<>();
                            updatedMap.put("FullName", updatedUser.getFullName());
                            updatedMap.put("Phone", updatedUser.getPhone());
                            updatedMap.put("RoleName", updatedUser.getRoleName());
                            updatedMap.put("Email", updatedUser.getEmail());

                            if (password != null && !password.isEmpty()) {
                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(password);
                            }

                            dbRef.updateChildren(updatedMap).addOnSuccessListener(unused ->
                                    Toast.makeText(UsermanagerActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show());
                        });
                dialog.show();
            }

            @Override
            public void onDelete(int position) {
                User user = userList.get(position);
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(UsermanagerActivity.this, position, pos -> {
                    FirebaseDatabase.getInstance().getReference("User").child(user.getUserId())
                            .removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(UsermanagerActivity.this, "Đã xóa người dùng!", Toast.LENGTH_SHORT).show());
                });
                dialog.show();
            }
        });

        recyclerUsers.setAdapter(userAdapter);
        loadData();

        btnAdd.setOnClickListener(v -> {
            AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, null, -1, false,
                    (newUser, pos, isEdit, password) -> {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.createUserWithEmailAndPassword(newUser.getEmail(), password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String uid = auth.getCurrentUser().getUid();
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("Email", newUser.getEmail());
                                        map.put("FullName", newUser.getFullName());
                                        map.put("Phone", newUser.getPhone());
                                        map.put("RoleName", newUser.getRoleName());

                                        FirebaseDatabase.getInstance().getReference("User").child(uid)
                                                .setValue(map)
                                                .addOnSuccessListener(a -> Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show());
                                    }
                                });
                    });
            dialog.show();
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_usermanagement);
        
        // Ẩn tab dành cho Admin nếu không phải Admin (thường chỉ Admin vào trang này)
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intentNav = new Intent(getApplicationContext(), DashboardActivity.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_contentmanagement) {
                Intent intentNav = new Intent(getApplicationContext(), ContentListActivity.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_approve) {
                Intent intentNav = new Intent(getApplicationContext(), ReviewContentActivity.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_usermanagement) {
                Intent intentNav = new Intent(getApplicationContext(), UsermanagerActivity.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_notification) {
                Intent intentNav = new Intent(getApplicationContext(), NotificationActivity.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;

            } else if (itemId == R.id.navigation_profile) {
                Intent intentNav = new Intent(getApplicationContext(), Profile.class);
                attachUserData(intentNav);
                startActivity(intentNav);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
    
    // Helper method: attach user data to Intent
    private void attachUserData(Intent intent) {
        intent.putExtra("userId", userId);
        intent.putExtra("fullName", fullName);
        intent.putExtra("roleName", roleName);
        intent.putExtra("phone", phone);
        intent.putExtra("email", email);
    }

    private void initViews() {
        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAddUser);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void loadData() {
        FirebaseDatabase.getInstance().getReference("User")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String uid = data.getKey();
                            String fullName = data.child("FullName").getValue(String.class);
                            String email = data.child("Email").getValue(String.class);
                            String phone = data.child("Phone").getValue(String.class);
                            String role = data.child("RoleName").getValue(String.class);

                            userList.add(new User(uid, fullName, role, email, phone));
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void applyNavVisibility() {
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_usermanagement);
        applyNavVisibility(); // Ẩn tab nếu không phải admin

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                intent = new Intent(this, DashboardActivity.class);
            } else if (id == R.id.navigation_contentmanagement) {
                intent = new Intent(this, ContentListActivity.class);
            } else if (id == R.id.navigation_approve) {
                intent = new Intent(this, ReviewContentActivity.class);
            } else if (id == R.id.navigation_usermanagement) {
                return true;
            } else if (id == R.id.navigation_notification) {
                intent = new Intent(this, NotificationActivity.class);
            } else if (id == R.id.navigation_profile) {
                intent = new Intent(this, Profile.class);
            }

            if (intent != null) {
                attachUserData(intent);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}