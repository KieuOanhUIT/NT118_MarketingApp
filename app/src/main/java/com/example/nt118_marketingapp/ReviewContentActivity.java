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

    private DatabaseReference contentRef, approvalRef;
    private FirebaseAuth auth;

    private List<Content> allContents = new ArrayList<>();

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

    /** ================== Chỉ lấy Content có Status = "Done" ================== **/
    private void loadContentsFromFirebase() {
        contentRef.orderByChild("Status").equalTo("Done")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allContents.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Content c = child.getValue(Content.class);
                            if (c != null) {
                                // Lưu lại id Firebase (nếu cần)
                                try {
                                    java.lang.reflect.Field idField = Content.class.getDeclaredField("id");
                                    idField.setAccessible(true);
                                    idField.set(c, child.getKey());
                                } catch (Exception ignored) {}

                                allContents.add(c);
                            }
                        }
                        displayFilteredList(spinnerFilter.getSelectedItem().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ReviewContentActivity.this,
                                "Lỗi tải dữ liệu: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** ================== Hiển thị danh sách theo bộ lọc ================== **/
    private void displayFilteredList(String filter) {
        contentList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Content item : allContents) {
            boolean isDone = "Done".equalsIgnoreCase(item.getStatus());

            // Chỉ hiển thị các bài có Status = Done
            if (!isDone) continue;

            View itemView = inflater.inflate(R.layout.item_content_review, contentList, false);

            TextView tvTitle = itemView.findViewById(R.id.tvContentTitle);
            TextView tvDesc = itemView.findViewById(R.id.tvContentDesc);
            TextView tvTime = itemView.findViewById(R.id.tvContentTime);
            Button btnApprove = itemView.findViewById(R.id.btnApprove);
            Button btnReject = itemView.findViewById(R.id.btnReject);

            tvTitle.setText(item.getTitle());
            tvDesc.setText(item.getTag() + " - " + item.getChannel());
            tvTime.setText("Tạo lúc: " + item.getCreatedTime());

            btnApprove.setOnClickListener(v -> handleApproval(item, true, "Đạt yêu cầu"));
            btnReject.setOnClickListener(v -> showRejectPopup(item));

            contentList.addView(itemView);
        }
    }

    /** ================== Hàm duyệt / không duyệt ================== **/
    private void handleApproval(Content item, boolean approved, String reason) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";
        String approvalId = approvalRef.push().getKey();
        String time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        // Tạo đối tượng ApprovalModel để lưu vào Firebase
        ApprovalModel approval = new ApprovalModel(time, item.getUrl(), reason, userId);

        approvalRef.child(approvalId).setValue(approval)
                .addOnSuccessListener(aVoid -> {
                    if (approved) {
                        Toast.makeText(this, " Đã duyệt: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                        //  Chuyển sang màn hình SchedulePostActivity
                        Intent intent = new Intent(ReviewContentActivity.this, SchedulePostActivity.class);
                        intent.putExtra("contentTitle", item.getTitle());
                        intent.putExtra("contentUrl", item.getUrl());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Đã từ chối: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi lưu phê duyệt!", Toast.LENGTH_SHORT).show()
                );
    }

    /** ================== Popup nhập lý do từ chối ================== **/
    private void showRejectPopup(Content item) {
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
                handleApproval(item, false, reason);
                dialog.dismiss();
            }
        });
    }

    /** ==================  Bottom Navigation ================== **/
    private void setupBottomNav() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_approve);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_contentmanagement) {
                startActivity(new Intent(getApplicationContext(), ContentListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_usermanagement) {
                startActivity(new Intent(getApplicationContext(), UsermanagerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_notification) {
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(getApplicationContext(), Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    /** ==================  Model Approval ================== **/
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
}
