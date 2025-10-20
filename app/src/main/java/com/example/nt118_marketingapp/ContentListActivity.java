package com.example.nt118_marketingapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Locale;

public class ContentListActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter;
    private LinearLayout layoutContentTable;

    private List<ContentItem> allContents;

    // ----------------------------
    // Mô tả 1 content
    // ----------------------------
    static class ContentItem {
        int id;
        String title;
        String caption;
        String timestamp;
        String channel;
        String author;
        String status;
        String link;

        ContentItem(int id, String title, String caption, String timestamp,
                    String channel, String author, String status, String link) {
            this.id = id;
            this.title = title;
            this.caption = caption;
            this.timestamp = timestamp;
            this.channel = channel;
            this.author = author;
            this.status = status;
            this.link = link;
        }
    }

    // ----------------------------
    // Khởi tạo
    // ----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        etSearch = findViewById(R.id.etSearchContent);
        spinnerFilter = findViewById(R.id.spinnerStatusFilter);
        layoutContentTable = findViewById(R.id.layoutContentTable);

        // Gán danh sách filter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.content_status_filter,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        generateSampleData();

        // Hiển thị ban đầu
        displayFilteredContent("", "Tất cả");

        // Lọc theo search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayFilteredContent(s.toString(), spinnerFilter.getSelectedItem().toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Lọc theo spinner
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                displayFilteredContent(etSearch.getText().toString(), parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    // ----------------------------
    // Tạo dữ liệu mẫu
    // ----------------------------
    private void generateSampleData() {
        allContents = new ArrayList<>();
        allContents.add(new ContentItem(1, "Black Friday Sale", "Giảm 50% toàn bộ sản phẩm", "2025-10-14 14:00", "Facebook", "Quyên", "Chờ duyệt", "https://fb.com/post1"));
        allContents.add(new ContentItem(2, "Noel Campaign", "Video quà tặng cuối năm", "2025-10-15 09:00", "Instagram", "Duy", "Đã duyệt", "https://ig.com/post2"));
        allContents.add(new ContentItem(3, "Flash Sale 11.11", "Bài viết Flash Sale cực hot", "2025-10-16 11:30", "TikTok", "Lan", "Đã đăng", "https://tiktok.com/post3"));
        allContents.add(new ContentItem(4, "Summer Giveaway", "Mini game trúng thưởng mùa hè", "2025-10-17 08:45", "Facebook", "Hà", "Chờ duyệt", "https://fb.com/post4"));
    }

    // ----------------------------
    // Hiển thị danh sách sau khi lọc
    // ----------------------------
    private void displayFilteredContent(String query, String statusFilter) {
        layoutContentTable.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        String keyword = query.toLowerCase(Locale.ROOT).trim();

        for (ContentItem item : allContents) {
            // Lọc theo keyword và status
            if (!keyword.isEmpty() && !item.title.toLowerCase(Locale.ROOT).contains(keyword) &&
                    !item.caption.toLowerCase(Locale.ROOT).contains(keyword)) continue;
            if (!statusFilter.equals("Tất cả") && !item.status.equalsIgnoreCase(statusFilter)) continue;

            View row = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);

            ((TextView) row.findViewById(R.id.tvId)).setText(String.valueOf(item.id));
            ((TextView) row.findViewById(R.id.tvTitle)).setText(item.title);
            ((TextView) row.findViewById(R.id.tvCaption)).setText(item.caption);
            ((TextView) row.findViewById(R.id.tvTime)).setText(item.timestamp);
            ((TextView) row.findViewById(R.id.tvChannel)).setText(item.channel);
            ((TextView) row.findViewById(R.id.tvAuthor)).setText(item.author);
            ((TextView) row.findViewById(R.id.tvStatus)).setText(item.status);
            ((TextView) row.findViewById(R.id.tvLink)).setText(item.link);

            Button btnChange = row.findViewById(R.id.btnChangeStatus);
            btnChange.setOnClickListener(v -> {
                // Chuyển trạng thái demo
                if (item.status.equals("Chờ duyệt")) item.status = "Đã duyệt";
                else if (item.status.equals("Đã duyệt")) item.status = "Đã đăng";
                else item.status = "Chờ duyệt";

                Toast.makeText(this, "🔄 Đổi trạng thái: " + item.title + " → " + item.status, Toast.LENGTH_SHORT).show();
                displayFilteredContent(query, statusFilter);
            });

            layoutContentTable.addView(row);
        }
    }
}