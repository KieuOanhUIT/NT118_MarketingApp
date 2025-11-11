package com.example.nt118_marketingapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nt118_marketingapp.model.Content;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContentListActivity extends AppCompatActivity {

    private LinearLayout layoutContentTable;
    private DatabaseReference contentRef;
    private Button btnAddContent, btnContentCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        layoutContentTable = findViewById(R.id.layoutContentTable);
        contentRef = FirebaseDatabase.getInstance().getReference("Content");

        btnAddContent = findViewById(R.id.btnAddContent);
        btnContentCalendar = findViewById(R.id.btnContentCalendar);

        btnAddContent.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, CreateContentActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnContentCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, ContentCalendarActivity.class);
            startActivity(intent);
        });

        loadContentList();
    }

    private void loadContentList() {
        contentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutContentTable.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(ContentListActivity.this);

                if (!snapshot.exists()) {
                    Toast.makeText(ContentListActivity.this, "Không có dữ liệu Content!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    Content content = child.getValue(Content.class);
                    if (content == null) continue;

                    String status = safe(content.getStatus()).toLowerCase();
                    String contentId = child.getKey();

                    View itemView = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);

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

                    // Gán dữ liệu
                    tvTitle.setText(safe(content.getTitle()));
                    tvType.setText("Loại: " + safe(content.getType()));
                    tvChannel.setText("Kênh: " + safe(content.getChannel()));
                    tvTag.setText("Thẻ: " + safe(content.getTag()));
                    tvCreatedTime.setText("Tạo lúc: " + safe(content.getCreatedTime()));
                    tvUrl.setText("URL: " + safe(content.getUrl()));
                    btnStatus.setText(safe(content.getStatus()));
                    setStatusButtonStyle(btnStatus, status);

                    // ⚙️ Các trạng thái DONE / APPROVED / REJECTED / SCHEDULED / PUBLISHED => không chỉnh sửa được
                    if (isLockedStatus(status)) {
                        disableEditButton(btnEdit);
                        btnStatus.setEnabled(false);
                    }

                    // Xem chi tiết - Mở EditContentActivity ở chế độ XEM (không cho phép chỉnh sửa)
                    btnView.setOnClickListener(v -> {
                        Intent intent = new Intent(ContentListActivity.this, EditContentActivity.class);
                        intent.putExtra("CONTENT_ID", contentId);
                        intent.putExtra("TITLE", content.getTitle());
                        intent.putExtra("CHANNEL", content.getChannel());
                        intent.putExtra("STATUS", content.getStatus());
                        intent.putExtra("LINK", content.getUrl());
                        intent.putExtra("TIMESTAMP", content.getCreatedTime());
                        intent.putExtra("CAPTION", content.getTag());
                        intent.putExtra("EDIT_MODE", false); // Chế độ XEM
                        startActivity(intent);
                    });

                    // Chỉnh sửa - Mở EditContentActivity ở chế độ CHỈNH SỬA
                    btnEdit.setOnClickListener(v -> {
                        if (!btnEdit.isEnabled()) return;
                        Intent intent = new Intent(ContentListActivity.this, EditContentActivity.class);
                        intent.putExtra("CONTENT_ID", contentId);
                        intent.putExtra("TITLE", content.getTitle());
                        intent.putExtra("CHANNEL", content.getChannel());
                        intent.putExtra("STATUS", content.getStatus());
                        intent.putExtra("LINK", content.getUrl());
                        intent.putExtra("TIMESTAMP", content.getCreatedTime());
                        intent.putExtra("CAPTION", content.getTag());
                        intent.putExtra("EDIT_MODE", true); // Chế độ CHỈNH SỬA
                        startActivity(intent);
                    });

                    // Xóa (hiện popup xác nhận)
                    btnDelete.setOnClickListener(v -> showDeleteConfirmDialog(contentId, content.getTitle(), itemView));

                    // Đổi trạng thái
                    btnStatus.setOnClickListener(v -> {
                        String current = safe(content.getStatus());
                        String next = getNextStatus(current);

                        // Cập nhật trạng thái trong Firebase
                        contentRef.child(contentId).child("status").setValue(next)
                                .addOnSuccessListener(aVoid -> {
                                    btnStatus.setText(next);
                                    setStatusButtonStyle(btnStatus, next.toLowerCase());
                                    Toast.makeText(ContentListActivity.this, "Đã đổi sang " + next, Toast.LENGTH_SHORT).show();

                                    if (isLockedStatus(next.toLowerCase())) {
                                        disableEditButton(btnEdit);
                                        btnStatus.setEnabled(false);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(ContentListActivity.this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show()
                                );
                    });

                    layoutContentTable.addView(itemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContentListActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ==============================
     * 🔹 Hàm tiện ích
     * ============================== */

    private String safe(String value) {
        return value != null ? value : "-";
    }

    private boolean isLockedStatus(String status) {
        return status.equals("done") ||
                status.equals("approved") ||
                status.equals("scheduled") ||
                status.equals("published");
    }

    private String getNextStatus(String current) {
        if (current == null) return "To do";
        switch (current.toLowerCase()) {
            case "to do": return "In progress";
            case "in progress": return "Done";
            default: return current; // Không đổi với các trạng thái locked
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

    /** ✅ Hiển thị popup xác nhận xóa */
    private void showDeleteConfirmDialog(String contentId, String title, View itemView) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_confirm_delete_content);
        dialog.setCancelable(true);

        TextView tvContentName = dialog.findViewById(R.id.tvContentName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        tvContentName.setVisibility(View.VISIBLE);
        tvContentName.setText("🗂 " + title);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            contentRef.child(contentId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ContentListActivity.this, "Đã xóa nội dung!", Toast.LENGTH_SHORT).show();
                        layoutContentTable.removeView(itemView);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ContentListActivity.this, "Lỗi khi xóa nội dung!", Toast.LENGTH_SHORT).show()
                    );
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

    }
}
