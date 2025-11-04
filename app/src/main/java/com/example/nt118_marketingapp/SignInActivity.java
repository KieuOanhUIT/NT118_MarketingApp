package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("User");

        // Ánh xạ view
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        findViewById(R.id.btnSignIn).setOnClickListener(v -> signIn());

        // Khi nhấn "Quên mật khẩu?"
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void signIn() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng nhập bằng Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid(); // Lấy Firebase UID

                            // Lấy thông tin user từ Realtime Database
                            getUserInfo(userId, email);
                        }
                    } else {
                        // Đăng nhập thất bại
                        Toast.makeText(SignInActivity.this,
                                "Sai email hoặc mật khẩu!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserInfo(String userId, String email) {
        // Đọc thông tin user từ Realtime Database theo Firebase UID
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu user
                    String fullName = snapshot.child("FullName").getValue(String.class);
                    String roleName = snapshot.child("RoleName").getValue(String.class);
                    String phone = snapshot.child("Phone").getValue(String.class);

                    Toast.makeText(SignInActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang DashboardActivity
                    Intent intent = new Intent(SignInActivity.this, DashboardActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("fullName", fullName);
                    intent.putExtra("roleName", roleName);
                    intent.putExtra("phone", phone);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    // Trường hợp user có trong Auth nhưng chưa có trong Database
                    Toast.makeText(SignInActivity.this, "Không tìm thấy thông tin user!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Lỗi khi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}