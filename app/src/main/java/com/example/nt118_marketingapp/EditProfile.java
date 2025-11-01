package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText edtFullName, edtPosition, edtPhone, edtEmail;
    private TextView btnSaveInfo, btnCancel;
    private String userId;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        userRef = FirebaseDatabase.getInstance().getReference("User");

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        // Nhận dữ liệu hiện tại (được truyền từ Profile)
        edtFullName.setText(intent.getStringExtra("fullName"));
        edtPosition.setText(intent.getStringExtra("position"));
        edtPhone.setText(intent.getStringExtra("phone"));
        edtEmail.setText(intent.getStringExtra("email"));

        btnSaveInfo.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        edtFullName = findViewById(R.id.edtFullName);
        edtPosition = findViewById(R.id.edtPosition);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnCancel = findViewById(R.id.btnSignIn);
    }

    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String position = edtPosition.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng để cập nhật!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận lưu thay đổi")
                .setMessage("Bạn có chắc muốn cập nhật thông tin này không?")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // Cập nhật trực tiếp lên Firebase
                    userRef.child(userId).child("FullName").setValue(fullName);
                    userRef.child(userId).child("RoleName").setValue(position);
                    userRef.child(userId).child("Phone").setValue(phone);
                    userRef.child(userId).child("Email").setValue(email);

                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Gửi kết quả về Profile để reload lại
                    setResult(RESULT_OK, new Intent());
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}