package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    // Khai báo các view
    private TextView tvTitle, tvSubtitle, tvForgotPassword;
    private TextInputEditText edtEmail, edtPassword;
    private TextView btnSignIn; // nếu trong common_button có TextView làm nút, còn nếu là Button thì đổi kiểu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in); // <-- đặt đúng tên file XML của bạn

        // Ánh xạ (findViewById)
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                // Tạm thời hiển thị thông báo, bạn có thể thay bằng logic đăng nhập thật
                Toast.makeText(this, "Đăng nhập với: " + email, Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện khi nhấn "Quên mật khẩu"
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng quên mật khẩu sắp ra mắt!", Toast.LENGTH_SHORT).show()
        );
    }
}