package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

public class Profile extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvFullName, tvPosition, tvPhone, tvEmail;
    private TextView btnEditProfile, tvForgotPassword;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        // 🔹 Nhận dữ liệu từ SignInActivity
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String fullName = intent.getStringExtra("fullName");
        String roleName = intent.getStringExtra("roleName");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");

        // Nếu có sẵn thông tin thì hiển thị luôn, không cần load Firebase
        if (fullName != null) {
            tvFullName.setText(fullName);
            tvPosition.setText(roleName);
            tvPhone.setText(phone);
            tvEmail.setText(email);
        }

        // Nếu không có thì load từ Firebase theo userId
        if (userId != null && (fullName == null || fullName.isEmpty())) {
            loadUserProfile(userId);
        }

        // Khởi tạo launcher chỉnh sửa thông tin
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String newName = result.getData().getStringExtra("fullName");
                        String newPosition = result.getData().getStringExtra("position");
                        String newPhone = result.getData().getStringExtra("phone");
                        String newEmail = result.getData().getStringExtra("email");

                        tvFullName.setText(newName);
                        tvPosition.setText(newPosition);
                        tvPhone.setText(newPhone);
                        tvEmail.setText(newEmail);
                    }
                }
        );

        // 🔹 Chỉnh sửa thông tin
        btnEditProfile.setOnClickListener(v -> {
            Intent editIntent = new Intent(Profile.this, EditProfile.class);
            editIntent.putExtra("fullName", tvFullName.getText().toString());
            editIntent.putExtra("position", tvPosition.getText().toString());
            editIntent.putExtra("phone", tvPhone.getText().toString());
            editIntent.putExtra("email", tvEmail.getText().toString());
            editProfileLauncher.launch(editIntent);
        });

        // 🔹 Đổi mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent1 = new Intent(Profile.this, ChangePassWordCre.class);
            startActivity(intent1);
        });

        // 🔹 Bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvPosition = findViewById(R.id.tvPosition);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    // 🔹 Hàm đọc dữ liệu từ Firebase nếu cần
    private void loadUserProfile(String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(userId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("FullName").getValue(String.class);
                    String roleName = snapshot.child("RoleName").getValue(String.class);
                    String phone = snapshot.child("Phone").getValue(String.class);
                    String email = snapshot.child("Email").getValue(String.class);

                    tvFullName.setText(fullName);
                    tvPosition.setText(roleName);
                    tvPhone.setText(phone);
                    tvEmail.setText(email);
                } else {
                    tvFullName.setText("Không tìm thấy người dùng!");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvFullName.setText("Lỗi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    // 🔹 Bottom Navigation setup
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

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
            } else if (itemId == R.id.navigation_notification) {
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}