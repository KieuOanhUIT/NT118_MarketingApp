package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class Profile extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvFullName, tvPosition, tvPhone, tvEmail;
    private TextView btnEditProfile, tvForgotPassword;

    // Dùng ActivityResultLauncher để nhận kết quả khi quay lại từ EditProfile
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
                        // Lấy dữ liệu cập nhật
                        String newName = result.getData().getStringExtra("fullName");
                        String newPosition = result.getData().getStringExtra("position");
                        String newPhone = result.getData().getStringExtra("phone");
                        String newEmail = result.getData().getStringExtra("email");

                        // Cập nhật giao diện
                        tvFullName.setText(newName);
                        tvPosition.setText(newPosition);
                        tvPhone.setText(newPhone);
                        tvEmail.setText(newEmail);
                    }
                }
        );

        // Sự kiện khi nhấn “Chỉnh sửa thông tin”
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);

            // Truyền dữ liệu hiện tại sang EditProfile
            intent.putExtra("fullName", tvFullName.getText().toString());
            intent.putExtra("position", tvPosition.getText().toString());
            intent.putExtra("phone", tvPhone.getText().toString());
            intent.putExtra("email", tvEmail.getText().toString());

            // Mở trang chỉnh sửa và chờ kết quả trả về
            editProfileLauncher.launch(intent);
        });

        // Xử lý nếu người dùng nhấn “Đổi mật khẩu”
        tvForgotPassword.setOnClickListener(v ->
                // TODO: sau có thể mở màn hình đổi mật khẩu
                android.widget.Toast.makeText(this, "Chức năng đổi mật khẩu sắp ra mắt!", android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvPosition = findViewById(R.id.tvPosition);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);

        btnEditProfile = findViewById(R.id.btnSignIn); // include button
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }
}