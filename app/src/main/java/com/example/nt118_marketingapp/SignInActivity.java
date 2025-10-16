package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // Xử lý Insets cho EdgeToEdge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ 2 nút
        Button confirmButton = findViewById(R.id.commonButton);
        Button cancelButton = findViewById(R.id.commonButtonOutline);

        // Xử lý sự kiện
        confirmButton.setOnClickListener(v -> {
            // TODO: Xử lý đăng nhập
        });

        cancelButton.setOnClickListener(v -> {
            finish(); // Ví dụ: quay lại màn hình trước
        });
    }
}
