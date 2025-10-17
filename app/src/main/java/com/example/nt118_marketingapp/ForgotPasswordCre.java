package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordCre extends AppCompatActivity {

    // Khai báo các view trong XML
    private TextInputEditText edtEmail;
    private TextView tvForgotPassword;
    private Button btnSignIn; // nút được include từ common_button.xml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_cre); // gắn layout XML vào Activity

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // do "common_button" là layout được include, nên bạn phải tìm nút con của nó:
        View btnInclude = findViewById(R.id.btnSignIn);
        btnSignIn = btnInclude.findViewById(R.id.btnSignIn); // id của nút trong common_button.xml

        // Xử lý khi nhấn nút xác nhận
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Thực hiện logic gửi xác nhận / khôi phục mật khẩu ở đây
                Toast.makeText(this, "Đã gửi yêu cầu đến " + email, Toast.LENGTH_LONG).show();
            }
        });

        // Xử lý khi nhấn "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> {
            finish(); // đóng activity hiện tại, quay lại màn hình đăng nhập
        });
    }
}