package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * EditContentActivity - Màn hình xem và chỉnh sửa thông tin content
 * 
 * Chức năng:
 * - Hiển thị thông tin content (mặc định ở chế độ chỉ xem - không chỉnh sửa được)
 * - Cho phép bật/tắt chế độ chỉnh sửa qua nút Edit/Save
 * - Quản lý subtasks: thêm, sửa, xóa
 * - Lưu thay đổi khi người dùng bấm Save
 */
public class EditContentActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private Button btnEditSave;
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
    private boolean isEditMode = false; // Trạng thái chế độ chỉnh sửa
    private Calendar selectedDateTime;
    
    // Draft subtask tracking
    private View currentDraftSubtask = null;
    private boolean isDraftSubtaskValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_content);
        
        // Initialize views
        initViews();
        
        // Set up event listeners
        setupListeners();
        
        // Load data (giả lập dữ liệu hoặc lấy từ Intent/Database)
        loadContentData();
        
        // Set initial state - View mode (không cho phép chỉnh sửa)
        setEditMode(false);
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEditSave = findViewById(R.id.btnEditSave);
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
    }

    /**
     * Thiết lập các event listeners
     */
    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
        
        // Nút Edit/Save - Toggle giữa chế độ xem và chỉnh sửa
        btnEditSave.setOnClickListener(v -> toggleEditMode());
        
        // Nút Add/Save Subtask
        btnAddSubtask.setOnClickListener(v -> handleAddSubtaskClick());
        
        // Time picker - Chỉ mở khi ở chế độ chỉnh sửa
        editTime.setOnClickListener(v -> {
            if (isEditMode) {
                showDateTimePicker();
            }
        });
    }

    /**
     * Load dữ liệu content (giả lập hoặc từ Intent/Database)
     * Trong thực tế, bạn có thể nhận content ID từ Intent và load từ database
     */
    private void loadContentData() {
        // Ví dụ: Giả lập dữ liệu có sẵn
        editTitle.setText("Content Marketing Q4 2025");
        // spinnerType được chọn index 0 (Post Facebook)
        spinnerType.setSelection(0);
        editChannel.setText("Fanpage Công ty");
        editTags.setText("marketing, Q4, promotion");
        editTime.setText("24/10/2025 14:30");
        // spinnerStatus được chọn index 1 (In progress)
        spinnerStatus.setSelection(1);
        editAttachment.setText("https://drive.google.com/example");
        editEditorLink.setText("https://docs.google.com/example");
        
        // Trong thực tế, bạn sẽ load từ database hoặc Intent:
        // Intent intent = getIntent();
        // String contentId = intent.getStringExtra("CONTENT_ID");
        // loadContentFromDatabase(contentId);
    }

    /**
     * Toggle giữa chế độ xem và chỉnh sửa
     */
    private void toggleEditMode() {
        if (isEditMode) {
            // Đang ở chế độ Edit -> Chuyển sang Save
            saveContentData();
            setEditMode(false);
            Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        } else {
            // Đang ở chế độ View -> Chuyển sang Edit
            setEditMode(true);
            Toast.makeText(this, "Chế độ chỉnh sửa", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Bật/tắt chế độ chỉnh sửa cho tất cả input fields
     * @param enabled true = cho phép chỉnh sửa, false = chỉ xem
     */
    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        
        // Cập nhật text và style của nút Edit/Save
        if (enabled) {
            btnEditSave.setText(R.string.btn_save);
            btnEditSave.setBackgroundTintList(getColorStateList(R.color.colorAccent));
        } else {
            btnEditSave.setText(R.string.btn_edit);
            btnEditSave.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
        }
        
        // Bật/tắt khả năng chỉnh sửa cho các EditText
        editTitle.setEnabled(enabled);
        editChannel.setEnabled(enabled);
        editTags.setEnabled(enabled);
        editTime.setEnabled(enabled);
        editAttachment.setEnabled(enabled);
        editEditorLink.setEnabled(enabled);
        
        // Bật/tắt khả năng chọn cho Spinner
        spinnerType.setEnabled(enabled);
        spinnerStatus.setEnabled(enabled);
        
        // Thay đổi style để người dùng biết được trạng thái
        float alpha = enabled ? 1.0f : 0.7f;
        editTitle.setAlpha(alpha);
        editChannel.setAlpha(alpha);
        editTags.setAlpha(alpha);
        editTime.setAlpha(alpha);
        editAttachment.setAlpha(alpha);
        editEditorLink.setAlpha(alpha);
        spinnerType.setAlpha(alpha);
        spinnerStatus.setAlpha(alpha);
        
        // Show/Hide nút Add Subtask dựa trên chế độ
        btnAddSubtask.setVisibility(enabled ? View.VISIBLE : View.GONE);
        
        // Nếu chuyển về chế độ View, xóa draft subtask nếu có
        if (!enabled && currentDraftSubtask != null) {
            subtasksContainer.removeView(currentDraftSubtask);
            currentDraftSubtask = null;
            isDraftSubtaskValid = false;
        }
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

    /**
     * Lưu dữ liệu content
     * Trong thực tế, bạn sẽ lưu vào database hoặc gửi lên server
     */
    private void saveContentData() {
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
        
        // Lưu vào database hoặc gửi lên server
        // Ví dụ: updateContentInDatabase(contentId, title, type, channel, ...);
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("EditContent", "Saved - Title: " + title);
        android.util.Log.d("EditContent", "Saved - Type: " + type);
        android.util.Log.d("EditContent", "Saved - Channel: " + channel);
        android.util.Log.d("EditContent", "Saved - Tags: " + tags);
        android.util.Log.d("EditContent", "Saved - Time: " + time);
        android.util.Log.d("EditContent", "Saved - Status: " + status);
        android.util.Log.d("EditContent", "Saved - Attachment: " + attachment);
        android.util.Log.d("EditContent", "Saved - Editor Link: " + editorLink);
    }

    /**
     * Phương thức static để mở màn hình này từ Activity khác
     * Sử dụng:
     * EditContentActivity.start(context, contentId);
     */
    public static void start(android.content.Context context, String contentId) {
        android.content.Intent intent = new android.content.Intent(context, EditContentActivity.class);
        intent.putExtra("CONTENT_ID", contentId);
        context.startActivity(intent);
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
        
        // Cập nhật nút Add Subtask thành "Lưu Subtask" (disabled)
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
            // Không có draft -> Hiển thị "Thêm Subtask"
            btnAddSubtask.setText(R.string.btn_add_subtask);
            btnAddSubtask.setEnabled(true);
            btnAddSubtask.setAlpha(1.0f);
            btnAddSubtask.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
            btnAddSubtask.setTextColor(getColor(R.color.background));
        } else {
            // Có draft -> Hiển thị "Lưu Subtask"
            btnAddSubtask.setText("Lưu Subtask");
            btnAddSubtask.setEnabled(isDraftSubtaskValid);
            btnAddSubtask.setAlpha(isDraftSubtaskValid ? 1.0f : 0.5f);
            
            if (isDraftSubtaskValid) {
                btnAddSubtask.setBackgroundTintList(getColorStateList(R.color.colorSecondary));
                btnAddSubtask.setTextColor(getColor(R.color.background));
            } else {
                btnAddSubtask.setBackgroundTintList(getColorStateList(R.color.inputBorder));
                btnAddSubtask.setTextColor(getColor(R.color.textPrimary));
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
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("EditContent", "Saved Subtask - Title: " + title);
        android.util.Log.d("EditContent", "Saved Subtask - Assignee: " + assignee);
        android.util.Log.d("EditContent", "Saved Subtask - Deadline: " + deadline);
        
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
        
        Toast.makeText(this, "Đã lưu subtask: " + title, Toast.LENGTH_SHORT).show();
        
        // Trong thực tế, bạn sẽ lưu vào database
        // saveSubtaskToDatabase(contentId, title, assignee, deadline);
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
