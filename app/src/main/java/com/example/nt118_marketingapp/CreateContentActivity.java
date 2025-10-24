package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateContentActivity extends AppCompatActivity {
    private EditText editTitle;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);

    editTitle = findViewById(R.id.editTitle);
    btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(CreateContentActivity.this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                } else {
                    // Xử lý lưu content ở đây
                    Toast.makeText(CreateContentActivity.this, "Tạo content thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
