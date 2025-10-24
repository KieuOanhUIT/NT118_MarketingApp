package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class NewPassword extends AppCompatActivity {

    private TextInputEditText edtPassword1;
    private TextInputEditText edtPassword2;
    private Button btnSignIn;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password); // gắn layout XML

        // Ánh xạ view
        edtPassword1 = findViewById(R.id.edtEmail);     // ô nhập mật khẩu 1
        edtPassword2 = findViewById(R.id.edtPassword);  // ô nhập mật khẩu 2
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Do bạn dùng <include layout="@layout/common_button"> nên cần lấy button con bên trong include
        View btnInclude = findViewById(R.id.btnSignIn);
        btnSignIn = btnInclude.findViewById(R.id.btnSignIn); // id của nút trong common_button.xml

        // Sự kiện khi nhấn nút "Xác nhận" / "Sign In"
        btnSignIn.setOnClickListener(v -> {
            String pass1 = edtPassword1.getText().toString().trim();
            String pass2 = edtPassword2.getText().toString().trim();

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Gửi yêu cầu đổi mật khẩu lên server hoặc xử lý lưu mật khẩu ở đây
            Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_LONG).show();

            // Quay lại màn hình đăng nhập (nếu muốn)
            finish();
        });

        // Khi nhấn "Quay lại đăng nhập"
        tvForgotPassword.setOnClickListener(v -> finish());
    }
}