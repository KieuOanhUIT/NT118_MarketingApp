package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationDetailActivity extends AppCompatActivity {

    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView txtMessage, txtCreatedTime, txtType;
    private TextView txtContentId, txtApprovedBy, txtApprovedAt, txtReason;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        bindView();
        dbRef = FirebaseDatabase.getInstance().getReference();

        String notificationId = getIntent().getStringExtra("notificationId");
        if (notificationId != null) {
            loadNotificationDetail(notificationId);
        }
    }

    /* ================= BIND VIEW ================= */

    private void bindView() {
        ivIcon = findViewById(R.id.ivIcon);
        tvTitle = findViewById(R.id.tvTitle);

        txtMessage = findViewById(R.id.txtMessage);
        txtCreatedTime = findViewById(R.id.txtCreatedTime);
        txtType = findViewById(R.id.txtType);

        txtContentId = findViewById(R.id.txtContentId);
        txtApprovedBy = findViewById(R.id.txtApprovedBy);
        txtApprovedAt = findViewById(R.id.txtApprovedAt);
        txtReason = findViewById(R.id.txtReason);
    }

    /* ================= LOAD NOTIFICATION ================= */

    private void loadNotificationDetail(String notificationId) {
        dbRef.child("Notification").child(notificationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String message = snapshot.child("Message").getValue(String.class);
                        String type = snapshot.child("Type").getValue(String.class);
                        String createdTime = snapshot.child("CreatedTime").getValue(String.class);
                        String userId = snapshot.child("UserId").getValue(String.class);

                        txtMessage.setText(message != null ? message : "-");
                        txtCreatedTime.setText(createdTime != null ? createdTime : "-");
                        txtType.setText("Loại: " + (type != null ? type : "-"));

                        if ("Approval".equals(type)) {
                            showApprovalUI();
                            loadApprovalDetail(userId);
                        } else if ("Task".equals(type)) {
                            showTaskUI();
                        } else {
                            showDefaultUI();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    /* ================= APPROVAL ================= */

    private void showApprovalUI() {
        ivIcon.setImageResource(R.drawable.ic_approve);
        tvTitle.setText("Thông báo duyệt nội dung");

        txtContentId.setVisibility(View.VISIBLE);
        txtApprovedBy.setVisibility(View.VISIBLE);
        txtApprovedAt.setVisibility(View.VISIBLE);
        txtReason.setVisibility(View.VISIBLE);
    }

    private void loadApprovalDetail(String userId) {
        dbRef.child("Approval")
                .orderByChild("UserId")
                .equalTo(userId)
                .limitToLast(1) // lấy approval mới nhất
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {

                            String contentId = s.child("ContentId").getValue(String.class);
                            String reason = s.child("Reason").getValue(String.class);
                            String approvedAt = s.child("ApprovedAt").getValue(String.class);
                            String approverId = s.child("UserId").getValue(String.class);

                            txtContentId.setText("Content ID: " + (contentId != null ? contentId : "-"));
                            txtApprovedAt.setText("Thời gian duyệt: " + (approvedAt != null ? approvedAt : "-"));
                            txtReason.setText("Lý do: " + (reason != null ? reason : "-"));

                            if (approverId != null) {
                                loadApproverName(approverId);
                            }
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadApproverName(String userId) {
        dbRef.child("User").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fullName = snapshot.child("FullName").getValue(String.class);
                        txtApprovedBy.setText("Người duyệt: " + (fullName != null ? fullName : "-"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    /* ================= TASK ================= */

    private void showTaskUI() {
        ivIcon.setImageResource(R.drawable.ic_task);
        ivIcon.setColorFilter(getColor(R.color.colorSecondary));
        tvTitle.setText("Công việc");

        // Task không có thông tin duyệt
        txtContentId.setVisibility(View.GONE);
        txtApprovedBy.setVisibility(View.GONE);
        txtApprovedAt.setVisibility(View.GONE);
        txtReason.setVisibility(View.GONE);
    }

    /* ================= DEFAULT ================= */

    private void showDefaultUI() {
        ivIcon.setImageResource(R.drawable.ic_notification);
        tvTitle.setText("Thông báo");

        txtContentId.setVisibility(View.GONE);
        txtApprovedBy.setVisibility(View.GONE);
        txtApprovedAt.setVisibility(View.GONE);
        txtReason.setVisibility(View.GONE);
    }
}
