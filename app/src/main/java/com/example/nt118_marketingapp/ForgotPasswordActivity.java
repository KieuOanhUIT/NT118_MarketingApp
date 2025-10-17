package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextView tvForgotPassword;
    private View btnSignIn; // nếu "common_button" là layout riêng, bạn có thể ánh xạ nó như View

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // gắn layout XML

        // Ánh xạ các View từ XML
        edtEmail = findViewById(R.id.edtEmail);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Xử lý khi bấm nút "Sign In" hoặc "Gửi yêu cầu"
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Thực hiện logic gửi email khôi phục mật khẩu
                Toast.makeText(this, "Yêu cầu khôi phục đã được gửi cho " + email, Toast.LENGTH_LONG).show();
            }
        });

        // Xử lý khi bấm "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> finish()); // hoặc chuyển sang LoginActivity
    }
}