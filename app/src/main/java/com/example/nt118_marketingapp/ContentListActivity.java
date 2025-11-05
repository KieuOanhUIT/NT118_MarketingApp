package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
    private Button btnAddContent, btnContentCalendar; // 👉 thêm biến nút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        layoutContentTable = findViewById(R.id.layoutContentTable);
        contentRef = FirebaseDatabase.getInstance().getReference("Content");

        // 👉 ánh xạ nút "Thêm Content" và "Lịch Content"
        btnAddContent = findViewById(R.id.btnAddContent);
        btnContentCalendar = findViewById(R.id.btnContentCalendar);

        // 🎯 Xử lý nhấn nút "Thêm Content" → mở trang CreateContentActivity
        btnAddContent.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, CreateContentActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 🎯 Xử lý nhấn nút "Lịch Content" (nếu bạn có activity này)
        btnContentCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, ContentCalendarActivity.class);
            startActivity(intent);
        });

        // 🔁 Load danh sách Content
        loadContentList();
    }

    /** Load danh sách Content từ Firebase */
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

                    // ✅ Chỉ hiển thị 3 trạng thái: to do, in progress, done
                    if (!(status.equals("to do") || status.equals("in progress") || status.equals("done"))) {
                        continue;
                    }

                    String contentId = child.getKey();
                    View itemView = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);

                    // Ánh xạ view
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

                    // Hiển thị nội dung
                    tvTitle.setText(content.getTitle() != null ? content.getTitle() : "(Không có tiêu đề)");
                    tvType.setText("Loại: " + safe(content.getType()));
                    tvChannel.setText("Kênh: " + safe(content.getChannel()));
                    tvTag.setText("Thẻ: " + safe(content.getTag()));
                    tvCreatedTime.setText("Tạo lúc: " + safe(content.getCreatedTime()));
                    tvUrl.setText("URL: " + safe(content.getUrl()));
                    btnStatus.setText(safe(content.getStatus()));

                    // 🎨 Gán màu theo status
                    setStatusButtonStyle(btnStatus, status);

                    // ✅ Khi status là "done" thì khóa nút Edit
                    if (status.equals("done")) {
                        disableEditButton(btnEdit);
                    }

                    // Nút xem
                    btnView.setOnClickListener(v ->
                            Toast.makeText(ContentListActivity.this, "Xem: " + content.getTitle(), Toast.LENGTH_SHORT).show()
                    );

                    // Nút sửa
                    btnEdit.setOnClickListener(v -> {
                        if (!btnEdit.isEnabled()) return;
                        Intent intent = new Intent(ContentListActivity.this, EditContentActivity.class);
                        intent.putExtra("contentId", contentId);
                        startActivity(intent);
                    });

                    // Nút xóa
                    btnDelete.setOnClickListener(v -> {
                        contentRef.child(contentId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ContentListActivity.this, "Đã xóa nội dung!", Toast.LENGTH_SHORT).show();
                                    layoutContentTable.removeView(itemView);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(ContentListActivity.this, "Lỗi khi xóa nội dung!", Toast.LENGTH_SHORT).show()
                                );
                    });

                    // Nút trạng thái
                    btnStatus.setOnClickListener(v -> {
                        String currentStatus = safe(content.getStatus());
                        String nextStatus = getNextStatus(currentStatus);
                        btnStatus.setText(nextStatus);
                        setStatusButtonStyle(btnStatus, nextStatus.toLowerCase());

                        contentRef.child(contentId).child("status").setValue(nextStatus)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ContentListActivity.this, "Đã đổi sang " + nextStatus, Toast.LENGTH_SHORT).show();
                                    if (nextStatus.equalsIgnoreCase("done")) {
                                        disableEditButton(btnEdit);
                                    } else {
                                        enableEditButton(btnEdit);
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

    /** Xử lý null */
    private String safe(String value) {
        return value != null ? value : "-";
    }

    /** Chu kỳ status */
    private String getNextStatus(String current) {
        if (current == null) return "To do";
        switch (current.toLowerCase()) {
            case "to do":
                return "In progress";
            case "in progress":
                return "Done";
            case "done":
                return "To do";
            default:
                return "To do";
        }
    }

    /** 🎨 Gán màu cho nút trạng thái */
    private void setStatusButtonStyle(Button btn, String status) {
        int colorRes;
        switch (status.toLowerCase()) {
            case "to do":
                colorRes = R.color.deadlineOverdue;
                break;
            case "in progress":
                colorRes = R.color.deadlineWarning;
                break;
            case "done":
                colorRes = R.color.deadlineUpcoming;
                break;
            default:
                colorRes = R.color.deadlineOverdue;
        }

        btn.setBackgroundTintList(ContextCompat.getColorStateList(this, colorRes));
        btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    /** 🔒 Disable Edit */
    private void disableEditButton(ImageButton btn) {
        btn.setEnabled(false);
        btn.setAlpha(0.4f);
    }

    /** 🔓 Enable Edit */
    private void enableEditButton(ImageButton btn) {
        btn.setEnabled(true);
        btn.setAlpha(1f);
    }
}
