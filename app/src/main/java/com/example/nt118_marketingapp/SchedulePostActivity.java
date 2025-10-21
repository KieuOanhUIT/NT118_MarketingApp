package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SchedulePostActivity extends AppCompatActivity {

    private Spinner spinnerPlatform;
    private EditText etDateTime;
    private Button btnConfirm, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_post);

        spinnerPlatform = findViewById(R.id.spinnerPlatform);
        etDateTime = findViewById(R.id.etDateTime);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        // Gán danh sách nền tảng (Facebook, TikTok)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Facebook", "TikTok"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlatform.setAdapter(adapter);

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            String platform = spinnerPlatform.getSelectedItem().toString();
            String time = etDateTime.getText().toString().trim();
            if (time.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập thời gian đăng!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Đã đặt lịch đăng lên " + platform + " vào " + time,
                        Toast.LENGTH_LONG).show();
                finish(); // Quay lại màn hình trước
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
