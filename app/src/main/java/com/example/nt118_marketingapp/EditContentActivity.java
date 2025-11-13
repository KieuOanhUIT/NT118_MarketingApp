package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.nt118_marketingapp.model.Content;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private TextView tvHeaderTitle;
    
    // Input fields
    private EditText editTitle;
    private Spinner spinnerType;
    private EditText editChannel;
    private EditText editTags;
    private EditText editDate;
    private EditText editTime;
    private Spinner spinnerStatus;
    private EditText editAttachment;
    private EditText editEditorLink;
    
    // State variables
    private boolean isEditMode = false; // Trạng thái chế độ chỉnh sửa
    private Calendar selectedDateTime;
    private String currentStatus = ""; // Lưu trạng thái hiện tại của content
    private String contentID = ""; // ID của content trong Firebase
    
    // Firebase
    private DatabaseReference contentRef;
    
    // Draft subtask tracking
    private View currentDraftSubtask = null;
    private boolean isDraftSubtaskValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_content);
        
        // Initialize Firebase
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        
        // Initialize views
        initViews();
        
        // Set up event listeners
        setupListeners();
        
        // Load data (giả lập dữ liệu hoặc lấy từ Intent/Database)
        loadContentData();
        
        // Check if Edit Mode should be enabled from Intent
        boolean shouldEnableEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        
        // Kiểm tra xem content có status locked không
        if (isLockedStatus(currentStatus)) {
            // Nếu status là Done/Approved/Scheduled/Published -> disable nút Edit
            shouldEnableEditMode = false;
            btnEditSave.setEnabled(false);
            btnEditSave.setAlpha(0.4f);
        }
        
        // Set initial state based on Intent or default to View mode
        setEditMode(shouldEnableEditMode);
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEditSave = findViewById(R.id.btnEditSave);
        btnAddSubtask = findViewById(R.id.btnAddSubtask);
        subtasksContainer = findViewById(R.id.subtasksContainer);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        
        editTitle = findViewById(R.id.editTitle);
        spinnerType = findViewById(R.id.spinnerType);
        editChannel = findViewById(R.id.editChannel);
        editTags = findViewById(R.id.editTags);
        editDate = findViewById(R.id.editDate);
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
        
        // Date picker
        editDate.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePicker();
            }
        });
        
        // Time picker
        editTime.setOnClickListener(v -> {
            if (isEditMode) {
                showTimePicker();
            }
        });
    }

    /**
     * Load dữ liệu content (từ Intent hoặc dữ liệu mẫu)
     */
    private void loadContentData() {
        Intent intent = getIntent();
        
        // Kiểm tra xem có CONTENT_ID từ Intent không
        if (intent.hasExtra("CONTENT_ID")) {
            contentID = intent.getStringExtra("CONTENT_ID");
            
            if (contentID != null && !contentID.isEmpty()) {
                // Load data từ Firebase
                contentRef.child(contentID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Content content = snapshot.getValue(Content.class);
                            if (content != null) {
                                // Set dữ liệu vào các field
                                if (content.getTitle() != null) editTitle.setText(content.getTitle());
                                if (content.getTag() != null) editTags.setText(content.getTag());
                                if (content.getChannel() != null) editChannel.setText(content.getChannel());
                                
                                // Parse timestamp và tách thành date và time
                                if (content.getCreatedTime() != null && !content.getCreatedTime().isEmpty()) {
                                    try {
                                        // Giả sử timestamp có format "dd/MM/yyyy HH:mm"
                                        String[] parts = content.getCreatedTime().split(" ");
                                        if (parts.length >= 1) {
                                            editDate.setText(parts[0]); // Ngày
                                        }
                                        if (parts.length >= 2) {
                                            editTime.setText(parts[1]); // Giờ
                                        }
                                    } catch (Exception e) {
                                        // Nếu parse lỗi, set toàn bộ vào date
                                        editDate.setText(content.getCreatedTime());
                                    }
                                }
                                
                                if (content.getEditorLink() != null) editEditorLink.setText(content.getEditorLink());
                                if (content.getUrl() != null) editAttachment.setText(content.getUrl());
                                
                                // Lưu status hiện tại
                                currentStatus = content.getStatus() != null ? content.getStatus() : "";
                                
                                // Set status trong Spinner
                                if (content.getStatus() != null) {
                                    String[] statusArray = getResources().getStringArray(R.array.full_content_status_options);
                                    for (int i = 0; i < statusArray.length; i++) {
                                        if (statusArray[i].equalsIgnoreCase(content.getStatus())) {
                                            spinnerStatus.setSelection(i);
                                            break;
                                        }
                                    }
                                }
                                
                                // Set type trong Spinner (nếu có)
                                // TODO: Implement type selection nếu cần
                                spinnerType.setSelection(0);
                            }
                        } else {
                            Toast.makeText(EditContentActivity.this, "Không tìm thấy content", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditContentActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        } else {
            // Dữ liệu mẫu nếu không có Intent (for testing)
            editTitle.setText("Content Marketing Q4 2025");
            spinnerType.setSelection(0);
            editChannel.setText("Fanpage Công ty");
            editTags.setText("marketing, Q4, promotion");
            editDate.setText("24/10/2025");
            editTime.setText("14:30");
            spinnerStatus.setSelection(1);
            editAttachment.setText("https://drive.google.com/example");
            editEditorLink.setText("https://docs.google.com/example");
        }
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
        
        // Cập nhật header title dựa trên trạng thái
        if (enabled) {
            tvHeaderTitle.setText(R.string.edit_content_title); // "Chỉnh sửa Content"
        } else {
            tvHeaderTitle.setText(R.string.view_content_title); // "Xem Content"
        }
        
        // Cập nhật text và style của nút Edit/Save
        if (enabled) {
            btnEditSave.setText(R.string.btn_save);
            btnEditSave.setBackgroundTintList(getColorStateList(R.color.colorAccent));
        } else {
            btnEditSave.setText(R.string.btn_edit);
            btnEditSave.setBackgroundTintList(getColorStateList(R.color.colorSecondary));
        }
        
        // Thay đổi adapter của spinnerStatus dựa trên chế độ
        updateStatusSpinner(enabled);
        
        // Bật/tắt khả năng chỉnh sửa cho các EditText
        editTitle.setEnabled(enabled);
        editChannel.setEnabled(enabled);
        editTags.setEnabled(enabled);
        editDate.setEnabled(enabled);
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
        editDate.setAlpha(alpha);
        editTime.setAlpha(alpha);
        editAttachment.setAlpha(alpha);
        editEditorLink.setAlpha(alpha);
        spinnerType.setAlpha(alpha);
        spinnerStatus.setAlpha(alpha);
        
        // Show/Hide nút Add Subtask dựa trên chế độ
        btnAddSubtask.setVisibility(enabled ? View.VISIBLE : View.GONE);
        
        // Khoá/mở khoá tất cả subtasks hiện có
        setSubtasksEditMode(enabled);
        
        // Nếu chuyển về chế độ View, xóa draft subtask nếu có
        if (!enabled && currentDraftSubtask != null) {
            subtasksContainer.removeView(currentDraftSubtask);
            currentDraftSubtask = null;
            isDraftSubtaskValid = false;
        }
    }
    
    /**
     * Cập nhật adapter của status spinner dựa trên chế độ view/edit
     * - Chế độ xem: hiển thị tất cả status (full_content_status_options)
     * - Chế độ sửa: chỉ hiển thị To do/In progress/Done (employee_content_status_options)
     */
    private void updateStatusSpinner(boolean isEditMode) {
        String currentStatusValue = currentStatus;
        
        if (isEditMode) {
            // Chế độ edit: chỉ hiển thị 3 status (To do/In progress/Done)
            ArrayAdapter<CharSequence> employeeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.employee_content_status_options,
                android.R.layout.simple_spinner_item
            );
            employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(employeeAdapter);
            
            // Set lại vị trí của status hiện tại trong danh sách employee
            String[] employeeStatuses = getResources().getStringArray(R.array.employee_content_status_options);
            for (int i = 0; i < employeeStatuses.length; i++) {
                if (employeeStatuses[i].equals(currentStatusValue)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        } else {
            // Chế độ view: hiển thị tất cả status
            ArrayAdapter<CharSequence> fullAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.full_content_status_options,
                android.R.layout.simple_spinner_item
            );
            fullAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(fullAdapter);
            
            // Set lại vị trí của status hiện tại trong danh sách full
            String[] fullStatuses = getResources().getStringArray(R.array.full_content_status_options);
            for (int i = 0; i < fullStatuses.length; i++) {
                if (fullStatuses[i].equals(currentStatusValue)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Khoá/mở khoá tất cả subtasks trong container
     */
    private void setSubtasksEditMode(boolean enabled) {
        for (int i = 0; i < subtasksContainer.getChildCount(); i++) {
            View subtaskView = subtasksContainer.getChildAt(i);
            
            // Tìm các view trong subtask
            EditText editSubtaskTitle = subtaskView.findViewById(R.id.editSubtaskTitle);
            EditText editAssign = subtaskView.findViewById(R.id.editAssign);
            EditText editSubtaskDeadline = subtaskView.findViewById(R.id.editSubtaskDeadline);
            ImageButton btnDeleteSubtask = subtaskView.findViewById(R.id.btnDeleteSubtask);
            LinearLayout layoutAssign = subtaskView.findViewById(R.id.layoutAssign);
            android.widget.CheckBox checkboxSubtask = subtaskView.findViewById(R.id.checkboxSubtask);
            
            if (editSubtaskTitle != null) {
                editSubtaskTitle.setEnabled(enabled);
                editSubtaskTitle.setAlpha(enabled ? 1.0f : 0.7f);
            }
            if (editAssign != null) {
                editAssign.setEnabled(enabled);
                editAssign.setAlpha(enabled ? 1.0f : 0.7f);
            }
            if (editSubtaskDeadline != null) {
                editSubtaskDeadline.setEnabled(enabled);
                editSubtaskDeadline.setClickable(enabled);
                editSubtaskDeadline.setAlpha(enabled ? 1.0f : 0.7f);
            }
            if (layoutAssign != null) {
                layoutAssign.setClickable(enabled);
                layoutAssign.setAlpha(enabled ? 1.0f : 0.7f);
            }
            if (btnDeleteSubtask != null) {
                btnDeleteSubtask.setEnabled(enabled);
                btnDeleteSubtask.setAlpha(enabled ? 1.0f : 0.4f);
            }
            if (checkboxSubtask != null) {
                checkboxSubtask.setEnabled(enabled);
                checkboxSubtask.setAlpha(enabled ? 1.0f : 0.7f);
            }
        }
    }
    
    /**
     * Kiểm tra xem status có bị lock không
     */
    private boolean isLockedStatus(String status) {
        if (status == null || status.isEmpty()) return false;
        String statusLower = status.toLowerCase();
        return statusLower.equals("done") ||
                statusLower.equals("approved") ||
                statusLower.equals("scheduled") ||
                statusLower.equals("published");
    }

    /**
     * Hiển thị Date Picker
     */
    private void showDatePicker() {
        // Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // Cập nhật text của EditText Date
                updateDateField();
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
                
                // Cập nhật text của EditText Time
                updateTimeField();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true // 24-hour format
        );
        timePickerDialog.show();
    }

    /**
     * Cập nhật hiển thị ngày
     */
    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editDate.setText(sdf.format(selectedDateTime.getTime()));
    }

    /**
     * Cập nhật hiển thị giờ
     */
    private void updateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
        String date = editDate.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String attachment = editAttachment.getText().toString().trim();
        String editorLink = editEditorLink.getText().toString().trim();

        // Ghép date và time thành timestamp
        String timestamp = date + " " + time;

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
        android.util.Log.d("EditContent", "Saved - Timestamp: " + timestamp);
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
        EditText editAssign = subtaskView.findViewById(R.id.editAssign);
        EditText editSubtaskDeadline = subtaskView.findViewById(R.id.editSubtaskDeadline);
        ImageButton btnDeleteSubtask = subtaskView.findViewById(R.id.btnDeleteSubtask);
        LinearLayout layoutAssign = subtaskView.findViewById(R.id.layoutAssign);
        
        // Setup assign picker (click to open popup select user)
        if (layoutAssign != null) {
            layoutAssign.setOnClickListener(v -> {
                // TODO: Mở popup chọn người phụ trách
                Toast.makeText(this, "Chọn người phụ trách", Toast.LENGTH_SHORT).show();
            });
        }
        
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
        EditText editAssign = currentDraftSubtask.findViewById(R.id.editAssign);
        EditText editSubtaskDeadline = currentDraftSubtask.findViewById(R.id.editSubtaskDeadline);
        
        String title = editSubtaskTitle.getText().toString().trim();
        String assignee = editAssign.getText().toString().trim();
        String deadline = editSubtaskDeadline.getText().toString().trim();
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("EditContent", "Saved Subtask - Title: " + title);
        android.util.Log.d("EditContent", "Saved Subtask - Assignee: " + assignee);
        android.util.Log.d("EditContent", "Saved Subtask - Deadline: " + deadline);
        
        // Disable các input trong subtask này (chuyển sang chế độ view)
        editSubtaskTitle.setEnabled(false);
        editAssign.setEnabled(false);
        editSubtaskDeadline.setEnabled(false);
        editSubtaskTitle.setAlpha(0.7f);
        editAssign.setAlpha(0.7f);
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
