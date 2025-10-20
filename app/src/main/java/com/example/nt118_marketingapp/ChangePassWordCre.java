package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePassWordCre extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private Button btnSignIn; // thực tế là nút "Đổi mật khẩu"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass_word_cre); // gắn layout XML

        // Ánh xạ view từ XML
        edtEmail = findViewById(R.id.edtEmail);

        // Vì bạn dùng <include layout="@layout/common_button">
        // nên nút nằm trong layout include — phải lấy bằng 2 bước:
        View includeView = findViewById(R.id.btnSignIn);
        btnSignIn = includeView.findViewById(R.id.btnSignIn); // id của nút trong common_button.xml

        // Xử lý sự kiện khi nhấn nút
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Xử lý logic đổi mật khẩu tại đây (gửi API, cập nhật DB, v.v.)
            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();

            // Quay lại màn hình trước (nếu cần)
            finish();
        });
    }
}
