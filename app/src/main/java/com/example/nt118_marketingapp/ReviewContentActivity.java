package com.example.nt118_marketingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nt118_marketingapp.model.Content;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReviewContentActivity extends AppCompatActivity {

    private LinearLayout contentList;
    private Spinner spinnerFilter;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference contentRef, approvalRef, notificationRef;
    private FirebaseAuth auth;

    // THÊM: để phân quyền
    private String roleName;

    private static class ReviewItem {
        public final Content content;
        public final String contentId;
        public ReviewItem(Content content, String contentId) {
            this.content = content;
            this.contentId = contentId;
        }
    }

    private List<ReviewItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_content);

        // NHẬN ROLE TỪ INTENT
        roleName = getIntent().getStringExtra("roleName");

        contentList = findViewById(R.id.contentList);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        approvalRef = FirebaseDatabase.getInstance().getReference("Approval");
        notificationRef = FirebaseDatabase.getInstance().getReference("Notification");
        auth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                displayFilteredList(selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupBottomNavigation(); // ĐÃ SỬA CHỖ NÀY
        loadContentsFromFirebase();
    }

    /** ================== Load Content ================== **/
    private void loadContentsFromFirebase() {
        contentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Object value = child.getValue();
                    if (value instanceof Map) { // chỉ convert object JSON
                        Content c = child.getValue(Content.class);
                        if (c != null && child.getKey() != null) {
                            boolean add = allItems.add(new ReviewItem(c, child.getKey()));
                        }
                    }
                }
                // hiển thị theo filter hiện tại
                String selected = spinnerFilter.getSelectedItem() != null
                        ? spinnerFilter.getSelectedItem().toString()
                        : "Tất cả";
                displayFilteredList(selected);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReviewContentActivity.this,
                        "Lỗi tải dữ liệu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ================== Hiển thị danh sách Content theo filter ================== **/
    private void displayFilteredList(String filter) {
        contentList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        boolean isEmpty = true;

        for (ReviewItem ri : allItems) {
            Content item = ri.content;
            if (item == null || item.getStatus() == null) continue;

            String status = item.getStatus();

            // Filter
            if ("Cần duyệt".equalsIgnoreCase(filter) && !"Done".equalsIgnoreCase(status)) continue;
            if ("Đã duyệt".equalsIgnoreCase(filter) && !"Approved".equalsIgnoreCase(status)) continue;

            View itemView = inflater.inflate(R.layout.item_content_review, contentList, false);

            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            TextView tvStatus = itemView.findViewById(R.id.tvStatus);
            Button btnApprove = itemView.findViewById(R.id.btnApprove);
            Button btnReject  = itemView.findViewById(R.id.btnReject);

            tvTitle.setText(item.getTitle() != null ? item.getTitle() : "(Không có tiêu đề)");
            tvStatus.setText("Trạng thái: " + status);

            // Nếu content đã Approved, disable nút duyệt/từ chối
            if ("Approved".equalsIgnoreCase(status)) {
                btnApprove.setEnabled(false);
                btnApprove.setAlpha(0.5f);
                btnReject.setEnabled(false);
                btnReject.setAlpha(0.5f);
            } else {
                btnApprove.setOnClickListener(v -> showApprovePopup(ri));
                btnReject.setOnClickListener(v -> showRejectPopup(ri));
            }

            contentList.addView(itemView);
            isEmpty = false;
        }

        if (isEmpty) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Không có nội dung phù hợp.");
            emptyView.setTextSize(16);
            emptyView.setPadding(24, 32, 24, 32);
            emptyView.setTextColor(getResources().getColor(R.color.textSecondary));
            contentList.addView(emptyView);
        }
    }

    /** ================== Popup duyệt bài ================== **/
    private void showApprovePopup(ReviewItem ri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.dialog_approve, null);
        builder.setView(popupView);

        EditText edtUrl = popupView.findViewById(R.id.edtUrl);
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnConfirm = popupView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập link bài đăng!", Toast.LENGTH_SHORT).show();
                return;
            }
            handleApproval(ri, true, "Đạt yêu cầu", url);
            dialog.dismiss();
        });
    }

    /** ================== Popup từ chối bài ================== **/
    private void showRejectPopup(ReviewItem ri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.popup_reject_reason, null);
        builder.setView(popupView);

        EditText etReason = popupView.findViewById(R.id.etRejectReason);
        Button btnCancel = popupView.findViewById(R.id.btnCancelReject);
        Button btnConfirm = popupView.findViewById(R.id.btnConfirmReject);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do!", Toast.LENGTH_SHORT).show();
            } else {
                handleApproval(ri, false, reason);
                dialog.dismiss();
            }
        });
    }

    /** ================== Xử lý duyệt hoặc từ chối ================== **/
    private void handleApproval(ReviewItem ri, boolean approved, String reason, String scheduledUrl) {
        String reviewerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";
        String approvalId = approvalRef.push().getKey();
        String notiId = notificationRef.push().getKey();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Lưu Approval
        ApprovalModel approval = new ApprovalModel(time, ri.contentId, reason, reviewerId);
        if (approvalId != null) approvalRef.child(approvalId).setValue(approval);

        // Cập nhật status content
        String newStatus = approved ? "Approved" : "Rejected";
        if (ri.contentId != null && !ri.contentId.isEmpty()) {
            contentRef.child(ri.contentId).child("Status").setValue(newStatus);
            if (approved && scheduledUrl != null && !scheduledUrl.isEmpty()) {
                contentRef.child(ri.contentId).child("Url").setValue(scheduledUrl);
            }
        }

        // Gửi notification
        String message = approved
                ? "🎉 Bài viết \"" + ri.content.getTitle() + "\" đã được duyệt."
                : "❌ Bài viết \"" + ri.content.getTitle() + "\" bị từ chối. Lý do: " + reason;

        NotificationModel noti = new NotificationModel(
                ri.content.getUserId() != null ? ri.content.getUserId() : "unknown",
                approved ? "Approval" : "Rejection",
                message,
                false,
                time
        );
        if (notiId != null) notificationRef.child(notiId).setValue(noti);

        Toast.makeText(this, approved ? "✅ Đã duyệt bài: " + ri.content.getTitle()
                        : "❌ Đã từ chối: " + ri.content.getTitle(),
                Toast.LENGTH_SHORT).show();

        // Refresh danh sách
        displayFilteredList(spinnerFilter.getSelectedItem() != null
                ? spinnerFilter.getSelectedItem().toString()
                : "Tất cả");
    }

    /** Overload handleApproval cho từ chối không cần URL */
    private void handleApproval(ReviewItem ri, boolean approved, String reason) {
        handleApproval(ri, approved, reason, null);
    }

    /** ================== Bottom Navigation ================== **/
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_approve);

        // ẨN 2 TAB NẾU KHÔNG PHẢI ADMIN
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(this, DashboardActivity.class);
            } else if (itemId == R.id.navigation_contentmanagement) {
                intent = new Intent(this, ContentListActivity.class);
            } else if (itemId == R.id.navigation_approve) {
                return true; // đang ở đây
            } else if (itemId == R.id.navigation_usermanagement) {
                intent = new Intent(this, UsermanagerActivity.class);
            } else if (itemId == R.id.navigation_notification) {
                intent = new Intent(this, NotificationActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(this, Profile.class);
            }

            if (intent != null) {
                intent.putExtra("roleName", roleName); // truyền tiếp để trang khác cũng ẩn tab
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    /** ================== Model: Approval ================== **/
    public static class ApprovalModel {
        public String ApprovedAt;
        public String ContentId;
        public String Reason;
        public String UserId;

        public ApprovalModel() {}
        public ApprovalModel(String approvedAt, String contentId, String reason, String userId) {
            this.ApprovedAt = approvedAt;
            this.ContentId = contentId;
            this.Reason = reason;
            this.UserId = userId;
        }
    }

    /** ================== Model: Notification ================== **/
    public static class NotificationModel {
        public String UserId;
        public String Type;
        public String Message;
        public boolean IsRead;
        public String CreatedTime;

        public NotificationModel() {}
        public NotificationModel(String userId, String type, String message, boolean isRead, String createdTime) {
            this.UserId = userId;
            this.Type = type;
            this.Message = message;
            this.IsRead = isRead;
            this.CreatedTime = createdTime;
        }
    }
}
