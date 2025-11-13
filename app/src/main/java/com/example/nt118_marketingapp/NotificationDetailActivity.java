package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationDetailActivity extends AppCompatActivity {

    private TextView txtMessage, txtCreatedTime, txtType;
    private TextView txtContentId, txtApprovedBy, txtApprovedAt, txtReason;

    private String notificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        // Bind các TextView
        txtMessage = findViewById(R.id.txtMessage);
        txtCreatedTime = findViewById(R.id.txtCreatedTime);
        txtType = findViewById(R.id.txtType);
        txtContentId = findViewById(R.id.txtContentId);
        txtApprovedBy = findViewById(R.id.txtApprovedBy);
        txtApprovedAt = findViewById(R.id.txtApprovedAt);
        txtReason = findViewById(R.id.txtReason);

        // Lấy notificationId từ Intent
        notificationId = getIntent().getStringExtra("notificationId");
        if (notificationId != null) {
            loadNotificationDetail(notificationId);
        }
    }

    private void loadNotificationDetail(String notificationId) {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("Notification");
        notificationRef.child(notificationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String message = snapshot.child("Message").getValue(String.class);
                String type = snapshot.child("Type").getValue(String.class);
                String contentId = snapshot.child("ContentId").getValue(String.class);
                String createdTime = snapshot.child("CreatedTime").getValue(String.class);

                txtMessage.setText(message);
                txtType.setText(type != null ? type : "-");
                txtCreatedTime.setText(createdTime != null ? createdTime : "-");

                if ("Approval".equals(type) || "Rejection".equals(type)) {
                    if (contentId != null) {
                        loadApprovalDetail(contentId);
                    }
                } else {
                    txtContentId.setText("-");
                    txtApprovedBy.setText("-");
                    txtApprovedAt.setText("-");
                    txtReason.setText("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadApprovalDetail(String contentId) {
        DatabaseReference approvalRef = FirebaseDatabase.getInstance().getReference("Approval");
        approvalRef.orderByChild("ContentId").equalTo(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        for (DataSnapshot approvalSnap : snapshot.getChildren()) {
                            String userId = approvalSnap.child("UserId").getValue(String.class);
                            String reason = approvalSnap.child("Reason").getValue(String.class);
                            String approvedAt = approvalSnap.child("ApprovedAt").getValue(String.class);

                            if (userId != null) {
                                loadUserName(userId, contentId, reason, approvedAt);
                                break; // lấy approval đầu tiên
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadUserName(String userId, String contentId, String reason, String approvedAt) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String fullName = snapshot.child("FullName").getValue(String.class);

                txtContentId.setText(contentId != null ? contentId : "-");
                txtApprovedBy.setText(fullName != null ? fullName : "-");
                txtApprovedAt.setText(approvedAt != null ? approvedAt : "-");
                txtReason.setText(reason != null ? reason : "-");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
