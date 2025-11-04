package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewPassword extends AppCompatActivity {

    private TextInputEditText edtPassword1, edtPassword2;
    private Button btnSignIn;
    private TextView tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        edtPassword1 = findViewById(R.id.edtEmail);
        edtPassword2 = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(v -> {
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
            if (user != null) {
                user.updatePassword(newPass1).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                        finish(); // Quay lại Profile
                    } else {
                        Toast.makeText(this, "Lỗi: Hãy đăng nhập lại rồi thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Không có người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            }
        });

        tvForgotPassword.setOnClickListener(v -> finish());
    }
}