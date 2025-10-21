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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ReviewContentActivity extends AppCompatActivity {

    private LinearLayout contentList;
    private Spinner spinnerFilter;

    static class ContentItem {
        String title;
        String description;
        String time;
        boolean isApproved;

        ContentItem(String title, String description, String time, boolean isApproved) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.isApproved = isApproved;
        }
    }

    private List<ContentItem> allContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_content);

        contentList = findViewById(R.id.contentList);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        generateSampleData();
        displayFilteredList("Tất cả");

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                displayFilteredList(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void generateSampleData() {
        allContents = new ArrayList<>();
        allContents.add(new ContentItem("Chiến dịch Black Friday", "Bài đăng Facebook ưu đãi giảm 50% toàn bộ sản phẩm.", "14:00 - 15/10/2025", false));
        allContents.add(new ContentItem("Bài Instagram Noel", "Nội dung video ngắn kèm hình ảnh quà tặng cuối năm.", "09:00 - 16/10/2025", true));
        allContents.add(new ContentItem("Bài TikTok khuyến mãi Flash Sale", "Clip quảng bá Flash Sale 11/11 sắp tới.", "11:30 - 16/10/2025", false));
    }

    private void displayFilteredList(String filter) {
        contentList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ContentItem item : allContents) {
            if (filter.equals("Cần duyệt") && item.isApproved) continue;
            if (filter.equals("Đã duyệt") && !item.isApproved) continue;

            View itemView = inflater.inflate(R.layout.item_content_review, contentList, false);

            TextView tvTitle = itemView.findViewById(R.id.tvContentTitle);
            TextView tvDesc = itemView.findViewById(R.id.tvContentDesc);
            TextView tvTime = itemView.findViewById(R.id.tvContentTime);
            Button btnApprove = itemView.findViewById(R.id.btnApprove);
            Button btnReject = itemView.findViewById(R.id.btnReject);

            tvTitle.setText(item.title);
            tvDesc.setText(item.description);
            tvTime.setText("Tạo lúc: " + item.time);

            if (item.isApproved) {
                btnApprove.setEnabled(false);
                btnReject.setEnabled(false);
                btnApprove.setAlpha(0.5f);
                btnReject.setAlpha(0.5f);
            }

            btnApprove.setOnClickListener(v -> {
                item.isApproved = true;
                Toast.makeText(this, "Đã duyệt: " + item.title, Toast.LENGTH_SHORT).show();

                // Cập nhật UI trước khi chuyển màn hình
                displayFilteredList(spinnerFilter.getSelectedItem().toString());

                Intent intent = new Intent(ReviewContentActivity.this, SchedulePostActivity.class);
                intent.putExtra("contentTitle", item.title);
                startActivity(intent);
            });

            btnReject.setOnClickListener(v -> showRejectPopup(item));

            contentList.addView(itemView);
        }
    }

    private void showRejectPopup(ContentItem item) {
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
                Toast.makeText(this, "Đã từ chối \"" + item.title + "\"\nLý do: " + reason, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
}
