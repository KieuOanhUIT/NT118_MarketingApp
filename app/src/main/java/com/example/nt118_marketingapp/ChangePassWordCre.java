package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ChangePassWordCre extends AppCompatActivity {

    private TextInputEditText edtPassword;
    private Button btnSignIn; // nút "Xác thực"
    private Button btncancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass_word_cre);

        // Ánh xạ view
        edtPassword = findViewById(R.id.edtpw);
        btnSignIn = findViewById(R.id.btnSignIn);
        btncancel = findViewById(R.id.btncancel);

        // Xử lý khi nhấn nút
        btnSignIn.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thông báo xác thực thành công
            Toast.makeText(this, "Xác thực mật khẩu thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển sang trang NewPassword
            Intent intent = new Intent(ChangePassWordCre.this, NewPassword.class);
            startActivity(intent);
        });
        // Nút Hủy → Quay lại trang Profile
        btncancel.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hủy thay đổi", Toast.LENGTH_SHORT).show();
            finish(); // 🔹 Kết thúc Activity hiện tại → trở về ProfileActivity
        });
    }
}