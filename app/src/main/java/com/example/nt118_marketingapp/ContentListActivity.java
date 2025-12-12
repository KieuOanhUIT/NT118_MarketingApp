package com.example.nt118_marketingapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.nt118_marketingapp.model.Content;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;
import java.util.*;

public class ContentListActivity extends AppCompatActivity {

    private LinearLayout layoutContentTable;
    private DatabaseReference contentRef;
    private Button btnAddContent, btnContentCalendar;
    private BottomNavigationView bottomNavigationView;
    private EditText etSearchContent;
    private Spinner spinnerStatusFilter;
    
    // User data
    private String userId, fullName, roleName, phone, email;

    private final List<Content> allContents = new ArrayList<>();
    private final List<String> allKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        // NHẬN ROLE TỪ INTENT (bắt buộc để ẩn tab)
        roleName = getIntent().getStringExtra("roleName");

        layoutContentTable = findViewById(R.id.layoutContentTable);
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        btnAddContent = findViewById(R.id.btnAddContent);
        btnContentCalendar = findViewById(R.id.btnContentCalendar);
        etSearchContent = findViewById(R.id.etSearchContent);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Nhận thông tin người dùng từ Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        fullName = intent.getStringExtra("fullName");
        roleName = intent.getStringExtra("roleName");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        setupStatusFilterSpinner();
        setupSearchListener();
        setupBottomNavigation(); // ĐÃ SỬA CHỖ NÀY

        btnAddContent.setOnClickListener(v -> {
            startActivity(new Intent(ContentListActivity.this, ContentManageActivity.class));
        });

        btnContentCalendar.setOnClickListener(v -> {
            startActivity(new Intent(ContentListActivity.this, ContentCalendarActivity.class));
        });

