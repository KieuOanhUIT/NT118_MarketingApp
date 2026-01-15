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
import android.widget.CheckBox;
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
import com.example.nt118_marketingapp.model.SubTask;
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
 * ContentManageActivity - Màn hình quản lý content (Tạo mới / Xem / Chỉnh sửa)
 *
 * Chức năng:
 * - MODE CREATE: Tạo content mới với subtasks
 * - MODE VIEW: Xem thông tin content (read-only)
 * - MODE EDIT: Chỉnh sửa content và subtasks
 */
public class ContentManageActivity extends AppCompatActivity {

    // Mode enum
    private enum Mode {
        CREATE,  // Tạo content mới
        VIEW,    // Xem content (read-only)
        EDIT     // Chỉnh sửa content
    }

    // UI Components
    private ImageButton btnBack;
    private Button btnSubmit;
    private Button btnEditSave;  // NEW: Toggle VIEW <-> EDIT
    private Button btnAddSubtask;
    private LinearLayout subtasksContainer;
    private TextView tvHeaderTitle;  // NEW: Dynamic header text

    // Input fields
    private EditText editTitle;
    private LinearLayout layoutContentType;
    private EditText editContentType;
    private LinearLayout layoutSpinnerType;  // NEW: Container for spinner in VIEW/EDIT mode
    private Spinner spinnerType;  // NEW: Spinner for View/Edit mode
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
    private DatabaseReference subtaskRef;
    private DatabaseReference notificationRef;
    private FirebaseAuth mAuth;

    // State variables
    private Mode currentMode = Mode.CREATE;  // NEW: Current mode
    private String contentID = null;  // NEW: null = CREATE, non-null = VIEW/EDIT
    private Calendar selectedDateTime;
    private String selectedContentType = ""; // Lưu loại nội dung đã chọn
    private String customContentType = ""; // Lưu loại nội dung custom nếu chọn "Khác"
    private String currentStatus = "";  // NEW: Current content status

    // Draft subtask tracking
    private View currentDraftSubtask = null;
    private boolean isDraftSubtaskValid = false;
    private List<SubtaskData> savedSubtasks = new ArrayList<>();
    private List<SubTask> existingSubtasks = new ArrayList<>();  // NEW: For VIEW/EDIT mode

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

    // Subtask change tracking class (for EDIT mode)
    private static class SubtaskChange {
        String subtaskId;
        String originalTitle;
        String newTitle;
        String originalUserId;
        String newUserId;
        String originalUserName;
        String newUserName;
        boolean originalIsDone;
        boolean newIsDone;
        String originalDeadline;
        String newDeadline;
        boolean isDeleted = false;

        SubtaskChange(SubTask original) {
            this.subtaskId = original.getSubTaskId();
            this.originalTitle = original.getTitle();
            this.newTitle = original.getTitle();
            this.originalUserId = original.getUserId();
            this.newUserId = original.getUserId();
            this.originalIsDone = original.getIsDone();
            this.newIsDone = original.getIsDone();
            this.originalDeadline = original.getDeadline();
            this.newDeadline = original.getDeadline();
        }

        boolean hasChanges() {
            return !originalTitle.equals(newTitle) ||
                   !originalUserId.equals(newUserId) ||
                   originalIsDone != newIsDone ||
                   !originalDeadline.equals(newDeadline) ||
                   isDeleted;
        }

        boolean hasUserChange() {
            return !originalUserId.equals(newUserId);
        }
    }

