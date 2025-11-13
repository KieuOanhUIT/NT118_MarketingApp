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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsermanagerActivity extends AppCompatActivity {

    // khai báo dữ liệu
    private List<User> userList;
    // khai báo recycle view
    private RecyclerView recyclerUsers;

    // khai báo adapter
    private UserAdapter userAdapter;

    private FloatingActionButton btnAdd;
    private BottomNavigationView bottomNavigationView;

    // hàm load view by id
    public void findViewById () {
        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAddUser);
    }

    // hàm load dữ liệu từ database hiển thị lên màn hình
    public void loadData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        // truy vấn table user
        databaseReference.child("User")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // clear list
                            userList.clear();

                            // duyệt trong bảng user
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                // lấy dữ liệu
                                String UserId = dataSnapshot.getKey();
                                String FullName = dataSnapshot.child("FullName").getValue(String.class);
                                String Email = dataSnapshot.child("Email").getValue(String.class);
                                String Phone = dataSnapshot.child("Phone").getValue(String.class);
                                String RoleName = dataSnapshot.child("RoleName").getValue(String.class);

                                // thêm vào userlist
                                userList.add(new User( UserId,FullName, RoleName, Email, Phone));

                                // set notify change
                                userAdapter.notifyDataSetChanged();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermanager);

        // tham chiếu đến các thành phần giao diện
        findViewById();

        // khởi tạo list user
        userList = new ArrayList<>();

        // Cấu hình RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));


        // khởi tạo user Adapter
        userAdapter = new UserAdapter(this, userList, new UserAdapter.OnUserActionListener() {

//            public void onEdit(int position) {
//                User user = userList.get(position);
//                AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, user, position, true, (updatedUser, pos, isEdit, password) -> {
//                    userList.set(pos, updatedUser);
//                    userAdapter.notifyItemChanged(pos);
//                    Toast.makeText(UsermanagerActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
//                });
//                dialog.show();
//            }
            @Override
            public void onEdit(int position) {
                User user = userList.get(position);
                AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, user, position, true, (updatedUser, pos, isEdit, password) -> {

                    // 1️⃣ Lấy reference tới user trong Realtime Database
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(user.getUserId());

                    // 2️⃣ Tạo map chứa dữ liệu cập nhật
                    HashMap<String, Object> updatedMap = new HashMap<>();
                    updatedMap.put("FullName", updatedUser.getFullName());
                    updatedMap.put("Phone", updatedUser.getPhone());
                    updatedMap.put("RoleName", updatedUser.getRoleName());
                    updatedMap.put("Email", updatedUser.getEmail());

                    // Nếu muốn cập nhật password, cần dùng FirebaseAuth
                    if (password != null && !password.isEmpty()) {
                        FirebaseAuth.getInstance().getCurrentUser().updatePassword(password)
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(UsermanagerActivity.this, "Cập nhật password thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    // 3️⃣ Cập nhật Realtime Database
                    dbRef.updateChildren(updatedMap)
                            .addOnSuccessListener(unused -> {
//                                userList.set(pos, updatedUser); // cập nhật list local
//                                userAdapter.notifyItemChanged(pos); // cập nhật RecyclerView
                                Toast.makeText(UsermanagerActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UsermanagerActivity.this, "Lỗi khi cập nhật DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                });
                dialog.show();
            }


//            @Override
//            public void onDelete(int position) {
//                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(UsermanagerActivity.this, position, pos -> {
//                    userList.remove(pos);
//                    userAdapter.notifyItemRemoved(pos);
//                    Toast.makeText(UsermanagerActivity.this, "Đã xóa người dùng!", Toast.LENGTH_SHORT).show();
//                });
//                dialog.show();
//            }


            @Override
            public void onDelete(int position) {
                User user = userList.get(position);

                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                        UsermanagerActivity.this,
                        position,
                        pos -> {

                            // Xóa user khỏi Realtime Database
                            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                    .getReference("User")
                                    .child(user.getUserId());

                            dbRef.removeValue()
                                    .addOnSuccessListener(unused -> {
                                        // Xóa khỏi list local và cập nhật RecyclerView
//                                        userList.remove(pos);
//                                        userAdapter.notifyItemRemoved(pos);
                                        Toast.makeText(UsermanagerActivity.this, "Đã xóa người dùng!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(UsermanagerActivity.this, "Lỗi khi xóa DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Lưu ý: nếu muốn xóa luôn tài khoản Authentication, cần Firebase Admin SDK
                        }
                );
                dialog.show();
            }


        });

        // set adapter cho recycle view
        recyclerUsers.setAdapter(userAdapter);

        // Các chức năng
        // load dữ liệu từ database
        loadData();

        // thêm người dùng
        btnAdd.setOnClickListener(v -> {
            AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, null, -1, false, (newUser, pos, isEdit, password) -> {
                // khai báo database
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User");

                auth.createUserWithEmailAndPassword(newUser.getEmail(), password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                                String userId = auth.getCurrentUser().getUid();

                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("Email", newUser.getEmail());
                                userMap.put("FullName", newUser.getFullName());
                                userMap.put("Phone", newUser.getPhone());
                                userMap.put("RoleName", newUser.getRoleName());
                                userMap.put("Password", password);

                                dbRef.child(userId).setValue(userMap)
                                        .addOnSuccessListener(unused -> {
                                            Log.d("FirebaseDebug", "Saved user: " + userId);

                                            // Cập nhật list và RecyclerView ở đây
//                                            userList.add(newUser);
//                                            userAdapter.notifyItemInserted(userList.size() - 1);

                                            Toast.makeText(UsermanagerActivity.this, "Thêm người dùng thành công!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UsermanagerActivity.this, "Lỗi khi lưu vào DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(UsermanagerActivity.this, "Tạo tài khoản thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


            });
            dialog.show();
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_usermanagement);

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
                startActivity(new Intent(getApplicationContext(), Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

}

