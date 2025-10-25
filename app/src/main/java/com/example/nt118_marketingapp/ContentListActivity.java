package com.example.nt118_marketingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContentListActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter;
    private LinearLayout layoutContentTable;

    private List<ContentItem> allContents;
    private BottomNavigationView bottomNavigationView;


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
        String status;  // "To do", "In progress", "Done"
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
        displayFilteredContent("", "All");

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

        // Xử lý nút Thêm Content
        Button btnAddContent = findViewById(R.id.btnAddContent);
        btnAddContent.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, CreateContentActivity.class);
            startActivity(intent);
        });

        // Xử lý nút Content Calendar
        Button btnContentCalendar = findViewById(R.id.btnContentCalendar);
        btnContentCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ContentListActivity.this, ContentCalendarActivity.class);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_contentmanagement);

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

    // ----------------------------
    // Tạo dữ liệu mẫu
    // ----------------------------
    private void generateSampleData() {
        allContents = new ArrayList<>();
        allContents.add(new ContentItem(1, "Black Friday Sale", "Giảm 50% toàn bộ sản phẩm", "2025-10-14 14:00", "Facebook", "Quyên", "To do", "https://fb.com/post1"));
        allContents.add(new ContentItem(2, "Noel Campaign", "Video quà tặng cuối năm", "2025-10-15 09:00", "Instagram", "Duy", "In progress", "https://ig.com/post2"));
        allContents.add(new ContentItem(3, "Flash Sale 11.11", "Bài viết Flash Sale cực hot", "2025-10-16 11:30", "TikTok", "Lan", "Done", "https://tiktok.com/post3"));
        allContents.add(new ContentItem(4, "Summer Giveaway", "Mini game trúng thưởng mùa hè", "2025-10-17 08:45", "Facebook", "Hà", "To do", "https://fb.com/post4"));
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
            if (!statusFilter.equals("All") && !item.status.equalsIgnoreCase(statusFilter)) continue;

            View row = inflater.inflate(R.layout.item_content_row, layoutContentTable, false);

            ((TextView) row.findViewById(R.id.tvId)).setText("ID: #" + item.id);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(item.title);
            ((TextView) row.findViewById(R.id.tvCaption)).setText(item.caption);
            ((TextView) row.findViewById(R.id.tvTime)).setText("Time: " + item.timestamp);
            ((TextView) row.findViewById(R.id.tvChannel)).setText("Channel: " + item.channel);
            ((TextView) row.findViewById(R.id.tvAuthor)).setText("Author: " + item.author);
            ((TextView) row.findViewById(R.id.tvLink)).setText("Link: " + item.link);

            // View button
            ImageButton btnView = row.findViewById(R.id.btnView);
            btnView.setOnClickListener(v -> {
                // Navigate to EditContentActivity in VIEW mode
                Intent intent = new Intent(this, EditContentActivity.class);
                intent.putExtra("CONTENT_ID", String.valueOf(item.id));
                intent.putExtra("EDIT_MODE", false); // Chế độ xem (không sửa)
                intent.putExtra("TITLE", item.title);
                intent.putExtra("CAPTION", item.caption);
                intent.putExtra("CHANNEL", item.channel);
                intent.putExtra("STATUS", item.status);
                intent.putExtra("LINK", item.link);
                intent.putExtra("TIMESTAMP", item.timestamp);
                intent.putExtra("AUTHOR", item.author);
                startActivity(intent);
            });

            // Edit button
            ImageButton btnEdit = row.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(v -> {
                // Navigate to EditContentActivity with edit mode enabled
                Intent intent = new Intent(this, EditContentActivity.class);
                intent.putExtra("CONTENT_ID", String.valueOf(item.id));
                intent.putExtra("EDIT_MODE", true); // Bật sẵn chế độ sửa
                intent.putExtra("TITLE", item.title);
                intent.putExtra("CAPTION", item.caption);
                intent.putExtra("CHANNEL", item.channel);
                intent.putExtra("STATUS", item.status);
                intent.putExtra("LINK", item.link);
                intent.putExtra("TIMESTAMP", item.timestamp);
                intent.putExtra("AUTHOR", item.author);
                startActivity(intent);
            });

            // Delete button
            ImageButton btnDelete = row.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(v -> {
                showDeleteConfirmationPopup(item, query, statusFilter);
            });

            // Spinner chuyển trạng thái
            Spinner spinnerChangeStatus = row.findViewById(R.id.spinnerChangeStatus);
            
            // Tạo adapter cho spinner với các trạng thái
            ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.content_status_options,
                    android.R.layout.simple_spinner_item
            );
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChangeStatus.setAdapter(statusAdapter);
            
            // Set trạng thái hiện tại
            String[] statusOptions = getResources().getStringArray(R.array.content_status_options);
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equalsIgnoreCase(item.status)) {
                    spinnerChangeStatus.setSelection(i);
                    break;
                }
            }
            
            // Xử lý khi thay đổi trạng thái
            spinnerChangeStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = parent.getItemAtPosition(position).toString();
                    if (!newStatus.equalsIgnoreCase(item.status)) {
                        item.status = newStatus;
                        Toast.makeText(ContentListActivity.this, 
                            "🔄 Đã chuyển trạng thái: " + item.title + " → " + newStatus, 
                            Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });

            layoutContentTable.addView(row);
        }
    }

    /**
     * Hiển thị popup xác nhận xóa content
     */
    private void showDeleteConfirmationPopup(ContentItem item, String query, String statusFilter) {
        // Inflate popup layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_confirm_delete_content, null);

        // Create popup as overlay
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        FrameLayout popupContainer = new FrameLayout(this);
        popupContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        popupContainer.addView(popupView);
        rootView.addView(popupContainer);

        // Set content name (optional)
        TextView tvContentName = popupView.findViewById(R.id.tvContentName);
        tvContentName.setText(item.title);
        tvContentName.setVisibility(View.VISIBLE);

        // Cancel button
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            rootView.removeView(popupContainer);
        });

        // Delete button
        Button btnDeleteConfirm = popupView.findViewById(R.id.btnDelete);
        btnDeleteConfirm.setOnClickListener(v -> {
            // Remove item from list
            allContents.remove(item);
            
            // Show toast
            Toast.makeText(this, "✅ Đã xóa: " + item.title, Toast.LENGTH_SHORT).show();
            
            // Close popup
            rootView.removeView(popupContainer);
            
            // Refresh list
            displayFilteredContent(query, statusFilter);
        });

        // Close popup when clicking overlay
        View popupOverlay = popupView.findViewById(R.id.popupOverlay);
        popupOverlay.setOnClickListener(v -> {
            rootView.removeView(popupContainer);
        });
    }
}
