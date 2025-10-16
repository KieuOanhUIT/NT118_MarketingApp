package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        ImageView imgIcon = findViewById(R.id.imgDetailIcon);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvMessage = findViewById(R.id.tvDetailMessage);
        TextView tvTime = findViewById(R.id.tvDetailTime);

        // Nhận dữ liệu từ Intent
        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        String time = getIntent().getStringExtra("time");
        int iconResId = getIntent().getIntExtra("icon", R.drawable.ic_notification);

        // Hiển thị dữ liệu
        imgIcon.setImageResource(iconResId);
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvTime.setText(time);
    }
}
