package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class Profile extends AppCompatActivity {

    private ImageView imgAvatar, btnLogout;
    private TextView tvFullName, tvPosition, tvPhone, tvEmail, tvForgotPassword;
    private Button btnEditProfile;
    private BottomNavigationView bottomNavigationView;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String userId;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    // Lưu thông tin người dùng
    private String fullName, roleName, phone, email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("User");

        // Lấy userId từ Intent hoặc Firebase
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        if (userId == null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải dữ liệu người dùng
        loadUserProfile(userId);

        // Khi chỉnh sửa hồ sơ xong -> cập nhật lại
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserProfile(userId); // reload lại dữ liệu sau khi chỉnh sửa
                    }
                }
        );

        // Nút chỉnh sửa hồ sơ
        btnEditProfile.setOnClickListener(v -> {
            Intent editIntent = new Intent(Profile.this, EditProfile.class);
            editIntent.putExtra("userId", userId);
            editIntent.putExtra("fullName", fullName);
            editIntent.putExtra("position", roleName);
            editIntent.putExtra("phone", phone);
            editIntent.putExtra("email", email);
            editProfileLauncher.launch(editIntent);
        });

        // Nút đổi mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent changePassIntent = new Intent(Profile.this, ChangePassWordCre.class);
            changePassIntent.putExtra("userId", userId);
            startActivity(changePassIntent);
        });

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        // Thanh điều hướng
        setupBottomNavigation();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        tvFullName = findViewById(R.id.tvFullName);
        tvPosition = findViewById(R.id.tvPosition);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void loadUserProfile(String userId) {
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullName = snapshot.child("FullName").getValue(String.class);
                    roleName = snapshot.child("RoleName").getValue(String.class);
                    phone = snapshot.child("Phone").getValue(String.class);
                    email = snapshot.child("Email").getValue(String.class);

                    tvFullName.setText(fullName != null ? fullName : "");
                    tvPosition.setText(roleName != null ? roleName : "");
                    tvPhone.setText(phone != null ? phone : "");
                    tvEmail.setText(email != null ? email : "");
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

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    mAuth.signOut();

                    Intent intent = new Intent(Profile.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(Profile.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
            } else if (itemId == R.id.navigation_contentmanagement) {
                intent = new Intent(getApplicationContext(), ContentListActivity.class);
            } else if (itemId == R.id.navigation_approve) {
                intent = new Intent(getApplicationContext(), ReviewContentActivity.class);
            } else if (itemId == R.id.navigation_usermanagement) {
                intent = new Intent(getApplicationContext(), UsermanagerActivity.class);
            } else if (itemId == R.id.navigation_notification) {
                intent = new Intent(getApplicationContext(), NotificationActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }

            if (intent != null) {
                intent.putExtra("userId", userId);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}