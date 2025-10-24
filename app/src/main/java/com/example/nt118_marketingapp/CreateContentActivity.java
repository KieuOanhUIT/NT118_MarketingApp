package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * CreateContentActivity - Màn hình tạo mới content
 * 
 * Chức năng:
 * - Nhập thông tin content mới
 * - Quản lý subtasks: thêm, sửa, xóa
 * - Validate và lưu content
 */
public class CreateContentActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private Button btnSubmit;
    private Button btnAddSubtask;
    private LinearLayout subtasksContainer;
    
    // Input fields
    private EditText editTitle;
    private Spinner spinnerType;
    private EditText editChannel;
    private EditText editTags;
    private EditText editTime;
    private Spinner spinnerStatus;
    private EditText editAttachment;
    private EditText editEditorLink;
    
    // State variables
    private Calendar selectedDateTime;
    
    // Draft subtask tracking
    private View currentDraftSubtask = null;
    private boolean isDraftSubtaskValid = false;
    private List<SubtaskData> savedSubtasks = new ArrayList<>();
    
    // Subtask data class
    private static class SubtaskData {
        String title;
        String assignee;
        String deadline;
        
        SubtaskData(String title, String assignee, String deadline) {
            this.title = title;
            this.assignee = assignee;
            this.deadline = deadline;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);
        
        // Initialize views
        initViews();
        
        // Set up event listeners
        setupListeners();
    }
    
    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnAddSubtask = findViewById(R.id.btnAddSubtask);
        subtasksContainer = findViewById(R.id.subtasksContainer);
        
        editTitle = findViewById(R.id.editTitle);
        spinnerType = findViewById(R.id.spinnerType);
        editChannel = findViewById(R.id.editChannel);
        editTags = findViewById(R.id.editTags);
        editTime = findViewById(R.id.editTime);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        editAttachment = findViewById(R.id.editAttachment);
        editEditorLink = findViewById(R.id.editEditorLink);
        
        selectedDateTime = Calendar.getInstance();
        
        // Clear existing mock subtasks in container
        if (subtasksContainer != null) {
            subtasksContainer.removeAllViews();
        }
    }
    
    /**
     * Thiết lập các event listeners
     */
    private void setupListeners() {
        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Nút Submit - Tạo content
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> createContent());
        }
        
        // Nút Add/Save Subtask
        if (btnAddSubtask != null) {
            btnAddSubtask.setOnClickListener(v -> handleAddSubtaskClick());
        }
        
        // Time picker
        if (editTime != null) {
            editTime.setOnClickListener(v -> showDateTimePicker());
        }
    }
    
    /**
     * Tạo content mới
     */
    private void createContent() {
        // Lấy dữ liệu từ các input fields
        String title = editTitle.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String channel = editChannel.getText().toString().trim();
        String tags = editTags.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String attachment = editAttachment.getText().toString().trim();
        String editorLink = editEditorLink.getText().toString().trim();
        
        // Validate dữ liệu
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (channel.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập kênh đăng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Kiểm tra xem có draft subtask chưa lưu không
        if (currentDraftSubtask != null) {
            Toast.makeText(this, "Vui lòng lưu hoặc xóa subtask đang soạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lưu vào database hoặc gửi lên server
        // Ví dụ: createContentInDatabase(title, type, channel, tags, time, status, attachment, editorLink, savedSubtasks);
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("CreateContent", "Title: " + title);
        android.util.Log.d("CreateContent", "Type: " + type);
        android.util.Log.d("CreateContent", "Channel: " + channel);
        android.util.Log.d("CreateContent", "Tags: " + tags);
        android.util.Log.d("CreateContent", "Time: " + time);
        android.util.Log.d("CreateContent", "Status: " + status);
        android.util.Log.d("CreateContent", "Attachment: " + attachment);
        android.util.Log.d("CreateContent", "Editor Link: " + editorLink);
        android.util.Log.d("CreateContent", "Subtasks count: " + savedSubtasks.size());
        
        for (int i = 0; i < savedSubtasks.size(); i++) {
            SubtaskData subtask = savedSubtasks.get(i);
            android.util.Log.d("CreateContent", "Subtask " + (i+1) + ": " + subtask.title + " - " + subtask.assignee + " - " + subtask.deadline);
        }
        
        Toast.makeText(this, "Tạo content thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    /**
     * Hiển thị Date & Time Picker
     */
    private void showDateTimePicker() {
        // Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // Sau khi chọn ngày, hiển thị Time Picker
                showTimePicker();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    /**
     * Hiển thị Time Picker
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                
                // Cập nhật text của EditText
                updateTimeField();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true // 24-hour format
        );
        timePickerDialog.show();
    }
    
    /**
     * Cập nhật hiển thị thời gian
     */
    private void updateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        editTime.setText(sdf.format(selectedDateTime.getTime()));
    }
    
    // ==================== SUBTASK MANAGEMENT ====================
    
    /**
     * Xử lý khi click nút Add/Save Subtask
     */
    private void handleAddSubtaskClick() {
        if (currentDraftSubtask == null) {
            // Chưa có draft subtask -> Tạo mới
            addNewDraftSubtask();
        } else {
            // Đã có draft subtask -> Lưu nó
            saveDraftSubtask();
        }
    }
    
    /**
     * Thêm một subtask draft mới vào container
     */
    private void addNewDraftSubtask() {
        // Inflate layout subtask editable
        LayoutInflater inflater = LayoutInflater.from(this);
        View subtaskView = inflater.inflate(R.layout.item_subtask_editable, subtasksContainer, false);
        
        // Lấy các view components
        EditText editSubtaskTitle = subtaskView.findViewById(R.id.editSubtaskTitle);
        Spinner spinnerAssign = subtaskView.findViewById(R.id.spinnerAssign);
        EditText editSubtaskDeadline = subtaskView.findViewById(R.id.editSubtaskDeadline);
        ImageButton btnDeleteSubtask = subtaskView.findViewById(R.id.btnDeleteSubtask);
        
        // Setup deadline picker
        editSubtaskDeadline.setOnClickListener(v -> showSubtaskDeadlinePicker(editSubtaskDeadline));
        
        // Setup delete button
        btnDeleteSubtask.setOnClickListener(v -> {
            subtasksContainer.removeView(subtaskView);
            currentDraftSubtask = null;
            isDraftSubtaskValid = false;
            updateAddSubtaskButton();
        });
        
        // Setup validation - Theo dõi các trường input
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDraftSubtask(editSubtaskTitle, editSubtaskDeadline);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        editSubtaskTitle.addTextChangedListener(validationWatcher);
        editSubtaskDeadline.addTextChangedListener(validationWatcher);
        
        // Thêm vào container
        subtasksContainer.addView(subtaskView);
        currentDraftSubtask = subtaskView;
        isDraftSubtaskValid = false;
        
        // Cập nhật nút Add Subtask
        updateAddSubtaskButton();
        
        // Focus vào title
        editSubtaskTitle.requestFocus();
    }
    
    /**
     * Validate draft subtask
     */
    private void validateDraftSubtask(EditText editTitle, EditText editDeadline) {
        String title = editTitle.getText().toString().trim();
        String deadline = editDeadline.getText().toString().trim();
        
        // Valid nếu cả title và deadline đều không rỗng
        isDraftSubtaskValid = !title.isEmpty() && !deadline.isEmpty();
        updateAddSubtaskButton();
    }
    
    /**
     * Cập nhật trạng thái nút Add Subtask
     */
    private void updateAddSubtaskButton() {
        if (currentDraftSubtask == null) {
            // Không có draft -> Hiển thị "Thêm Subtask" (enabled)
            btnAddSubtask.setText(R.string.btn_add_subtask);
            btnAddSubtask.setEnabled(true);
            btnAddSubtask.setAlpha(1.0f);
            btnAddSubtask.setBackgroundTintList(null); // Reset về style mặc định
            btnAddSubtask.setTextColor(getColor(R.color.colorPrimary));
        } else {
            // Có draft
            if (isDraftSubtaskValid) {
                // Draft valid -> "Thêm Subtask" (enabled, màu xanh lá)
                btnAddSubtask.setText(R.string.btn_add_subtask);
                btnAddSubtask.setEnabled(true);
                btnAddSubtask.setAlpha(1.0f);
                btnAddSubtask.setBackgroundTintList(null);
                btnAddSubtask.setTextColor(getColor(R.color.colorPrimary));
            } else {
                // Draft invalid -> "Thêm Subtask" (disabled, mờ)
                btnAddSubtask.setText(R.string.btn_add_subtask);
                btnAddSubtask.setEnabled(false);
                btnAddSubtask.setAlpha(0.5f);
                btnAddSubtask.setTextColor(getColor(R.color.textSecondary));
            }
        }
    }
    
    /**
     * Lưu draft subtask hiện tại
     */
    private void saveDraftSubtask() {
        if (currentDraftSubtask == null || !isDraftSubtaskValid) {
            return;
        }
        
        // Lấy dữ liệu từ draft
        EditText editSubtaskTitle = currentDraftSubtask.findViewById(R.id.editSubtaskTitle);
        Spinner spinnerAssign = currentDraftSubtask.findViewById(R.id.spinnerAssign);
        EditText editSubtaskDeadline = currentDraftSubtask.findViewById(R.id.editSubtaskDeadline);
        
        String title = editSubtaskTitle.getText().toString().trim();
        String assignee = spinnerAssign.getSelectedItem().toString();
        String deadline = editSubtaskDeadline.getText().toString().trim();
        
        // Lưu vào danh sách
        savedSubtasks.add(new SubtaskData(title, assignee, deadline));
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("CreateContent", "Added Subtask - Title: " + title);
        android.util.Log.d("CreateContent", "Added Subtask - Assignee: " + assignee);
        android.util.Log.d("CreateContent", "Added Subtask - Deadline: " + deadline);
        
        // Disable các input trong subtask này (chuyển sang chế độ view)
        editSubtaskTitle.setEnabled(false);
        spinnerAssign.setEnabled(false);
        editSubtaskDeadline.setEnabled(false);
        editSubtaskTitle.setAlpha(0.7f);
        spinnerAssign.setAlpha(0.7f);
        editSubtaskDeadline.setAlpha(0.7f);
        
        // Reset draft state
        currentDraftSubtask = null;
        isDraftSubtaskValid = false;
        
        // Cập nhật nút
        updateAddSubtaskButton();
        
        Toast.makeText(this, "Đã thêm subtask: " + title, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Hiển thị Date Picker cho subtask deadline
     */
    private void showSubtaskDeadlinePicker(EditText editDeadline) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                String deadline = String.format(Locale.getDefault(), "%02d/%02d/%d", 
                    dayOfMonth, month + 1, year);
                editDeadline.setText(deadline);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}
