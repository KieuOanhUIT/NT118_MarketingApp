package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * EditContentActivity - Màn hình xem và chỉnh sửa thông tin content
 * 
 * Chức năng:
 * - Hiển thị thông tin content (mặc định ở chế độ chỉ xem - không chỉnh sửa được)
 * - Cho phép bật/tắt chế độ chỉnh sửa qua nút Edit/Save
 * - Lưu thay đổi khi người dùng bấm Save
 */
public class EditContentActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private Button btnEditSave;
    
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
}
