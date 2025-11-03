package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nt118_marketingapp.model.Content;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContentListActivity extends AppCompatActivity {

    private LinearLayout layoutContentTable;
    private DatabaseReference contentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        layoutContentTable = findViewById(R.id.layoutContentTable);
        contentRef = FirebaseDatabase.getInstance().getReference("Content");

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

                    String contentId = child.getKey();
                    View itemView = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);

                    TextView tvTitle = itemView.findViewById(R.id.tvTitle);
                    TextView tvType = itemView.findViewById(R.id.tvType);
                    TextView tvChannel = itemView.findViewById(R.id.tvChannel);
                    TextView tvTag = itemView.findViewById(R.id.tvTag);
                    TextView tvCreatedTime = itemView.findViewById(R.id.tvCreatedTime);
                    TextView tvUrl = itemView.findViewById(R.id.tvUrl);
                    Button btnStatus = itemView.findViewById(R.id.btnStatus);

                    tvTitle.setText(content.getTitle() != null ? content.getTitle() : "(Không có tiêu đề)");
                    tvType.setText("Loại: " + safe(content.getType()));
                    tvChannel.setText("Kênh: " + safe(content.getChannel()));
                    tvTag.setText("Thẻ: " + safe(content.getTag()));
                    tvCreatedTime.setText("Tạo lúc: " + safe(content.getCreatedTime()));
                    tvUrl.setText("URL: " + safe(content.getUrl()));
                    btnStatus.setText(safe(content.getStatus()));

                    // 🔘 Xử lý đổi trạng thái khi nhấn nút
                    btnStatus.setOnClickListener(v -> {
                        String currentStatus = safe(content.getStatus());
                        String nextStatus = getNextStatus(currentStatus);

                        // Cập nhật UI
                        btnStatus.setText(nextStatus);

                        // Cập nhật Firebase
                        contentRef.child(contentId).child("status").setValue(nextStatus)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(ContentListActivity.this, "Đã đổi trạng thái thành " + nextStatus, Toast.LENGTH_SHORT).show()
                                )
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

    private String safe(String value) {
        return value != null ? value : "-";
    }

    // 🔁 Hàm chuyển trạng thái tuần tự
    private String getNextStatus(String current) {
        switch (current.toLowerCase()) {
            case "pending":
                return "Approved";
            case "approved":
                return "Done";
            case "done":
                return "Pending";
            default:
                return "Pending";
        }
    }
}
