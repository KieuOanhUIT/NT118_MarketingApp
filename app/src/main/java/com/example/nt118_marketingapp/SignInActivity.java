package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        findViewById(R.id.btnSignIn).setOnClickListener(v -> signIn());

        userRef = FirebaseDatabase.getInstance().getReference("User");
    }

    private void signIn() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String dbEmail = userSnap.child("Email").getValue(String.class);
                    String dbPassword = userSnap.child("Password").getValue(String.class);

                    if (email.equals(dbEmail) && password.equals(dbPassword)) {
                        found = true;
                        String userId = userSnap.getKey(); // 🔹 Lấy key userId
                        String fullName = userSnap.child("FullName").getValue(String.class);
                        String roleName = userSnap.child("RoleName").getValue(String.class);
                        String phone = userSnap.child("Phone").getValue(String.class);

                        Toast.makeText(SignInActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // 🔹 Truyền dữ liệu qua Profile
                        Intent intent = new Intent(SignInActivity.this, Profile.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("fullName", fullName);
                        intent.putExtra("roleName", roleName);
                        intent.putExtra("phone", phone);
                        intent.putExtra("email", dbEmail);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(SignInActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Lỗi khi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}