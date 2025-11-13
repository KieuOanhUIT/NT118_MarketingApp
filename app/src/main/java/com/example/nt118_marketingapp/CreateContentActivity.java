package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nt118_marketingapp.model.Content;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private LinearLayout layoutContentType;
    private EditText editContentType;
    private EditText editChannel;
    private EditText editTags;
    private EditText editDate;
    private EditText editTime;
    private Spinner spinnerStatus;
    private TextView tvEditorLinkLabel;
    private EditText editAttachment;
    private EditText editEditorLink;
    
    // Firebase
    private DatabaseReference contentRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    
    // State variables
    private Calendar selectedDateTime;
    private String selectedContentType = ""; // Lưu loại nội dung đã chọn
    private String customContentType = ""; // Lưu loại nội dung custom nếu chọn "Khác"
    
    // Draft subtask tracking
    private View currentDraftSubtask = null;
    private boolean isDraftSubtaskValid = false;
    private List<SubtaskData> savedSubtasks = new ArrayList<>();
    
    // User list for assignee selection
    private List<UserData> userList = new ArrayList<>();
    
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
    
    // User data class
    private static class UserData {
        String userId;
        String fullName;
        String email;
        
        UserData(String userId, String fullName, String email) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        userRef = FirebaseDatabase.getInstance().getReference("User");
        
        // Initialize views
        initViews();
        
        // Set up event listeners
        setupListeners();
        
        // Setup spinners
        setupSpinners();
        
        // Load users from Firebase
        loadUsers();
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
        layoutContentType = findViewById(R.id.layoutContentType);
        editContentType = findViewById(R.id.editContentType);
        editChannel = findViewById(R.id.editChannel);
        editTags = findViewById(R.id.editTags);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        tvEditorLinkLabel = findViewById(R.id.tvEditorLinkLabel);
        editAttachment = findViewById(R.id.editAttachment);
        editEditorLink = findViewById(R.id.editEditorLink);
        
        selectedDateTime = Calendar.getInstance();
        
        // Check if date/hour are prefilled from calendar
        Intent intent = getIntent();
        if (intent.hasExtra("DATE") && intent.hasExtra("HOUR")) {
            String dateStr = intent.getStringExtra("DATE"); // Format: "yyyy-MM-dd"
            int hour = intent.getIntExtra("HOUR", -1);
            
            if (dateStr != null && hour >= 0) {
                prefillDateTime(dateStr, hour);
            }
        }
        
        // Clear existing mock subtasks in container
        if (subtasksContainer != null) {
            subtasksContainer.removeAllViews();
        }
    }
    
    /**
     * Prefill date and time from calendar selection
     */
    private void prefillDateTime(String dateStr, int hour) {
        try {
            // Parse date "yyyy-MM-dd"
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Calendar months are 0-indexed
                int day = Integer.parseInt(parts[2]);
                
                selectedDateTime.set(year, month, day, hour, 0);
                
                // Format and display date "dd/MM/yyyy"
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editDate.setText(dateFormatter.format(selectedDateTime.getTime()));
                
                // Format and display time "HH:mm"
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                editTime.setText(timeFormatter.format(selectedDateTime.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Thiết lập các spinners
     */
    private void setupSpinners() {
        // Status Spinner - Thêm option mặc định "To do" ở đầu
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("To do"); // Mặc định
        statusOptions.add("In progress");
        statusOptions.add("Done");
        
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(0); // Mặc định chọn "To do"
        
        // Listener để thay đổi label Editor Link khi status thay đổi
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getItemAtPosition(position).toString();
                updateEditorLinkLabel(status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    /**
     * Cập nhật label của Editor Link field dựa trên status
     */
    private void updateEditorLinkLabel(String status) {
        if (status.equals("Done")) {
            tvEditorLinkLabel.setText("Link Editor *");
        } else {
            tvEditorLinkLabel.setText(R.string.editor_link);
        }
    }
    
    /**
     * Load danh sách users từ Firebase
     */
    private void loadUsers() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String userId = child.getKey();
                    String fullName = child.child("FullName").getValue(String.class);
                    String email = child.child("Email").getValue(String.class);
                    
                    if (fullName != null && email != null) {
                        userList.add(new UserData(userId, fullName, email));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateContentActivity.this, 
                    "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
            }
        });
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
        
        // Date picker
        if (editDate != null) {
            editDate.setOnClickListener(v -> showDatePicker());
        }
        
        // Time picker
        if (editTime != null) {
            editTime.setOnClickListener(v -> showTimePicker());
        }
        
        // Content Type - Click để mở popup
        if (layoutContentType != null) {
            layoutContentType.setOnClickListener(v -> showContentTypePopup());
        }
        
        if (editContentType != null) {
            editContentType.setOnClickListener(v -> showContentTypePopup());
        }
    }
    
    /**
     * Hiển thị popup chọn loại nội dung
     */
    private void showContentTypePopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_select_content_type);
        dialog.setCancelable(true);

        // Get views from popup
        RadioGroup radioGroupContentType = dialog.findViewById(R.id.radioGroupContentType);
        RadioButton radioPostFacebook = dialog.findViewById(R.id.radioPostFacebook);
        RadioButton radioBlog = dialog.findViewById(R.id.radioBlog);
        RadioButton radioVideo = dialog.findViewById(R.id.radioVideo);
        RadioButton radioOther = dialog.findViewById(R.id.radioOther);
        LinearLayout layoutCustomTypeInput = dialog.findViewById(R.id.layoutCustomTypeInput);
        EditText editCustomTypePopup = dialog.findViewById(R.id.editCustomTypePopup);
        Button btnSaveContentType = dialog.findViewById(R.id.btnSaveContentType);

        // Set current selection
        if (!selectedContentType.isEmpty()) {
            switch (selectedContentType) {
                case "Post Facebook":
                    radioPostFacebook.setChecked(true);
                    break;
                case "Blog":
                    radioBlog.setChecked(true);
                    break;
                case "Video":
                    radioVideo.setChecked(true);
                    break;
                default:
                    radioOther.setChecked(true);
                    layoutCustomTypeInput.setVisibility(View.VISIBLE);
                    editCustomTypePopup.setText(customContentType);
                    break;
            }
        }

        // Handle radio button selection
        radioGroupContentType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOther) {
                layoutCustomTypeInput.setVisibility(View.VISIBLE);
                editCustomTypePopup.requestFocus();
            } else {
                layoutCustomTypeInput.setVisibility(View.GONE);
                editCustomTypePopup.setText("");
            }
        });

        // Handle save button
        btnSaveContentType.setOnClickListener(v -> {
            int selectedId = radioGroupContentType.getCheckedRadioButtonId();
            
            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn loại nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == R.id.radioPostFacebook) {
                selectedContentType = "Post Facebook";
                customContentType = "";
                editContentType.setText("Post Facebook");
            } else if (selectedId == R.id.radioBlog) {
                selectedContentType = "Blog";
                customContentType = "";
                editContentType.setText("Blog");
            } else if (selectedId == R.id.radioVideo) {
                selectedContentType = "Video";
                customContentType = "";
                editContentType.setText("Video");
            } else if (selectedId == R.id.radioOther) {
                String customType = editCustomTypePopup.getText().toString().trim();
                if (customType.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập loại nội dung", Toast.LENGTH_SHORT).show();
                    editCustomTypePopup.requestFocus();
                    return;
                }
                selectedContentType = "Khác";
                customContentType = customType;
                editContentType.setText(customType);
            }

            dialog.dismiss();
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
    
    /**
     * Tạo content mới
     */
    private void createContent() {
        // Lấy dữ liệu từ các input fields
        String title = editTitle.getText().toString().trim();
        
        // Xử lý Type - Sử dụng selectedContentType và customContentType
        String type;
        if (selectedContentType.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại nội dung", Toast.LENGTH_SHORT).show();
            layoutContentType.requestFocus();
            return;
        }
        
        if (selectedContentType.equals("Khác")) {
            type = customContentType;
        } else {
            type = selectedContentType;
        }
        
        String channel = editChannel.getText().toString().trim();
        String tags = editTags.getText().toString().trim();
        String dateStr = editDate.getText().toString().trim();
        String timeStr = editTime.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String attachment = editAttachment.getText().toString().trim();
        String editorLink = editEditorLink.getText().toString().trim();
        
        // Validate dữ liệu
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            editTitle.requestFocus();
            return;
        }
        
        if (channel.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập kênh đăng", Toast.LENGTH_SHORT).show();
            editChannel.requestFocus();
            return;
        }
        
        if (dateStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày đăng tải (dự kiến)", Toast.LENGTH_SHORT).show();
            editDate.requestFocus();
            return;
        }
        
        if (timeStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ đăng tải (dự kiến)", Toast.LENGTH_SHORT).show();
            editTime.requestFocus();
            return;
        }
        
        // Combine date and time
        String combinedTime = dateStr + " " + timeStr;
        
        // Validate: Nếu status là Done thì Editor Link là required
        if (status.equals("Done") && editorLink.isEmpty()) {
            Toast.makeText(this, "Link Editor là bắt buộc khi trạng thái là Done", Toast.LENGTH_SHORT).show();
            editEditorLink.requestFocus();
            return;
        }
        
        // Kiểm tra xem có draft subtask chưa lưu không
        if (currentDraftSubtask != null) {
            Toast.makeText(this, "Vui lòng lưu hoặc xóa subtask đang soạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lấy userId từ Firebase Auth
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Lấy thời gian hiện tại làm CreatedTime
        String createdTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        
        // Tạo Content object
        Content newContent = new Content(title, type, channel, tags, createdTime, status, 
                                        attachment, editorLink, userId);
        
        // Tạo key mới cho content
        String contentId = contentRef.push().getKey();
        
        if (contentId == null) {
            Toast.makeText(this, "Lỗi tạo ID content", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo map để lưu vào Firebase
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("Title", title);
        contentMap.put("Type", type);
        contentMap.put("Channel", channel);
        contentMap.put("Tag", tags);
        contentMap.put("CreatedTime", combinedTime); // Thời gian đăng tải dự kiến (dd/MM/yyyy HH:mm)
        contentMap.put("ModifiedTime", combinedTime);
        contentMap.put("PublishedTime", combinedTime); // Thời gian đăng tải dự kiến
        contentMap.put("Status", status);
        contentMap.put("Url", attachment);
        contentMap.put("EditorLink", editorLink);
        contentMap.put("UserId", userId);
        
        // Lưu vào Firebase
        contentRef.child(contentId).setValue(contentMap)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Tạo content thành công!", Toast.LENGTH_SHORT).show();
                
                // Quay về ContentListActivity và refresh
                Intent intent = new Intent(CreateContentActivity.this, ContentListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tạo content: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Hiển thị Date Picker
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // Cập nhật date field
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
                
                // Cập nhật time field
                updateTimeField();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true // 24-hour format
        );
        timePickerDialog.show();
    }
    
    /**
     * Cập nhật hiển thị date
     */
    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editDate.setText(sdf.format(selectedDateTime.getTime()));
    }
    
    /**
     * Cập nhật hiển thị time
     */
    private void updateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
        LinearLayout layoutAssign = subtaskView.findViewById(R.id.layoutAssign);
        EditText editAssign = subtaskView.findViewById(R.id.editAssign);
        EditText editSubtaskDeadline = subtaskView.findViewById(R.id.editSubtaskDeadline);
        ImageButton btnDeleteSubtask = subtaskView.findViewById(R.id.btnDeleteSubtask);
        
        // Setup assignee picker
        layoutAssign.setOnClickListener(v -> showAssigneePopup(editAssign));
        editAssign.setOnClickListener(v -> showAssigneePopup(editAssign));
        
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
        EditText editAssign = currentDraftSubtask.findViewById(R.id.editAssign);
        EditText editSubtaskDeadline = currentDraftSubtask.findViewById(R.id.editSubtaskDeadline);
        
        String title = editSubtaskTitle.getText().toString().trim();
        String assignee = editAssign.getText().toString().trim();
        String deadline = editSubtaskDeadline.getText().toString().trim();
        
        // Lưu vào danh sách
        savedSubtasks.add(new SubtaskData(title, assignee, deadline));
        
        // Log để kiểm tra (tạm thời)
        android.util.Log.d("CreateContent", "Added Subtask - Title: " + title);
        android.util.Log.d("CreateContent", "Added Subtask - Assignee: " + assignee);
        android.util.Log.d("CreateContent", "Added Subtask - Deadline: " + deadline);
        
        // Disable các input trong subtask này (chuyển sang chế độ view)
        editSubtaskTitle.setEnabled(false);
        editAssign.setEnabled(false);
        editSubtaskDeadline.setEnabled(false);
        editSubtaskTitle.setAlpha(0.7f);
        editAssign.setAlpha(0.7f);
        editSubtaskDeadline.setAlpha(0.7f);
        
        // Disable click listeners
        LinearLayout layoutAssign = currentDraftSubtask.findViewById(R.id.layoutAssign);
        layoutAssign.setClickable(false);
        layoutAssign.setEnabled(false);
        
        // Reset draft state
        currentDraftSubtask = null;
        isDraftSubtaskValid = false;
        
        // Cập nhật nút
        updateAddSubtaskButton();
        
        Toast.makeText(this, "Đã thêm subtask: " + title, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Hiển thị popup chọn người phụ trách
     */
    private void showAssigneePopup(EditText editAssign) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_select_assignee);
        dialog.setCancelable(true);

        // Get views from popup
        EditText editSearchUser = dialog.findViewById(R.id.editSearchUser);
        LinearLayout layoutUserList = dialog.findViewById(R.id.layoutUserList);

        // Display user list
        displayUserList(layoutUserList, editAssign, dialog, "");

        // Search functionality
        editSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                displayUserList(layoutUserList, editAssign, dialog, searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Hiển thị danh sách user trong popup
     */
    private void displayUserList(LinearLayout layoutUserList, EditText editAssign, 
                                  Dialog dialog, String searchText) {
        layoutUserList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (UserData user : userList) {
            // Filter by search text
            if (!searchText.isEmpty()) {
                String fullNameLower = user.fullName.toLowerCase();
                String emailLower = user.email.toLowerCase();
                if (!fullNameLower.contains(searchText) && !emailLower.contains(searchText)) {
                    continue;
                }
            }

            // Inflate user item
            View userItemView = inflater.inflate(R.layout.item_user_assignee, layoutUserList, false);
            TextView tvUserName = userItemView.findViewById(R.id.tvUserName);
            TextView tvUserEmail = userItemView.findViewById(R.id.tvUserEmail);

            tvUserName.setText(user.fullName);
            tvUserEmail.setText(user.email);

            // Click to select user
            userItemView.setOnClickListener(v -> {
                editAssign.setText(user.fullName);
                dialog.dismiss();
            });

            layoutUserList.addView(userItemView);
        }

        // Show message if no users found
        if (layoutUserList.getChildCount() == 0) {
            TextView tvNoUsers = new TextView(this);
            tvNoUsers.setText("Không tìm thấy người dùng");
            tvNoUsers.setTextColor(getResources().getColor(R.color.textSecondary, null));
            tvNoUsers.setPadding(16, 16, 16, 16);
            tvNoUsers.setGravity(android.view.Gravity.CENTER);
            layoutUserList.addView(tvNoUsers);
        }
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
