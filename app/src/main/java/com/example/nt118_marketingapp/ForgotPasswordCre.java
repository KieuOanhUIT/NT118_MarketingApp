package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordCre extends AppCompatActivity {

    // Khai báo các view trong XML
    private TextInputEditText edtEmail;
    private TextView tvForgotPassword;
    private Button btnSignIn; // nút Submit (được include từ common_button.xml)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_cre);

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Khi nhấn nút Submit (Gửi yêu cầu)
        btnSignIn.setOnClickListener(v -> {
            String code = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã xác thực", Toast.LENGTH_SHORT).show();
            } else {
                // Hiển thị thông báo
                Toast.makeText(this, "Đã xác thực thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang trang NewPassword
                Intent intent = new Intent(ForgotPasswordCre.this, NewPassword.class);
                startActivity(intent);
                finish(); // Đóng activity hiện tại
            }
        });

        // Khi nhấn "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> finish());
    }
}