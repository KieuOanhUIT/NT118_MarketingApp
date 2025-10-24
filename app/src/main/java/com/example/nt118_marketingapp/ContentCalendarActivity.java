package com.example.nt118_marketingapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * ContentCalendarActivity - Màn hình hiển thị lịch đăng content
 * 
 * Chức năng:
 * - Hiển thị lưới lịch 24h x 7 ngày
 * - Xem số lượng content theo giờ và ngày
 * - Chọn ô và thêm content mới
 * - Chuyển tuần trước/sau
 */
public class ContentCalendarActivity extends AppCompatActivity {

    // UI Components
    private TextView tvWeekRange;
    private Button btnAddContent;
    private ImageButton btnPreviousWeek, btnNextWeek;
    private LinearLayout dayHeadersContainer;
    private LinearLayout calendarGridContainer;

    // Data
    private ContentCalendarRepository repository;
    private LocalDate currentWeekStart; // Thứ 2 của tuần hiện tại
    private View selectedCell = null;   // Ô đang được chọn
    private LocalDate selectedDay = null;
    private int selectedHour = -1;

    // Constants
    private static final int CELL_WIDTH_DP = 80;
    private static final int CELL_HEIGHT_DP = 60;
    private static final int HOUR_COLUMN_WIDTH_DP = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_calendar);

        // Initialize components
        initViews();

        // Setup repository
        repository = ContentCalendarRepository.getInstance();

        // Set current week to this Monday
        currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        // Setup listeners
        setupListeners();

        // Load calendar
        loadCalendar();
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        tvWeekRange = findViewById(R.id.tvWeekRange);
        btnAddContent = findViewById(R.id.btnAddContent);
        btnPreviousWeek = findViewById(R.id.btnPreviousWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        dayHeadersContainer = findViewById(R.id.dayHeadersContainer);
        calendarGridContainer = findViewById(R.id.calendarGridContainer);
    }

    /**
     * Thiết lập các event listeners
     */
    private void setupListeners() {
        // Previous week button
        btnPreviousWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            loadCalendar();
        });

        // Next week button
        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            loadCalendar();
        });

        // Add content button
        btnAddContent.setOnClickListener(v -> {
            if (selectedDay != null && selectedHour >= 0) {
                // Navigate to Create Content with selected date and hour
                Intent intent = new Intent(this, CreateContentActivity.class);
                intent.putExtra("DATE", selectedDay.toString());
                intent.putExtra("HOUR", selectedHour);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn một ô trên lịch", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Set initial state - button disabled
        updateAddContentButton();
    }

    /**
     * Load và hiển thị lịch cho tuần hiện tại
     */
    private void loadCalendar() {
        // Update week range text
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        tvWeekRange.setText(currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter));

        // Clear existing views
        dayHeadersContainer.removeAllViews();
        calendarGridContainer.removeAllViews();
        selectedCell = null;
        selectedDay = null;
        selectedHour = -1;
        
        // Update button state
        updateAddContentButton();

        // Create day headers
        createDayHeaders();

        // Create calendar grid
        createCalendarGrid();
    }

    /**
     * Tạo header cho các ngày trong tuần
     */
    private void createDayHeaders() {
        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd");

        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeekStart.plusDays(i);
            String dayLabel = dayLabels[i] + " " + day.format(dateFormatter);

            TextView dayHeader = new TextView(this);
            dayHeader.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(CELL_WIDTH_DP),
                    dpToPx(50)
            ));
            dayHeader.setText(dayLabel);
            dayHeader.setGravity(Gravity.CENTER);
            dayHeader.setTextSize(13);
            dayHeader.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
            dayHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundCard));
            dayHeader.setPadding(4, 8, 4, 8);

            dayHeadersContainer.addView(dayHeader);
        }
    }

    /**
     * Tạo lưới lịch 24 giờ x 7 ngày
     */
    private void createCalendarGrid() {
        // Get content counts for the week
        List<ContentCount> contentCounts = repository.getContentCountsForWeek(currentWeekStart);

        // Create 24 rows (one for each hour)
        for (int hour = 0; hour < 24; hour++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Hour label column
            TextView hourLabel = new TextView(this);
            hourLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(HOUR_COLUMN_WIDTH_DP),
                    dpToPx(CELL_HEIGHT_DP)
            ));
            hourLabel.setText(String.format(Locale.getDefault(), "%02d:00", hour));
            hourLabel.setGravity(Gravity.CENTER);
            hourLabel.setTextSize(12);
            hourLabel.setTextColor(ContextCompat.getColor(this, R.color.textSecondary));
            hourLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.inputBackground));
            rowLayout.addView(hourLabel);

            // Create 7 cells (one for each day)
            for (int day = 0; day < 7; day++) {
                LocalDate currentDay = currentWeekStart.plusDays(day);
                int currentHour = hour;
                int count = repository.getContentCount(currentDay, currentHour);

                View cell = createCalendarCell(currentDay, currentHour, count);
                rowLayout.addView(cell);
            }

            calendarGridContainer.addView(rowLayout);
        }
    }

    /**
     * Tạo một ô trong lịch
     */
    private View createCalendarCell(LocalDate day, int hour, int count) {
        TextView cell = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(CELL_WIDTH_DP),
                dpToPx(CELL_HEIGHT_DP)
        );
        params.setMargins(2, 2, 2, 2);
        cell.setLayoutParams(params);

        // Set content count text
        cell.setText(String.valueOf(count));
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(16);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);

        // Set background based on count
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dpToPx(8));

        if (count > 0) {
            // Has content - green border with white background
            background.setColor(ContextCompat.getColor(this, R.color.backgroundCard));
            background.setStroke(dpToPx(2), ContextCompat.getColor(this, R.color.colorSecondary));
            cell.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
        } else {
            // No content - light red background
            background.setColor(Color.parseColor("#FFEBEE")); // Light red
            background.setStroke(dpToPx(1), ContextCompat.getColor(this, R.color.inputBorder));
            cell.setTextColor(ContextCompat.getColor(this, R.color.deadlineOverdue));
        }

        cell.setBackground(background);

        // Set click listener
        cell.setOnClickListener(v -> onCellClicked(cell, day, hour));

        // Store data in tag
        cell.setTag(new CellData(day, hour));

        return cell;
    }

    /**
     * Xử lý khi click vào một ô
     */
    private void onCellClicked(View cell, LocalDate day, int hour) {
        // Nếu click vào ô đang được chọn → Hủy chọn
        if (selectedCell == cell) {
            // Deselect
            CellData cellData = (CellData) cell.getTag();
            int count = repository.getContentCount(cellData.day, cellData.hour);
            updateCellAppearance((TextView) cell, count, false);
            
            selectedCell = null;
            selectedDay = null;
            selectedHour = -1;
            
            updateAddContentButton();
            Toast.makeText(this, "Đã hủy chọn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Deselect previous cell
        if (selectedCell != null) {
            CellData prevData = (CellData) selectedCell.getTag();
            int prevCount = repository.getContentCount(prevData.day, prevData.hour);
            updateCellAppearance((TextView) selectedCell, prevCount, false);
        }

        // Select new cell
        selectedCell = cell;
        selectedDay = day;
        selectedHour = hour;

        int count = repository.getContentCount(day, hour);
        updateCellAppearance((TextView) cell, count, true);
        
        // Update button state
        updateAddContentButton();

        // Show toast
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String message = String.format("Đã chọn: %s - %02d:00 (%d content)",
                day.format(formatter), hour, count);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cập nhật giao diện của ô (selected/unselected)
     */
    private void updateCellAppearance(TextView cell, int count, boolean isSelected) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dpToPx(8));

        if (isSelected) {
            // Selected state - thick border and light background
            if (count > 0) {
                background.setColor(Color.parseColor("#E8F5E9")); // Light green
                background.setStroke(dpToPx(4), ContextCompat.getColor(this, R.color.colorSecondary));
            } else {
                background.setColor(Color.parseColor("#FFCDD2")); // Lighter red
                background.setStroke(dpToPx(4), ContextCompat.getColor(this, R.color.deadlineOverdue));
            }
        } else {
            // Unselected state - normal appearance
            if (count > 0) {
                background.setColor(ContextCompat.getColor(this, R.color.backgroundCard));
                background.setStroke(dpToPx(2), ContextCompat.getColor(this, R.color.colorSecondary));
            } else {
                background.setColor(Color.parseColor("#FFEBEE"));
                background.setStroke(dpToPx(1), ContextCompat.getColor(this, R.color.inputBorder));
            }
        }

        cell.setBackground(background);
    }
    
    /**
     * Cập nhật trạng thái nút "Thêm Content"
     */
    private void updateAddContentButton() {
        if (selectedDay != null && selectedHour >= 0) {
            // Có ô được chọn - nút enabled
            btnAddContent.setEnabled(true);
            btnAddContent.setAlpha(1.0f);
            btnAddContent.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
        } else {
            // Không có ô được chọn - nút disabled
            btnAddContent.setEnabled(false);
            btnAddContent.setAlpha(0.5f);
            btnAddContent.setBackgroundTintList(getColorStateList(R.color.inputBorder));
        }
    }

    /**
     * Convert dp to pixels
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Helper class để lưu thông tin ô
     */
    private static class CellData {
        LocalDate day;
        int hour;

        CellData(LocalDate day, int hour) {
            this.day = day;
            this.hour = hour;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload calendar when returning from Create Content activity
        loadCalendar();
    }
}
