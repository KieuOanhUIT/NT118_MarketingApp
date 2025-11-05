package com.example.nt118_marketingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nt118_marketingapp.model.Content;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewContentActivity extends AppCompatActivity {

    private LinearLayout contentList;
    private Spinner spinnerFilter;
    private BottomNavigationView bottomNavigationView;

    private DatabaseReference contentRef, approvalRef, notificationRef;
    private FirebaseAuth auth;

    // Wrapper để giữ cả object Content và contentId (Firebase key)
    private static class ReviewItem {
        public final Content content;
        public final String contentId;
        public ReviewItem(Content content, String contentId) {
            this.content = content;
            this.contentId = contentId;
        }
    }

    // danh sách ReviewItem thay vì chỉ Content
    private List<ReviewItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_content);

        contentList = findViewById(R.id.contentList);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Firebase setup
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        approvalRef = FirebaseDatabase.getInstance().getReference("Approval");
        notificationRef = FirebaseDatabase.getInstance().getReference("Notification");
        auth = FirebaseAuth.getInstance();

        // Spinner filter setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                displayFilteredList(selected);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        setupBottomNav();
        loadContentsFromFirebase();
    }

    /** ================== Load Content có Status = "Done" ================== **/
    private void loadContentsFromFirebase() {
        // truy vấn các content có Status == "Done"
        contentRef.orderByChild("Status").equalTo("Done")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allItems.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Content c = child.getValue(Content.class);
                            if (c != null) {
                                String key = child.getKey(); // firebase key
                                allItems.add(new ReviewItem(c, key));
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

    /** ================== Hiển thị danh sách Content ================== **/
    private void displayFilteredList(String filter) {
        contentList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ReviewItem ri : allItems) {
            Content item = ri.content;

            // chỉ hiển thị content có status Done
            if (!"Done".equalsIgnoreCase(item.getStatus())) continue;

            View itemView = inflater.inflate(R.layout.item_content_review, contentList, false);

            // ánh xạ ID đúng với XML mới
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            TextView tvStatus = itemView.findViewById(R.id.tvStatus);
            Button btnApprove = itemView.findViewById(R.id.btnApprove);
            Button btnReject  = itemView.findViewById(R.id.btnReject);

            // gán dữ liệu
            tvTitle.setText(item.getTitle() != null ? item.getTitle() : "(Không có tiêu đề)");
            tvStatus.setText("Trạng thái: " + (item.getStatus() != null ? item.getStatus() : "Chưa xác định"));

            // Xử lý nút Duyệt
            btnApprove.setOnClickListener(v -> showApprovePopup(ri));

            // Xử lý nút Không duyệt
            btnReject.setOnClickListener(v -> showRejectPopup(ri));

            // thêm view vào layout cha
            contentList.addView(itemView);
        }

        // Nếu danh sách rỗng
        if (contentList.getChildCount() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Không có nội dung chờ duyệt.");
            emptyView.setTextSize(16);
            emptyView.setPadding(24, 32, 24, 32);
            emptyView.setTextColor(getResources().getColor(R.color.textSecondary));
            contentList.addView(emptyView);
        }
    }

    /** ================== Show popup để nhập link khi duyệt ================== **/
    private void showApprovePopup(ReviewItem ri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.dialog_approve, null); // bạn cần tạo dialog_approve.xml (edtUrl, btnCancel, btnConfirm)
        builder.setView(popupView);

        EditText edtUrl = popupView.findViewById(R.id.edtUrl);
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnConfirm = popupView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            // optional: validate url
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập link bài đăng!", Toast.LENGTH_SHORT).show();
                return;
            }
            // cập nhật Url và chuyển trạng thái: Approved -> Scheduled (theo flow bạn muốn)
            handleApproval(ri, true, "Đạt yêu cầu", url);
            dialog.dismiss();
        });
    }

    /** ================== Duyệt hoặc Không Duyệt ==================
     *  updated: accept ReviewItem and optional url
     **/
    private void handleApproval(ReviewItem ri, boolean approved, String reason, String scheduledUrl) {
        String reviewerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";
        String approvalId = approvalRef.push().getKey();
        String notiId = notificationRef.push().getKey();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Lưu Approval
        ApprovalModel approval = new ApprovalModel(time, ri.contentId, reason, reviewerId);
        if (approvalId != null) approvalRef.child(approvalId).setValue(approval);

        // Cập nhật trạng thái Content
        String newStatus = approved ? "Approved" : "Rejected";
        if (ri.contentId != null && !ri.contentId.isEmpty()) {
            contentRef.child(ri.contentId).child("Status").setValue(newStatus);
            if (approved && scheduledUrl != null && !scheduledUrl.isEmpty()) {
                contentRef.child(ri.contentId).child("Url").setValue(scheduledUrl);
            }
        }

        // Gửi Notification
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

        // Thông báo kết quả
        if (approved) {
            Toast.makeText(this, "✅ Đã duyệt bài: " + ri.content.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Đã từ chối: " + ri.content.getTitle(), Toast.LENGTH_SHORT).show();
        }

        // Refresh danh sách
        displayFilteredList(spinnerFilter.getSelectedItem() != null ? spinnerFilter.getSelectedItem().toString() : "Tất cả");
    }

    /** overload dùng cho từ chối (không cần url) */
    private void handleApproval(ReviewItem ri, boolean approved, String reason) {
        handleApproval(ri, approved, reason, null);
    }

    /** ================== Popup nhập lý do từ chối ================== **/
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

    /** ================== Bottom Navigation ================== **/
    private void setupBottomNav() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_approve);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_contentmanagement) {
                startActivity(new Intent(this, ContentListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_usermanagement) {
                startActivity(new Intent(this, UsermanagerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_notification) {
                startActivity(new Intent(this, NotificationActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
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
