package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class EditProfile extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private ImageView imgAvatar;
    private EditText edtFullName, edtPosition, edtPhone, edtEmail;
    private TextView btnSaveInfo, btnSignIn, tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ các view
        initViews();

        // Gán dữ liệu mẫu (hoặc dữ liệu thực từ server, Intent, SharedPreferences,…)
        edtFullName.setText("Nguyễn Văn A");
        edtPosition.setText("Nhân viên Marketing");
        edtPhone.setText("0123 456 789");
        edtEmail.setText("nguyenvana@gmail.com");

        // Nút Lưu thay đổi
        btnSaveInfo.setOnClickListener(v -> saveProfile());

        // Nút Hủy → Quay lại trang Profile
        btnSignIn.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hủy thay đổi", Toast.LENGTH_SHORT).show();
            finish(); // 🔹 Kết thúc Activity hiện tại → trở về ProfileActivity
        });
    }

    // Hàm ánh xạ các view từ XML
    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvTitle = findViewById(R.id.tvTitle);

        edtFullName = findViewById(R.id.edtFullName);
        edtPosition = findViewById(R.id.edtPosition);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);

        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    // Hàm xử lý khi nhấn nút “Lưu thay đổi”
    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String position = edtPosition.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị hộp thoại xác nhận lưu
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận lưu thay đổi")
                .setMessage("Bạn có chắc muốn cập nhật thông tin này không?")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // 🔹 Sau này bạn có thể gọi API hoặc lưu vào database tại đây
                    Toast.makeText(this, "Đã lưu thông tin mới cho " + fullName, Toast.LENGTH_LONG).show();

                    // 🔹 Quay lại trang Profile sau khi lưu
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}