    // Track changes for existing subtasks
    private Map<String, SubtaskChange> subtaskChanges = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_manage);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        userRef = FirebaseDatabase.getInstance().getReference("User");
        subtaskRef = FirebaseDatabase.getInstance().getReference("SubTask");
        notificationRef = FirebaseDatabase.getInstance().getReference("Notification");

        // Determine mode from Intent
        Intent intent = getIntent();
        if (intent.hasExtra("CONTENT_ID")) {
            contentID = intent.getStringExtra("CONTENT_ID");
            // Check if opened directly in EDIT mode
            boolean editMode = intent.getBooleanExtra("EDIT_MODE", false);
            currentMode = editMode ? Mode.EDIT : Mode.VIEW;
        } else {
            currentMode = Mode.CREATE;  // Create new content
        }

        // Initialize views
        initViews();

        // Set up event listeners
        setupListeners();

        // Setup spinners
        setupSpinners();

        // Update UI based on mode FIRST (before loading data)
        updateUIForMode();

        // Load users from Firebase (needed for assignee names)
        loadUsers();

        // Load existing content if in VIEW/EDIT mode (AFTER UI setup)
        if ((currentMode == Mode.VIEW || currentMode == Mode.EDIT) && contentID != null && !contentID.isEmpty()) {
            // Delay content loading slightly to ensure UI is fully initialized
            subtasksContainer.post(() -> loadContentData());
        }
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnEditSave = findViewById(R.id.btnEditSave);  // NEW
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);  // NEW
        btnAddSubtask = findViewById(R.id.btnAddSubtask);
        subtasksContainer = findViewById(R.id.subtasksContainer);

        editTitle = findViewById(R.id.editTitle);
        layoutContentType = findViewById(R.id.layoutContentType);
        editContentType = findViewById(R.id.editContentType);
        layoutSpinnerType = findViewById(R.id.layoutSpinnerType);  // NEW
        spinnerType = findViewById(R.id.spinnerType);  // NEW for VIEW/EDIT mode
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

        // NEW: Type Spinner (for VIEW/EDIT mode)
        if (spinnerType != null) {
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.content_type_options, android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerType.setAdapter(typeAdapter);
        }
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
                Toast.makeText(ContentManageActivity.this,
                    "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Thiết lập các event listeners
     */
    private void setupListeners() {
        // Nút quay lại - Check for unsaved changes
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBackPress());
        }

        // Nút Submit - Tạo content
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> createContent());
        }

        // NEW: Nút Edit/Save - Toggle VIEW <-> EDIT
        if (btnEditSave != null) {
            btnEditSave.setOnClickListener(v -> toggleEditMode());
        }

        // Nút Add/Save Subtask
        if (btnAddSubtask != null) {
            btnAddSubtask.setOnClickListener(v -> handleAddSubtaskClick());
        }

        // Date picker - only allow in CREATE/EDIT mode
        if (editDate != null) {
            editDate.setOnClickListener(v -> {
                if (currentMode != Mode.VIEW) {
                    showDatePicker();
                }
            });
        }

        // Time picker - only allow in CREATE/EDIT mode
        if (editTime != null) {
            editTime.setOnClickListener(v -> {
                if (currentMode != Mode.VIEW) {
                    showTimePicker();
                }
            });
        }

        // Content Type - Click để mở popup (in CREATE and EDIT modes)
        if (layoutContentType != null) {
            layoutContentType.setOnClickListener(v -> {
                if (currentMode == Mode.CREATE || currentMode == Mode.EDIT) {
                    showContentTypePopup();
                }
            });
        }

        if (editContentType != null) {
            editContentType.setOnClickListener(v -> {
                if (currentMode == Mode.CREATE || currentMode == Mode.EDIT) {
                    showContentTypePopup();
                }
            });
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

            // Không validate ở đây nữa, cho phép user đóng popup mà không chọn
            // Validation sẽ được thực hiện khi bấm nút Tạo Content

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

        // Validate title không trùng (check trong Firebase)
        validateTitleAndCreateContent(title, type, channel, tags, dateStr, timeStr,
                                     status, attachment, editorLink);
    }

    /**
     * Validate title không trùng và tạo content
     */
    private void validateTitleAndCreateContent(String title, String type, String channel,
                                              String tags, String dateStr, String timeStr,
                                              String status, String attachment, String editorLink) {
        // Check xem title đã tồn tại chưa
        contentRef.orderByChild("Title").equalTo(title)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Title đã tồn tại
                        Toast.makeText(ContentManageActivity.this,
                            "Tiêu đề \"" + title + "\" đã tồn tại. Vui lòng chọn tên khác!",
                            Toast.LENGTH_LONG).show();
                        editTitle.requestFocus();
                        editTitle.selectAll();
                    } else {
                        // Title hợp lệ, tiếp tục tạo content
                        proceedCreateContent(title, type, channel, tags, dateStr, timeStr,
                                           status, attachment, editorLink);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ContentManageActivity.this,
                        "Lỗi kiểm tra title: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Thực hiện tạo content sau khi validate
     */
    private void proceedCreateContent(String title, String type, String channel, String tags,
                                      String dateStr, String timeStr, String status,
                                      String attachment, String editorLink) {

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

        // Tự động lưu draft nếu valid, hoặc xóa nếu rỗng
        if (currentDraftSubtask != null) {
            if (isDraftSubtaskValid) {
                saveDraftSubtask();
            } else {
                // Xóa draft rỗng
                subtasksContainer.removeView(currentDraftSubtask);
                currentDraftSubtask = null;
                isDraftSubtaskValid = false;
            }
        }

        // Lấy userId từ Firebase Auth
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Lấy thời gian hiện tại làm CreatedTime
        String createdTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        // Tạo Content object
        Content newContent = new Content(title, type, channel, tags, createdTime, status,
                                        attachment, editorLink, userId);

        // Generate auto key từ Firebase (format: -OhLzVdBk57fu7pXJjyt)
        String contentId = contentRef.push().getKey();
        
        if (contentId == null) {
            Toast.makeText(this, "Lỗi tạo ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo map để lưu vào Firebase
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("ContentId", contentId);
        contentMap.put("Title", title);
        contentMap.put("Type", type);
        contentMap.put("Channel", channel);
        contentMap.put("Tag", tags);
        contentMap.put("CreatedTime", combinedTime);
        contentMap.put("ModifiedTime", combinedTime);
        contentMap.put("PublishedTime", combinedTime);
        contentMap.put("Status", status);
        contentMap.put("Url", attachment);
        contentMap.put("EditorLink", editorLink);
        contentMap.put("UserId", userId);

        // Lưu vào Firebase
        contentRef.child(contentId).setValue(contentMap)
            .addOnSuccessListener(aVoid -> {
                // Lưu subtasks vào Firebase và gửi thông báo
                saveSubtasksToFirebase(contentId, title);

                Toast.makeText(ContentManageActivity.this,
                    "Tạo content thành công!", Toast.LENGTH_SHORT).show();

                // Quay về ContentListActivity và refresh với user data
                Intent backIntent = new Intent(ContentManageActivity.this, ContentListActivity.class);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // Pass user data from current Intent
                Intent currentIntent = getIntent();
                backIntent.putExtra("userId", currentIntent.getStringExtra("userId"));
                backIntent.putExtra("fullName", currentIntent.getStringExtra("fullName"));
                backIntent.putExtra("roleName", currentIntent.getStringExtra("roleName"));
                backIntent.putExtra("phone", currentIntent.getStringExtra("phone"));
                backIntent.putExtra("email", currentIntent.getStringExtra("email"));
                startActivity(backIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ContentManageActivity.this,
                    "Lỗi lưu content: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        } else if (isDraftSubtaskValid) {
            // Đã có draft subtask hợp lệ -> Lưu và tạo draft mới
            saveDraftSubtask();
            addNewDraftSubtask();
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
                validateAndAutoSaveSubtask(editSubtaskTitle, editAssign, editSubtaskDeadline);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editSubtaskTitle.addTextChangedListener(validationWatcher);
        editAssign.addTextChangedListener(validationWatcher);
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
     * Validate draft subtask và tự động save nếu đầy đủ dữ liệu
     */
    private void validateAndAutoSaveSubtask(EditText editTitle, EditText editAssign, EditText editDeadline) {
        String title = editTitle.getText().toString().trim();
        String assignee = editAssign.getText().toString().trim();
        String deadline = editDeadline.getText().toString().trim();

        // Valid nếu cả title, assignee và deadline đều không rỗng
        boolean allFieldsFilled = !title.isEmpty() && !assignee.isEmpty() && !deadline.isEmpty();

        if (allFieldsFilled && !isDraftSubtaskValid) {
            // Đánh dấu là hợp lệ
            isDraftSubtaskValid = true;
        } else if (!allFieldsFilled) {
            isDraftSubtaskValid = false;
        }

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

        // Lưu vào danh sách (cho cả CREATE và EDIT mode)
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
     * Hiển thị popup chọn người phụ trách (without callback - for draft subtasks)
     */
    private void showAssigneePopup(EditText editAssign) {
        showAssigneePopup(editAssign, null);
    }

    /**
     * Hiển thị popup chọn người phụ trách (with callback - for existing subtasks)
     */
    private void showAssigneePopup(EditText editAssign, UserIdCallback callback) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_select_assignee);
        dialog.setCancelable(true);

        // Get views from popup
        EditText editSearchUser = dialog.findViewById(R.id.editSearchUser);
        LinearLayout layoutUserList = dialog.findViewById(R.id.layoutUserList);

        // Display user list
        displayUserList(layoutUserList, editAssign, dialog, "", callback);

        // Search functionality
        editSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                displayUserList(layoutUserList, editAssign, dialog, searchText, callback);
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
     * Wrapper method for showSubtaskDeadlinePicker with callback
     */
    private void showDateTimePicker(DateTimeCallback callback) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                // Sau khi chọn ngày, hiển thị Time Picker
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, hourOfDay, minute) -> {
                        String deadline = String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d",
                            dayOfMonth, month + 1, year, hourOfDay, minute);
                        callback.onDateTimeSelected(deadline);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
                );
                timePickerDialog.show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Hiển thị danh sách user trong popup
     */
    private void displayUserList(LinearLayout layoutUserList, EditText editAssign,
                                  Dialog dialog, String searchText, UserIdCallback callback) {
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
                if (callback != null) {
                    callback.onUserIdFound(user.userId);
                }
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
     * Hiển thị Date Picker sau đó Time Picker cho subtask deadline
     */
    private void showSubtaskDeadlinePicker(EditText editDeadline) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                // Sau khi chọn ngày, hiển thị Time Picker
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, hourOfDay, minute) -> {
                        String deadline = String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d",
                            dayOfMonth, month + 1, year, hourOfDay, minute);
                        editDeadline.setText(deadline);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
                );
                timePickerDialog.show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Lưu subtasks vào Firebase và gửi thông báo
     */
    private void saveSubtasksToFirebase(String contentId, String contentTitle) {
        if (savedSubtasks.isEmpty()) {
            return;
        }

        for (SubtaskData subtaskData : savedSubtasks) {
            // Tìm userId từ assignee name
            findUserIdByName(subtaskData.assignee, userId -> {
                if (userId != null && !userId.isEmpty()) {
                    // Generate auto key từ Firebase
                    String subtaskId = subtaskRef.push().getKey();
                    
                    if (subtaskId == null) {
                        android.util.Log.e("ContentManage", "Failed to generate subtask ID");
                        return;
                    }
                    
                    SubTask subtask = new SubTask(
                        subtaskId,
                        contentId,
                        userId,
                        subtaskData.title,
                        false,
                        subtaskData.deadline
                    );

                    // Lưu vào Firebase
                    Map<String, Object> subtaskMap = new HashMap<>();
                    subtaskMap.put("SubTaskId", subtaskId);
                    subtaskMap.put("ContentId", contentId);
                    subtaskMap.put("UserId", userId);
                    subtaskMap.put("Title", subtaskData.title);
                    subtaskMap.put("IsDone", false);
                    subtaskMap.put("Deadline", subtaskData.deadline);

                    subtaskRef.child(subtaskId).setValue(subtaskMap)
                        .addOnSuccessListener(aVoid -> {
                            // Gửi thông báo cho user được giao subtask
                            sendSubtaskNotification(userId, contentTitle, subtaskData.title);
                        });
                }
            });
        }
    }

    /**
     * Tìm userId từ tên user
     */
    private void findUserIdByName(String fullName, UserIdCallback callback) {
        if (fullName == null || fullName.isEmpty()) {
            android.util.Log.e("ContentManage", "findUserIdByName: fullName is null or empty");
            callback.onUserIdFound(null);
            return;
        }
        
        android.util.Log.d("ContentManage", "findUserIdByName: Searching for user: " + fullName);
        
        userRef.orderByChild("FullName").equalTo(fullName)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    android.util.Log.d("ContentManage", "findUserIdByName: snapshot.exists() = " + snapshot.exists() + ", children count = " + snapshot.getChildrenCount());
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            android.util.Log.d("ContentManage", "findUserIdByName: Found userId = " + userId + " for fullName = " + fullName);
                            callback.onUserIdFound(userId);
                            return;
                        }
                    }
                    android.util.Log.e("ContentManage", "findUserIdByName: NOT FOUND user with fullName = " + fullName);
                    callback.onUserIdFound(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("ContentManage", "findUserIdByName: Database error - " + error.getMessage());
                    callback.onUserIdFound(null);
                }
            });
    }

    /**
     * Gửi thông báo cho user được giao subtask
     */
    private void sendSubtaskNotification(String userId, String contentTitle, String subtaskTitle) {
        // Generate auto key từ Firebase
        String notificationId = notificationRef.push().getKey();
        
        if (notificationId == null) {
            android.util.Log.e("CreateContent", "Failed to generate notification ID");
            return;
        }
        
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("NotiId", notificationId);
        notificationMap.put("UserId", userId);
        notificationMap.put("Type", "Task Assignment");
        notificationMap.put("Message", "Bạn được giao subtask: " + subtaskTitle + " trong content: " + contentTitle);
        notificationMap.put("IsRead", false);
        notificationMap.put("CreatedTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        notificationRef.child(notificationId).setValue(notificationMap);
    }

    /**
     * Callback interface for finding userId
     */
    private interface UserIdCallback {
        void onUserIdFound(String userId);
    }

    /**
     * Callback interface for date time picker
     */
    private interface DateTimeCallback {
        void onDateTimeSelected(String dateTime);
    }

    // ==================== VIEW/EDIT MODE METHODS (from EditContentActivity) ====================

    /**
     * Load dữ liệu content từ Firebase (for VIEW/EDIT mode) - Optimized
     */
    private void loadContentData() {
        if (contentID == null || contentID.isEmpty()) {
            android.util.Log.e("ContentManage", "Content ID null hoặc rỗng");
            Toast.makeText(this, "Lỗi: Không tìm thấy content ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("ContentManage", "Đang tải content: " + contentID);

        contentRef.child(contentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) {
                        android.util.Log.e("ContentManage", "Content không tồn tại: " + contentID);
                        Toast.makeText(ContentManageActivity.this, "Không tìm thấy content", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Content content = snapshot.getValue(Content.class);
                    if (content == null) {
                        android.util.Log.e("ContentManage", "Không thể parse content data");
                        Toast.makeText(ContentManageActivity.this, "Lỗi tải dữ liệu content", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    android.util.Log.d("ContentManage", "Content loaded: " + content.getTitle());

                    if (content != null) {
                        // Populate fields
                        if (content.getTitle() != null) editTitle.setText(content.getTitle());
                        if (content.getTag() != null) editTags.setText(content.getTag());
                        if (content.getChannel() != null) editChannel.setText(content.getChannel());

                        // Parse and display date/time
                        if (content.getCreatedTime() != null && !content.getCreatedTime().isEmpty()) {
                            try {
                                String[] parts = content.getCreatedTime().split(" ");
                                if (parts.length >= 1) editDate.setText(parts[0]);
                                if (parts.length >= 2) editTime.setText(parts[1]);
                            } catch (Exception e) {
                                editDate.setText(content.getCreatedTime());
                            }
                        }

                        if (content.getEditorLink() != null) editEditorLink.setText(content.getEditorLink());
                        if (content.getUrl() != null) editAttachment.setText(content.getUrl());

                        currentStatus = content.getStatus() != null ? content.getStatus() : "";

                        // Set status in spinner
                        if (content.getStatus() != null) {
                            String[] statusArray = getResources().getStringArray(R.array.full_content_status_options);
                            for (int i = 0; i < statusArray.length; i++) {
                                if (statusArray[i].equalsIgnoreCase(content.getStatus())) {
                                    spinnerStatus.setSelection(i);
                                    break;
                                }
                            }
                        }

                        // Set type - Always use editContentType and selectedContentType (for both CREATE and EDIT)
                        if (editContentType != null && content.getType() != null) {
                            String type = content.getType();
                            editContentType.setText(type);
                            
                            // Set selectedContentType based on content type
                            if (type.equals("Post Facebook") || type.equals("Blog") || type.equals("Video")) {
                                selectedContentType = type;
                                customContentType = "";
                            } else {
                                selectedContentType = "Khác";
                                customContentType = type;
                            }
                        }
                        
                        // Also set spinnerType if it exists (for VIEW mode compatibility)
                        if (spinnerType != null && content.getType() != null) {
                            String[] typeArray = getResources().getStringArray(R.array.content_type_options);
                            for (int i = 0; i < typeArray.length; i++) {
                                if (typeArray[i].equalsIgnoreCase(content.getType())) {
                                    spinnerType.setSelection(i);
                                    break;
                                }
                            }
                        }

                        // Load subtasks
                        loadSubtasksFromFirebase(contentID);
                    }
                } catch (Exception e) {
                    android.util.Log.e("ContentManage", "Exception populating UI: " + e.getMessage());
                    Toast.makeText(ContentManageActivity.this, "Lỗi hiển thị content", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("ContentManage", "Firebase error: " + error.getMessage());
                Toast.makeText(ContentManageActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Load subtasks từ Firebase (for VIEW/EDIT mode) - Optimized
     */
    private void loadSubtasksFromFirebase(String contentId) {
        loadSubtasksFromFirebase(contentId, null);
    }
    
    /**
     * Load subtasks từ Firebase với callback
     */
    private void loadSubtasksFromFirebase(String contentId, Runnable onComplete) {
        if (contentId == null || contentId.isEmpty()) {
            android.util.Log.e("ContentManage", "Content ID null hoặc rỗng");
            return;
        }

        if (subtasksContainer == null) {
            android.util.Log.e("ContentManage", "subtasksContainer is null");
            return;
        }

        android.util.Log.d("ContentManage", "Đang tải subtasks cho content: " + contentId);

        subtaskRef.orderByChild("ContentId").equalTo(contentId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        existingSubtasks.clear();
                        subtasksContainer.removeAllViews();
                        subtaskChanges.clear();  // Clear old changes before loading new subtasks

                        android.util.Log.d("ContentManage", "Snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());

                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            int count = 0;
                            for (DataSnapshot subtaskSnapshot : snapshot.getChildren()) {
                                try {
                                    SubTask subtask = subtaskSnapshot.getValue(SubTask.class);
                                    if (subtask != null && subtask.getTitle() != null) {
                                        existingSubtasks.add(subtask);
                                        displaySubtask(subtask);
                                        count++;
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("ContentManage", "Lỗi parse subtask: " + e.getMessage());
                                }
                            }
                            android.util.Log.d("ContentManage", "Đã tải " + count + " subtask(s)");
                        } else {
                            android.util.Log.d("ContentManage", "Không có subtasks cho content này");
                        }
                        
                        // Call callback if provided
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ContentManage", "Lỗi xử lý subtasks: " + e.getMessage());
                        Toast.makeText(ContentManageActivity.this,
                            "Lỗi hiển thị subtasks", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("ContentManage", "Lỗi Firebase: " + error.getMessage());
                    Toast.makeText(ContentManageActivity.this,
                        "Lỗi tải subtasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Display subtask trong VIEW/EDIT mode
     */
    private void displaySubtask(SubTask subtask) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View subtaskView = inflater.inflate(R.layout.item_subtask_editable, subtasksContainer, false);

        EditText editSubtaskTitle = subtaskView.findViewById(R.id.editSubtaskTitle);
        EditText editAssign = subtaskView.findViewById(R.id.editAssign);
        EditText editSubtaskDeadline = subtaskView.findViewById(R.id.editSubtaskDeadline);
        CheckBox checkboxDone = subtaskView.findViewById(R.id.checkboxSubtask);
        ImageButton btnDelete = subtaskView.findViewById(R.id.btnDeleteSubtask);
        LinearLayout layoutAssign = subtaskView.findViewById(R.id.layoutAssign);

        // Fill data
        if (subtask.getTitle() != null) editSubtaskTitle.setText(subtask.getTitle());
        if (subtask.getDeadline() != null) editSubtaskDeadline.setText(subtask.getDeadline());
        checkboxDone.setChecked(subtask.getIsDone());

        // Store selected userId in tag for later updates
        subtaskView.setTag(subtask.getUserId());

        // Find and display assignee name - Use cached userList first for performance
        if (subtask.getUserId() != null && !subtask.getUserId().isEmpty()) {
            // Check cache first
            boolean foundInCache = false;
            for (UserData user : userList) {
                if (user.userId.equals(subtask.getUserId())) {
                    editAssign.setText(user.fullName);
                    foundInCache = true;
                    break;
                }
            }

            // If not in cache, query Firebase (only as fallback)
            if (!foundInCache) {
                userRef.child(subtask.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fullName = snapshot.child("FullName").getValue(String.class);
                            if (fullName != null) {
                                editAssign.setText(fullName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ContentManage", "Lỗi tải user: " + error.getMessage());
                    }
                });
            }
        }

        // Initialize change tracking for this subtask
        if (currentMode == Mode.EDIT && subtask.getSubTaskId() != null) {
            if (!subtaskChanges.containsKey(subtask.getSubTaskId())) {
                SubtaskChange change = new SubtaskChange(subtask);
                change.originalUserName = editAssign.getText().toString();
                change.newUserName = editAssign.getText().toString();
                subtaskChanges.put(subtask.getSubTaskId(), change);
            }
        }

        // Set read-only in VIEW mode
        if (currentMode == Mode.VIEW) {
            editSubtaskTitle.setEnabled(false);
            editAssign.setEnabled(false);
            editSubtaskDeadline.setEnabled(false);
            checkboxDone.setEnabled(false);
            btnDelete.setVisibility(View.GONE);
            layoutAssign.setClickable(false);
            layoutAssign.setFocusable(false);
        } else {
            // EDIT or CREATE mode: allow modifications
            editSubtaskTitle.setEnabled(true);
            editAssign.setEnabled(true);
            editSubtaskDeadline.setEnabled(true);
            checkboxDone.setEnabled(true);
            btnDelete.setVisibility(View.VISIBLE);
            layoutAssign.setClickable(true);
            layoutAssign.setFocusable(true);

            // Delete button listener - Mark as deleted (don't delete immediately)
            btnDelete.setOnClickListener(v -> {
                if (subtask.getSubTaskId() != null && !subtask.getSubTaskId().isEmpty()) {
                    SubtaskChange change = subtaskChanges.get(subtask.getSubTaskId());
                    if (change != null) {
                        change.isDeleted = true;
                    }
                }
                subtasksContainer.removeView(subtaskView);
                existingSubtasks.remove(subtask);
                Toast.makeText(ContentManageActivity.this, "Subtask sẽ bị xóa khi bạn nhấn Lưu", Toast.LENGTH_SHORT).show();
            });

            // Checkbox listener - Track change in memory
            checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (subtask.getSubTaskId() != null && !subtask.getSubTaskId().isEmpty()) {
                    SubtaskChange change = subtaskChanges.get(subtask.getSubTaskId());
                    if (change != null) {
                        change.newIsDone = isChecked;
                    }
                }
            });

            // Title change listener - Track change in memory
            editSubtaskTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (subtask.getSubTaskId() != null && !subtask.getSubTaskId().isEmpty()) {
                        SubtaskChange change = subtaskChanges.get(subtask.getSubTaskId());
                        if (change != null) {
                            change.newTitle = s.toString().trim();
                        }
                    }
                }
            });

            // Assign popup listener - Track change in memory
            View.OnClickListener assignListener = v -> {
                showAssigneePopup(editAssign, selectedUserId -> {
                    // Update UI
                    for (UserData user : userList) {
                        if (user.userId.equals(selectedUserId)) {
                            editAssign.setText(user.fullName);
                            subtaskView.setTag(selectedUserId);
                            
                            // Track change in memory
                            if (subtask.getSubTaskId() != null && !subtask.getSubTaskId().isEmpty()) {
                                SubtaskChange change = subtaskChanges.get(subtask.getSubTaskId());
                                if (change != null) {
                                    change.newUserId = selectedUserId;
                                    change.newUserName = user.fullName;
                                }
                            }
                            break;
                        }
                    }
                });
            };
            layoutAssign.setOnClickListener(assignListener);
            editAssign.setOnClickListener(assignListener);

            // Deadline picker listener - Track change in memory
            editSubtaskDeadline.setOnClickListener(v -> {
                showDateTimePicker(new DateTimeCallback() {
                    @Override
                    public void onDateTimeSelected(String dateTime) {
                        editSubtaskDeadline.setText(dateTime);
                        
                        // Track change in memory
                        if (subtask.getSubTaskId() != null && !subtask.getSubTaskId().isEmpty()) {
                            SubtaskChange change = subtaskChanges.get(subtask.getSubTaskId());
                            if (change != null) {
                                change.newDeadline = dateTime;
                            }
                        }
                    }
                });
            });
        }

        subtasksContainer.addView(subtaskView);
    }

    /**
     * Toggle giữa VIEW và EDIT mode
     */
    private void toggleEditMode() {
        if (currentMode == Mode.VIEW) {
            currentMode = Mode.EDIT;
            updateUIForMode();
        } else if (currentMode == Mode.EDIT) {
            // Save any pending draft subtask before saving content
            if (currentDraftSubtask != null && isDraftSubtaskValid) {
                saveDraftSubtask();
            }
            
            // Save changes to Firebase
            // Mode will be switched to VIEW in the callback after save completes
            updateContentToFirebase();
        }
    }

    /**
     * Update UI theo mode hiện tại
     */
    private void updateUIForMode() {
        updateUIForMode(false);
    }
    
    /**
     * Update UI theo mode hiện tại với option skip reload subtasks
     */
    private void updateUIForMode(boolean skipSubtasksReload) {
        switch (currentMode) {
            case CREATE:
                // Header
                if (tvHeaderTitle != null) tvHeaderTitle.setText("Tạo Content Mới");
                if (btnEditSave != null) btnEditSave.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setVisibility(View.VISIBLE);

                // Enable all fields
                setFieldsEnabled(true);

                // Show layoutContentType (custom popup), hide spinnerType
                if (layoutContentType != null) layoutContentType.setVisibility(View.VISIBLE);
                if (spinnerType != null) spinnerType.setVisibility(View.GONE);
                break;

            case VIEW:
                // Header
                if (tvHeaderTitle != null) tvHeaderTitle.setText("Xem Content");
                if (btnEditSave != null) {
                    btnEditSave.setVisibility(View.VISIBLE);
                    btnEditSave.setText("Sửa");
                }
                if (btnSubmit != null) btnSubmit.setVisibility(View.GONE);

                // Disable all fields
                setFieldsEnabled(false);

                // Hide layoutContentType, show layoutSpinnerType
                if (layoutContentType != null) layoutContentType.setVisibility(View.GONE);
                if (layoutSpinnerType != null) layoutSpinnerType.setVisibility(View.VISIBLE);

                // Update status spinner for VIEW mode
                updateStatusSpinner(false);

                // Hide Add Subtask button
                if (btnAddSubtask != null) btnAddSubtask.setVisibility(View.GONE);

                // Reload subtasks to update UI state (only if not skipped)
                if (!skipSubtasksReload) {
                    reloadSubtasksUI();
                }
                break;

            case EDIT:
                // Header
                if (tvHeaderTitle != null) tvHeaderTitle.setText("Chỉnh sửa Content");
                if (btnEditSave != null) {
                    btnEditSave.setVisibility(View.VISIBLE);
                    btnEditSave.setText("Lưu");
                }
                if (btnSubmit != null) btnSubmit.setVisibility(View.GONE);

                // Enable fields (except locked status)
                setFieldsEnabled(true);
                if (isLockedStatus(currentStatus)) {
                    spinnerStatus.setEnabled(false);
                }

                // Show layoutContentType (custom popup), hide layoutSpinnerType - same as CREATE mode
                if (layoutContentType != null) layoutContentType.setVisibility(View.VISIBLE);
                if (layoutSpinnerType != null) layoutSpinnerType.setVisibility(View.GONE);

                // Update status spinner for EDIT mode
                updateStatusSpinner(true);

                // Show Add Subtask button
                if (btnAddSubtask != null) btnAddSubtask.setVisibility(View.VISIBLE);

                // Reload subtasks to update UI state (only if not skipped)
                if (!skipSubtasksReload) {
                    reloadSubtasksUI();
                }
                break;
        }
    }

    /**
     * Reload subtasks UI to reflect current mode (VIEW/EDIT)
     */
    private void reloadSubtasksUI() {
        if (existingSubtasks.isEmpty()) {
            return;
        }

        // Clear previous changes when switching modes
        if (currentMode == Mode.VIEW) {
            subtaskChanges.clear();
        }

        subtasksContainer.removeAllViews();
        for (SubTask subtask : existingSubtasks) {
            displaySubtask(subtask);
        }
    }

    /**
     * Enable/disable input fields
     */
    private void setFieldsEnabled(boolean enabled) {
        editTitle.setEnabled(enabled);
        editChannel.setEnabled(enabled);
        editTags.setEnabled(enabled);
        editDate.setEnabled(enabled);
        editTime.setEnabled(enabled);
        spinnerStatus.setEnabled(enabled);
        editAttachment.setEnabled(enabled);
        editEditorLink.setEnabled(enabled);
        

        if (spinnerType != null) spinnerType.setEnabled(enabled);
        if (editContentType != null) editContentType.setEnabled(enabled);
    }

    /**
     * Update status spinner theo mode
     */
    private void updateStatusSpinner(boolean isEditMode) {
        ArrayAdapter<CharSequence> adapter;
        if (isEditMode) {
            // EDIT mode: only To do / In progress / Done
            adapter = ArrayAdapter.createFromResource(this,
                R.array.employee_content_status_options, android.R.layout.simple_spinner_item);
        } else {
            // VIEW mode: all statuses
            adapter = ArrayAdapter.createFromResource(this,
                R.array.full_content_status_options, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Save current selection
        String currentSelection = spinnerStatus.getSelectedItem() != null ?
            spinnerStatus.getSelectedItem().toString() : "";

        spinnerStatus.setAdapter(adapter);

        // Restore selection if possible
        if (!currentSelection.isEmpty()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(currentSelection)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Check if status is locked (Admin-only)
     */
    private boolean isLockedStatus(String status) {
        return status.equalsIgnoreCase("Accepted") ||
               status.equalsIgnoreCase("Reject") ||
               status.equalsIgnoreCase("Scheduled") ||
               status.equalsIgnoreCase("Published") ||
               status.equalsIgnoreCase("Locked");
    }

    /**
     * Update content vào Firebase (for EDIT mode) - Batch update content + subtasks
     */
    private void updateContentToFirebase() {
        String title = editTitle.getText().toString().trim();
        
        // Get type from selectedContentType and customContentType (same logic as createContent)
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

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        String combinedTime = dateStr + " " + timeStr;

        // Update content data
        Map<String, Object> contentUpdates = new HashMap<>();
        contentUpdates.put("Title", title);
        contentUpdates.put("Type", type);
        contentUpdates.put("Channel", channel);
        contentUpdates.put("Tag", tags);
        contentUpdates.put("ModifiedTime", combinedTime);
        contentUpdates.put("Status", status);
        contentUpdates.put("Url", attachment);
        contentUpdates.put("EditorLink", editorLink);

        contentRef.child(contentID).updateChildren(contentUpdates)
            .addOnSuccessListener(aVoid -> {
                // Update subtasks and send notifications
                updateSubtasksToFirebase(title);
                currentStatus = status;
                
                // Switch to VIEW mode after saving
                currentMode = Mode.VIEW;
                
                // Reload subtasks from Firebase to get fresh data, then update UI
                loadSubtasksFromFirebase(contentID, () -> {
                    // After subtasks loaded, update UI to VIEW mode (skip subtasks reload)
                    updateUIForMode(true);
                    Toast.makeText(ContentManageActivity.this, "Đã lưu thành công!", Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ContentManageActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Update subtasks changes to Firebase and send notifications
     */
    private void updateSubtasksToFirebase(String contentTitle) {
        android.util.Log.d("ContentManage", "updateSubtasksToFirebase called");
        android.util.Log.d("ContentManage", "subtaskChanges count: " + subtaskChanges.size());
        android.util.Log.d("ContentManage", "savedSubtasks count: " + savedSubtasks.size());
        
        int changesCount = 0;
        
        // 1. Process existing subtask changes (edit/delete)
        for (SubtaskChange change : subtaskChanges.values()) {
            if (!change.hasChanges()) {
                continue;
            }
            
            changesCount++;
            
            if (change.isDeleted) {
                // Delete subtask from Firebase
                subtaskRef.child(change.subtaskId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Send notification to original assignee
                        sendSubtaskRemovalNotification(change.originalUserId, contentTitle, change.originalTitle);
                    });
            } else {
                // Update subtask fields
                Map<String, Object> subtaskUpdates = new HashMap<>();
                subtaskUpdates.put("Title", change.newTitle);
                subtaskUpdates.put("UserId", change.newUserId);
                subtaskUpdates.put("IsDone", change.newIsDone);
                subtaskUpdates.put("Deadline", change.newDeadline);
                
                subtaskRef.child(change.subtaskId).updateChildren(subtaskUpdates)
                    .addOnSuccessListener(aVoid -> {
                        // Send notifications based on what changed
                        
                        // 1. User assignment changed
                        if (change.hasUserChange()) {
                            // Notify old user they were removed
                            sendSubtaskRemovalNotification(change.originalUserId, contentTitle, change.newTitle);
                            // Notify new user they were assigned
                            sendSubtaskAssignmentNotification(change.newUserId, contentTitle, change.newTitle);
                        } else {
                            // 2. Other fields changed (title, status, deadline)
                            String changeDetails = buildChangeMessage(change);
                            sendSubtaskUpdateNotification(change.newUserId, contentTitle, change.newTitle, changeDetails);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ContentManage", "Lỗi cập nhật subtask: " + e.getMessage());
                    });
            }
        }
        
        // 2. Save new subtasks added in EDIT mode
        // Note: Check savedSubtasks list instead of currentMode (mode may already be switched)
        if (!savedSubtasks.isEmpty()) {
            android.util.Log.d("ContentManage", "Saving " + savedSubtasks.size() + " new subtasks");
            for (SubtaskData subtaskData : savedSubtasks) {
                changesCount++;
                android.util.Log.d("ContentManage", "Processing subtask: " + subtaskData.title + ", assignee: " + subtaskData.assignee);
                
                // Find userId from assignee name
                findUserIdByName(subtaskData.assignee, userId -> {
                    android.util.Log.d("ContentManage", "findUserIdByName callback - userId: " + userId + " for assignee: " + subtaskData.assignee);
                    if (userId == null || userId.isEmpty()) {
                        android.util.Log.e("ContentManage", "KHÔNG TÌM THẤY userId cho assignee: " + subtaskData.assignee);
                        return;
                    }
                    
                    // Generate subtask ID
                    String subtaskId = subtaskRef.push().getKey();
                    if (subtaskId == null) {
                        android.util.Log.e("ContentManage", "KHÔNG tạo được subtaskId");
                        return;
                    }
                    
                    android.util.Log.d("ContentManage", "Saving subtask to Firebase - ID: " + subtaskId);
                    android.util.Log.d("ContentManage", "🔍 Constructor params - subtaskId: " + subtaskId);
                    android.util.Log.d("ContentManage", "🔍 Constructor params - contentID: " + contentID);
                    android.util.Log.d("ContentManage", "🔍 Constructor params - userId: " + userId);
                    android.util.Log.d("ContentManage", "🔍 Constructor params - title: " + subtaskData.title);
                    android.util.Log.d("ContentManage", "🔍 Constructor params - deadline: " + subtaskData.deadline);
                    
                    // Create SubTask object
                    SubTask newSubtask = new SubTask(
                        subtaskId,
                        contentID,
                        userId,
                        subtaskData.title,
                        false,
                        subtaskData.deadline
                    );
                    
                    android.util.Log.d("ContentManage", "🔍 After constructor - newSubtask.ContentId = " + newSubtask.getContentId());
                    android.util.Log.d("ContentManage", "🔍 After constructor - newSubtask.Title = " + newSubtask.getTitle());
                    
                    // Save to Firebase
                    subtaskRef.child(subtaskId).setValue(newSubtask)
                        .addOnSuccessListener(aVoid -> {
                            android.util.Log.d("ContentManage", "✅ Subtask saved successfully: " + subtaskData.title);
                            // Send notification to assigned user
                            sendSubtaskNotification(userId, contentTitle, subtaskData.title);
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("ContentManage", "❌ Lỗi lưu subtask mới: " + e.getMessage());
                        });
                });
            }
        }
        
        // Clear changes after save
        subtaskChanges.clear();
        savedSubtasks.clear();
    }

    /**
     * Build change message for subtask update notification
     */
    private String buildChangeMessage(SubtaskChange change) {
        List<String> changes = new ArrayList<>();
        
        if (!change.originalTitle.equals(change.newTitle)) {
            changes.add("tên: '" + change.originalTitle + "' → '" + change.newTitle + "'");
        }
        if (change.originalIsDone != change.newIsDone) {
            changes.add("trạng thái: " + (change.newIsDone ? "Hoàn thành" : "Chưa hoàn thành"));
        }
        if (!change.originalDeadline.equals(change.newDeadline)) {
            changes.add("deadline: " + change.newDeadline);
        }
        
        return String.join(", ", changes);
    }

    /**
     * Send notification when user is removed from subtask
     */
    private void sendSubtaskRemovalNotification(String userId, String contentTitle, String subtaskTitle) {
        String notificationId = notificationRef.push().getKey();
        
        if (notificationId == null) {
            android.util.Log.e("ContentManage", "Failed to generate notification ID");
            return;
        }
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("NotiId", notificationId);
        notification.put("UserId", userId);
        notification.put("Type", "Subtask Removal");
        notification.put("Message", "Bạn đã được gỡ khỏi subtask: " + subtaskTitle + " trong content: " + contentTitle);
        notification.put("IsRead", false);
        notification.put("CreatedTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new java.util.Date()));
        
        notificationRef.child(notificationId).setValue(notification);
    }

    /**
     * Send notification when user is assigned to subtask
     */
    private void sendSubtaskAssignmentNotification(String userId, String contentTitle, String subtaskTitle) {
        String notificationId = notificationRef.push().getKey();
        
        if (notificationId == null) {
            android.util.Log.e("ContentManage", "Failed to generate notification ID");
            return;
        }
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("NotiId", notificationId);
        notification.put("UserId", userId);
        notification.put("Type", "Task Assignment");
        notification.put("Message", "Bạn được giao subtask: " + subtaskTitle + " trong content: " + contentTitle);
        notification.put("IsRead", false);
        notification.put("CreatedTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new java.util.Date()));
        
        notificationRef.child(notificationId).setValue(notification);
    }

    /**
     * Send notification when subtask is updated
     */
    private void sendSubtaskUpdateNotification(String userId, String contentTitle, String subtaskTitle, String changeDetails) {
        String notificationId = notificationRef.push().getKey();
        
        if (notificationId == null) {
            android.util.Log.e("ContentManage", "Failed to generate notification ID");
            return;
        }
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("NotiId", notificationId);
        notification.put("UserId", userId);
        notification.put("Type", "Subtask Update");
        notification.put("Message", "Subtask '" + subtaskTitle + "' trong content '" + contentTitle + "' đã được cập nhật: " + changeDetails);
        notification.put("IsRead", false);
        notification.put("CreatedTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new java.util.Date()));
        
        notificationRef.child(notificationId).setValue(notification);
    }

    /**
     * Handle back button press with unsaved changes check
     */
    private void handleBackPress() {
        if (currentMode == Mode.EDIT && hasUnsavedChanges()) {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thay đổi chưa lưu")
                .setMessage("Bạn có thay đổi chưa lưu. Bạn có muốn thoát mà không lưu?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    // Discard changes
                    subtaskChanges.clear();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Lưu và thoát", (dialog, which) -> {
                    // Save and exit
                    updateContentToFirebase();
                    new android.os.Handler().postDelayed(() -> finish(), 1000);
                })
                .show();
        } else {
            finish();
        }
    }

    /**
     * Check if there are unsaved changes
     */
    private boolean hasUnsavedChanges() {
        // Check content fields
        // (Simple check - compare with original values if needed)
        
        // Check subtask changes
        for (SubtaskChange change : subtaskChanges.values()) {
            if (change.hasChanges()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void onBackPressed() {
        handleBackPress();
    }

    /**
     * Static method để mở activity này (for compatibility)
     */
    public static void start(android.content.Context context, String contentId) {
        android.content.Intent intent = new android.content.Intent(context, ContentManageActivity.class);
        intent.putExtra("CONTENT_ID", contentId);
        context.startActivity(intent);
    }
}
