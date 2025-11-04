package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextView tvForgotPassword;
    private View btnSubmit;

    private FirebaseAuth mAuth; // Khai báo FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSubmit = findViewById(R.id.btnSignIn);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý khi nhấn nút "Gửi yêu cầu"
        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi email reset mật khẩu qua Firebase
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Đã gửi email khôi phục đến " + email,
                                    Toast.LENGTH_LONG).show();
                            finish(); // Quay lại màn hình trước (thường là SignInActivity)
                        } else {
                            Toast.makeText(this,
                                    "Gửi email thất bại. Kiểm tra lại địa chỉ email!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Khi nhấn "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> finish());
    }
}