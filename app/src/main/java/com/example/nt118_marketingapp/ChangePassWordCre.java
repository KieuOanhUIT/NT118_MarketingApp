package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;

public class ChangePassWordCre extends AppCompatActivity {

    private TextInputEditText edtPassword;
    private Button btnSignIn; // nút "Xác thực"
    private Button btncancel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass_word_cre);

        edtPassword = findViewById(R.id.edtpw);
        btnSignIn = findViewById(R.id.btnSignIn);
        btncancel = findViewById(R.id.btncancel);
        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            if (email == null) {
                Toast.makeText(this, "Không thể xác định email người dùng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xác thực lại người dùng bằng email và mật khẩu hiện tại
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn nhập mật khẩu mới
                    Intent intent = new Intent(ChangePassWordCre.this, NewPassword.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btncancel.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hủy thay đổi mật khẩu", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}