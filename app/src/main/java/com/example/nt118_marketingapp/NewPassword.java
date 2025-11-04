package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewPassword extends AppCompatActivity {

    private TextInputEditText edtPassword1, edtPassword2;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("User");

        // Ánh xạ view
        edtPassword1 = findViewById(R.id.edtEmail);     // ô nhập mật khẩu mới
        edtPassword2 = findViewById(R.id.edtPassword);  // ô xác nhận mật khẩu
        findViewById(R.id.btnSignIn).setOnClickListener(v -> changePassword());
        findViewById(R.id.btncancel).setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String newPass1 = edtPassword1.getText().toString().trim();
        String newPass2 = edtPassword2.getText().toString().trim();

        if (newPass1.isEmpty() || newPass2.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass1.equals(newPass2)) {
            Toast.makeText(this, "Mật khẩu mới không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Không có người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Đổi mật khẩu trên Firebase Authentication
        user.updatePassword(newPass1)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // ✅ Cập nhật mật khẩu trong Realtime Database
                        userRef.child(userId).child("Password").setValue(newPass1)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();

                                        // Quay lại ProfileActivity
                                        Intent intent = new Intent(NewPassword.this, Profile.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Đổi mật khẩu trên Database thất bại!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Lỗi: hãy đăng nhập lại và thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}