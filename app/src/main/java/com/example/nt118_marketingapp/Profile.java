package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // Gán dữ liệu mẫu ban đầu
        tvFullName.setText("Nguyễn Văn A");
        tvPosition.setText("Nhân viên Marketing");
        tvPhone.setText("0123 456 789");
        tvEmail.setText("nguyenvana@gmail.com");

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

        // Khi nhấn “Đổi mật khẩu” → chuyển sang trang ChangePasswordCre
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

        btnEditProfile = findViewById(R.id.btnUp); // nút chỉnh sửa thông tin
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // nút đổi mật khẩu
    }
}