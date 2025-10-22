package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    // Khai báo các view
    private TextView tvTitle, tvSubtitle, tvForgotPassword;
    private TextInputEditText edtEmail, edtPassword;
    private TextView btnSignIn; // nếu trong layout là Button thì đổi sang Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Ánh xạ view
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Xử lý sự kiện Đăng nhập
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                // Tạm thời hiển thị thông báo và chuyển sang Profile
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignInActivity.this, Profile.class);
                startActivity(intent);
                finish(); // Đóng SignIn để không quay lại
            }
        });

        // ✅ Khi nhấn “Quên mật khẩu” → chuyển sang ForgotPasswordActivity
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}