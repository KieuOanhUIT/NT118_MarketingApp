package com.example.nt118_marketingapp;

import android.os.Bundle;
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

    private TextView tvTitle, tvMessage, tvTime, tvExtra;
    private ImageView ivIcon;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvTime = findViewById(R.id.tvTime);
        tvExtra = findViewById(R.id.tvExtra);
        ivIcon = findViewById(R.id.ivIcon);

        dbRef = FirebaseDatabase.getInstance().getReference();

        // Lấy dữ liệu cơ bản từ Intent
        String notificationId = getIntent().getStringExtra("notificationId");
        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        String time = getIntent().getStringExtra("time");
        int iconRes = getIntent().getIntExtra("icon", R.drawable.ic_task);

        tvTitle.setText(title);
        tvMessage.setText(message);
        tvTime.setText(time);
        ivIcon.setImageResource(iconRes);

        if (notificationId != null) {
            loadNotificationDetail(notificationId);
        }
    }

    private void loadNotificationDetail(String notificationId) {
        dbRef.child("Notification").child(notificationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot notiSnap) {
                        if (notiSnap.exists()) {
                            String type = notiSnap.child("Type").getValue(String.class);
                            String userId = notiSnap.child("UserId").getValue(String.class);
                            String contentId = notiSnap.child("ContentId").getValue(String.class);

                            if (contentId == null) {
                                tvExtra.setText("Không có thông tin Content liên kết.");
                                return;
                            }

                            // Lấy chi tiết bài viết
                            loadContentInfo(contentId, content -> {
                                // Nếu là duyệt hay từ chối, thì nối thêm info người duyệt + lý do
                                if ("Approval".equalsIgnoreCase(type) || "Rejection".equalsIgnoreCase(type)) {
                                    loadApprovalInfo(contentId, approval -> {
                                        loadUserInfo(approval.userId, approverName -> {
                                            String statusText = "Approval".equalsIgnoreCase(type)
                                                    ? "✅ Bài viết đã được duyệt"
                                                    : "❌ Bài viết bị từ chối";

                                            String detail = statusText +
                                                    "\n\n👤 Người duyệt: " + approverName +
                                                    "\n📝 Lý do: " + (approval.reason != null ? approval.reason : "Không có") +
                                                    "\n🕒 Thời gian xử lý: " + (approval.approvedAt != null ? approval.approvedAt : "N/A") +
                                                    "\n\n📄 Tiêu đề bài viết: " + content.title +
                                                    "\n📌 Trạng thái: " + content.status +
                                                    "\n⏰ Giờ đăng: " + (content.publishedTime.isEmpty() ? "Chưa đăng" : content.publishedTime);

                                            tvExtra.setText(detail);
                                        });
                                    });
                                } else {
                                    tvExtra.setText("📄 Tiêu đề: " + content.title +
                                            "\n📌 Trạng thái: " + content.status +
                                            "\n⏰ Giờ đăng: " + (content.publishedTime.isEmpty() ? "Chưa đăng" : content.publishedTime));
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvExtra.setText("Lỗi khi tải chi tiết thông báo.");
                    }
                });
    }

    // ----------------- LOAD APPROVAL INFO -------------------
    private static class ApprovalDetail {
        String userId, reason, approvedAt;

        ApprovalDetail(String userId, String reason, String approvedAt) {
            this.userId = userId;
            this.reason = reason;
            this.approvedAt = approvedAt;
        }
    }

    private interface ApprovalCallback {
        void onApprovalLoaded(ApprovalDetail approval);
    }

    private void loadApprovalInfo(String contentId, ApprovalCallback callback) {
        dbRef.child("Approval")
                .orderByChild("ContentId").equalTo(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String userId = child.child("UserId").getValue(String.class);
                            String reason = child.child("Reason").getValue(String.class);
                            String approvedAt = child.child("ApprovedAt").getValue(String.class);

                            callback.onApprovalLoaded(new ApprovalDetail(
                                    userId != null ? userId : "-",
                                    reason,
                                    approvedAt
                            ));
                            return; // chỉ cần lấy 1 bản ghi phù hợp
                        }
                        tvExtra.setText("Không tìm thấy dữ liệu phê duyệt cho nội dung này.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ----------------- LOAD USER INFO -------------------
    private interface UserCallback {
        void onUserLoaded(String name);
    }

    private void loadUserInfo(String userId, UserCallback callback) {
        dbRef.child("User").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                        if (userSnap.exists()) {
                            String fullName = userSnap.child("FullName").getValue(String.class);
                            callback.onUserLoaded(fullName != null ? fullName : "Không rõ");
                        } else {
                            callback.onUserLoaded("Không tìm thấy người duyệt");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ----------------- LOAD CONTENT INFO -------------------
    private static class ContentDetail {
        String title, status, publishedTime;

        ContentDetail(String title, String status, String publishedTime) {
            this.title = title;
            this.status = status;
            this.publishedTime = publishedTime;
        }
    }

    private interface ContentCallback {
        void onContentLoaded(ContentDetail content);
    }

    private void loadContentInfo(String contentId, ContentCallback callback) {
        dbRef.child("Content").child(contentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot contentSnap) {
                        if (contentSnap.exists()) {
                            String title = contentSnap.child("Title").getValue(String.class);
                            String status = contentSnap.child("Status").getValue(String.class);
                            String publishedTime = contentSnap.child("PublishedTime").getValue(String.class);

                            callback.onContentLoaded(new ContentDetail(
                                    title != null ? title : "Không rõ",
                                    status != null ? status : "Không rõ",
                                    publishedTime != null ? publishedTime : ""
                            ));
                        } else {
                            callback.onContentLoaded(new ContentDetail("Không tìm thấy bài viết", "-", ""));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
