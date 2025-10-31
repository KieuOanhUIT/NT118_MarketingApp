package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvFullName, tvPosition, tvPhone, tvEmail;
    private TextView btnEditProfile, tvForgotPassword;
    private BottomNavigationView bottomNavigationView;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        // Lấy dữ liệu người dùng thật từ Firebase
        loadUserProfile("U001"); // tạm test với U001

        // Khởi tạo launcher để nhận kết quả trả về
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

        // Khi nhấn "Chỉnh sửa thông tin"
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);
            intent.putExtra("fullName", tvFullName.getText().toString());
            intent.putExtra("position", tvPosition.getText().toString());
            intent.putExtra("phone", tvPhone.getText().toString());
            intent.putExtra("email", tvEmail.getText().toString());
            editProfileLauncher.launch(intent);
        });

        // Khi nhấn “Đổi mật khẩu”
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, ChangePassWordCre.class);
            startActivity(intent);
        });

        // Thiết lập bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

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

    // 🔹 Hàm đọc dữ liệu từ Firebase
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
}