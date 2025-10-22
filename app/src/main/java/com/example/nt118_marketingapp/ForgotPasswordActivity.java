package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextView tvForgotPassword;
    private View btnSubmit; // nút gửi yêu cầu quên mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSubmit = findViewById(R.id.btnSignIn); // nếu layout dùng id này thì giữ nguyên

        // Khi nhấn "Submit" hoặc "Gửi yêu cầu"
        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            } else {
                // Hiện thông báo
                Toast.makeText(this, "Yêu cầu khôi phục đã được gửi cho " + email, Toast.LENGTH_LONG).show();

                // Chuyển sang trang ForgotPasswordCre
                Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordCre.class);
                startActivity(intent);
                finish(); // Đóng trang hiện tại nếu không cần quay lại
            }
        });

        // Khi nhấn "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> {
            finish();
        });
    }
}