        loadContentList();
    }

    // ====================== GIỮ NGUYÊN TOÀN BỘ LOGIC CŨ ======================
    private void loadContentList() {
        contentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allContents.clear();
                allKeys.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Object value = child.getValue();
                    if (value instanceof Map) {
                        Content content = child.getValue(Content.class);
                        if (content != null) {
                            allContents.add(content);
                            allKeys.add(child.getKey());
                        }
                    } else {
                        System.out.println("Node không hợp lệ, bỏ qua: " + child.getKey() + " -> " + value);
                    }
                }
                filterAndDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContentListActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStatusFilterSpinner() {
        List<String> statuses = Arrays.asList("Tất cả", "To do", "In progress", "Done", "Approved", "Rejected", "Scheduled", "Published");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(adapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filterAndDisplay(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupSearchListener() {
        etSearchContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterAndDisplay(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterAndDisplay() {
        layoutContentTable.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        String keyword = etSearchContent.getText().toString().trim().toLowerCase();
        String selectedStatus = spinnerStatusFilter.getSelectedItem().toString().toLowerCase();

        for (int i = 0; i < allContents.size(); i++) {
            Content content = allContents.get(i);
            String key = allKeys.get(i);
            if (content == null) continue;

            boolean matchStatus = selectedStatus.equals("tất cả") ||
                    safe(content.getStatus()).toLowerCase().equals(selectedStatus);
            boolean matchKeyword =
                    safe(content.getTitle()).toLowerCase().contains(keyword) ||
                            safe(content.getChannel()).toLowerCase().contains(keyword) ||
                            safe(content.getTag()).toLowerCase().contains(keyword);

            if (!matchStatus || !matchKeyword) continue;

            View itemView = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);
            bindContentItem(itemView, content, key);
            layoutContentTable.addView(itemView);
        }

        if (layoutContentTable.getChildCount() == 0)
            Toast.makeText(this, "Không tìm thấy nội dung phù hợp!", Toast.LENGTH_SHORT).show();
    }

    private void bindContentItem(View itemView, Content content, String contentId) {
        TextView tvTitle = itemView.findViewById(R.id.tvTitle);
        TextView tvType = itemView.findViewById(R.id.tvType);
        TextView tvChannel = itemView.findViewById(R.id.tvChannel);
        TextView tvTag = itemView.findViewById(R.id.tvTag);
        TextView tvCreatedTime = itemView.findViewById(R.id.tvCreatedTime);
        TextView tvUrl = itemView.findViewById(R.id.tvUrl);
        Button btnStatus = itemView.findViewById(R.id.btnStatus);
        ImageButton btnView = itemView.findViewById(R.id.btnView);
        ImageButton btnEdit = itemView.findViewById(R.id.btnEdit);
        ImageButton btnDelete = itemView.findViewById(R.id.btnDelete);

        tvTitle.setText(safe(content.getTitle()));
        tvType.setText("Loại: " + safe(content.getType()));
        tvChannel.setText("Kênh: " + safe(content.getChannel()));
        tvTag.setText("Thẻ: " + safe(content.getTag()));
        tvCreatedTime.setText("Tạo lúc: " + safe(content.getCreatedTime()));
        tvUrl.setText("URL: " + safe(content.getUrl()));
        btnStatus.setText(safe(content.getStatus()));
        setStatusButtonStyle(btnStatus, safe(content.getStatus()).toLowerCase());

        String status = safe(content.getStatus()).toLowerCase();
        if (isLockedStatus(status)) {
            disableEditButton(btnEdit);
            btnStatus.setEnabled(false);
        }

        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, ContentManageActivity.class);
            intent.putExtra("CONTENT_ID", contentId);
            startActivity(intent);
        });

        btnEdit.setOnClickListener(v -> {
            if (!btnEdit.isEnabled()) return;
            Intent intent = new Intent(ContentListActivity.this, ContentManageActivity.class);
            intent.putExtra("CONTENT_ID", contentId);
            intent.putExtra("EDIT_MODE", true);  // Open directly in EDIT mode
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog(contentId, content.getTitle(), itemView));

        btnStatus.setOnClickListener(v -> {
            String current = safe(content.getStatus());
            String next = getNextStatus(current);
            if (next.equals(current)) return;

            Map<String, Object> updates = new HashMap<>();
            updates.put("Status", next);
            updates.put("ModifiedTime", new Date().toString());

            contentRef.child(contentId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        btnStatus.setText(next);
                        setStatusButtonStyle(btnStatus, next.toLowerCase());
                        Toast.makeText(this, "Đã đổi sang " + next, Toast.LENGTH_SHORT).show();

                        if (isLockedStatus(next.toLowerCase())) {
                            disableEditButton(btnEdit);
                            btnStatus.setEnabled(false);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show());
        });
    }

    private String safe(String s) { return s == null ? "" : s; }
    private boolean isLockedStatus(String s) {
        return s.equals("done") || s.equals("approved") || s.equals("scheduled") || s.equals("published");
    }
    private String getNextStatus(String current) {
        if (current == null) return "To do";
        switch (current.toLowerCase()) {
            case "to do": return "In progress";
            case "in progress": return "Done";
            default: return current;
        }
    }

    private void setStatusButtonStyle(Button btn, String status) {
        int colorRes;
        switch (status.toLowerCase()) {
            case "to do": colorRes = R.color.deadlineOverdue; break;
            case "in progress": colorRes = R.color.deadlineWarning; break;
            case "done": colorRes = R.color.deadlineUpcoming; break;
            case "approved": colorRes = R.color.inputBorder; break;
            case "rejected": colorRes = R.color.deadlineOverdue; break;
            case "scheduled": colorRes = R.color.inputBorder; break;
            case "published": colorRes = R.color.inputBorder; break;
            default: colorRes = R.color.inputBorder;
        }
        btn.setBackgroundTintList(ContextCompat.getColorStateList(this, colorRes));
        btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }
    private void disableEditButton(ImageButton btn) {
        btn.setEnabled(false);
        btn.setAlpha(0.4f);
    }
    private void showDeleteConfirmDialog(String contentId, String title, View itemView) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_confirm_delete_content);
        dialog.setCancelable(true);

        TextView tvContentName = dialog.findViewById(R.id.tvContentName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        tvContentName.setText(" " + title);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            contentRef.child(contentId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        layoutContentTable.removeView(itemView);
                        dialog.dismiss();
                        Toast.makeText(this, "Đã xóa nội dung!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi xóa nội dung!", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // ====================== CHỈ SỬA PHẦN NÀY: NAVIGATION + PHÂN QUYỀN ======================
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_contentmanagement);
        
        // Ẩn tab dành cho Admin nếu không phải Admin
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        // ẨN 2 TAB NẾU KHÔNG PHẢI ADMIN
        if (!"Admin".equalsIgnoreCase(roleName)) {
            bottomNavigationView.getMenu().findItem(R.id.navigation_usermanagement).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.navigation_approve).setVisible(false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_contentmanagement) {
                return true;
            } else if (id == R.id.navigation_approve) {
                Intent intent = new Intent(getApplicationContext(), ReviewContentActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_usermanagement) {
                Intent intent = new Intent(getApplicationContext(), UsermanagerActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_notification) {
                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                attachUserData(intent);
                startActivity(intent);
            } else if (id == R.id.navigation_profile) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                attachUserData(intent);
                startActivity(intent);
            }
            return true;
        });
    }
    
    // Helper method: attach user data to Intent
    private void attachUserData(Intent intent) {
        intent.putExtra("userId", userId);
        intent.putExtra("fullName", fullName);
        intent.putExtra("roleName", roleName);
        intent.putExtra("phone", phone);
        intent.putExtra("email", email);
    }
}
