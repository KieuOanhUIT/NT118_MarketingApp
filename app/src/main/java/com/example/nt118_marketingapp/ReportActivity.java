
package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportItem> reportList = new ArrayList<>();
    private BarChart barChart;
    HorizontalBarChart horizontalBarChart;
    private DatabaseReference dbRef;
    EditText edtFromDate, edtToDate;
    Button btnApply;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        recyclerView = findViewById(R.id.recyclerReport);
        barChart = findViewById(R.id.barChart);

        edtFromDate = findViewById(R.id.edtFromDate);
        edtToDate = findViewById(R.id.edtToDate);
        btnApply = findViewById(R.id.btnApply);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference("Content");

        btnApply.setOnClickListener(v -> {
            String fromStr = edtFromDate.getText().toString().trim();
            String toStr = edtToDate.getText().toString().trim();

            setupBarChart(fromStr, toStr);
            setupHorizontalBarChart(fromStr, toStr);



            // Nếu người dùng để trống -> mặc định cả năm
            if (fromStr.isEmpty() || toStr.isEmpty()) {
                loadReportData(1, 12);
                return;
            }

            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat firebaseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                Calendar fromCal = Calendar.getInstance();
                Calendar toCal = Calendar.getInstance();

                fromCal.setTime(displayFormat.parse(fromStr));
                toCal.setTime(displayFormat.parse(toStr));

                int fromMonth = fromCal.get(Calendar.MONTH) + 1;
                int toMonth = toCal.get(Calendar.MONTH) + 1;

                loadReportData(fromMonth, toMonth);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 📅 Bắt sự kiện chọn ngày
        edtFromDate.setOnClickListener(v -> showDatePicker(edtFromDate));
        edtToDate.setOnClickListener(v -> showDatePicker(edtToDate));


    }

    private void loadReportData(int fromMonth, int toMonth) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Map<String, Integer>> monthChannelCount = new HashMap<>();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    String status = postSnap.child("Status").getValue(String.class);
                    String channel = postSnap.child("Channel").getValue(String.class);
                    String timeStr = postSnap.child("CreatedTime").getValue(String.class);

                    if (status == null || channel == null || timeStr == null) continue;
                    if (!status.equals("Published")) continue;

                    try {
                        Date date = format.parse(timeStr);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int month = cal.get(Calendar.MONTH) + 1; // 0-based → +1

                        if (month >= fromMonth && month <= toMonth) {
                            String monthStr = String.valueOf(month);
                            monthChannelCount.putIfAbsent(monthStr, new HashMap<>());
                            Map<String, Integer> channelMap = monthChannelCount.get(monthStr);

                            int count = channelMap.getOrDefault(channel, 0);
                            channelMap.put(channel, count + 1);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                updateUI(monthChannelCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateUI(Map<String, Map<String, Integer>> monthChannelCount) {
        reportList.clear();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (String month : monthChannelCount.keySet()) {
            Map<String, Integer> channelMap = monthChannelCount.get(month);
            for (Map.Entry<String, Integer> entry : channelMap.entrySet()) {
                String channel = entry.getKey();
                int count = entry.getValue();

                // thêm vào list cho RecyclerView
                reportList.add(new ReportItem("Tháng " + month, channel, count));

                // nếu chỉ muốn hiển thị tổng số bài của tháng trong biểu đồ:
                entries.add(new BarEntry(index, count));
                labels.add("T" + month + "-" + channel);
                index++;
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void setupBarChart(String fromStr, String toStr) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Content");


        // Parse ngày bắt đầu và kết thúc (giống logic bạn đã làm ở dưới)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date startDate = null;
        Date endDate = null;
        try {
            if (!fromStr.isEmpty() && !toStr.isEmpty()) {
                startDate = sdf.parse(fromStr);
                endDate = sdf.parse(toStr);
            }
        } catch (ParseException e) { e.printStackTrace(); }

        final Date finalStartDate = startDate;
        final Date finalEndDate = endDate;

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, Integer> completedCount = new HashMap<>();
                Map<Integer, Integer> remainingCount = new HashMap<>();

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    String status = postSnap.child("Status").getValue(String.class);
                    String timeStr = postSnap.child("CreatedTime").getValue(String.class);
                    if (status == null || timeStr == null) continue;

                    try {
                        Date date = format.parse(timeStr);

                        // --- THÊM ĐOẠN CHECK NÀY ---
                        if (finalStartDate != null && finalEndDate != null) {
                            if (date.before(finalStartDate) || date.after(finalEndDate)) {
                                continue; // Bỏ qua nếu nằm ngoài khoảng chọn
                            }
                        }
                        // -----------------------------


                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int month = cal.get(Calendar.MONTH) + 1;

                        if (status.equals("Published")) {
                            completedCount.put(month, completedCount.getOrDefault(month, 0) + 1);
                        } else {
                            remainingCount.put(month, remainingCount.getOrDefault(month, 0) + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Chuẩn bị dữ liệu cho BarChart
                List<BarEntry> completedEntries = new ArrayList<>();
                List<BarEntry> remainingEntries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                Set<Integer> months = new TreeSet<>();
                months.addAll(completedCount.keySet());
                months.addAll(remainingCount.keySet());

                int index = 0;
                for (int month : months) {
                    completedEntries.add(new BarEntry(index, completedCount.getOrDefault(month, 0)));
                    remainingEntries.add(new BarEntry(index, remainingCount.getOrDefault(month, 0)));
                    labels.add("Tháng " + month);
                    index++;
                }

                BarDataSet set1 = new BarDataSet(completedEntries, "Hoàn thành");
                set1.setColor(Color.rgb(99, 132, 255));

                BarDataSet set2 = new BarDataSet(remainingEntries, "Chưa hoàn thành");
                set2.setColor(Color.rgb(255, 99, 132));

                BarData data = new BarData(set1, set2);
                data.setBarWidth(0.3f);
                barChart.setData(data);

                barChart.getDescription().setEnabled(false);

                // Nhóm cột
                barChart.groupBars(-0.5f, 0.2f, 0f);

                // Trục X
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setGranularity(1f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                barChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupHorizontalBarChart(String fromStr, String toStr) {
        DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("Content");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();

        try {
            fromCal.setTime(Objects.requireNonNull(format.parse(fromStr)));
            toCal.setTime(Objects.requireNonNull(format.parse(toStr)));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 1️⃣ Lấy danh sách userId → FullName
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnap) {
                Map<String, String> userMap = new HashMap<>();
                for (DataSnapshot snap : userSnap.getChildren()) {
                    String userId = snap.getKey();
                    String fullName = snap.child("FullName").getValue(String.class);
                    if (userId != null && fullName != null) {
                        userMap.put(userId, fullName);
                    }
                }

                // 2️⃣ Lấy dữ liệu Content và đếm số bài Published
                contentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot contentSnap) {
                        Map<String, Integer> userPublishedCount = new HashMap<>();

                        for (DataSnapshot postSnap : contentSnap.getChildren()) {
                            String status = postSnap.child("Status").getValue(String.class);
                            String timeStr = postSnap.child("CreatedTime").getValue(String.class);
                            String userId = postSnap.child("UserId").getValue(String.class);

                            if (status == null || timeStr == null || userId == null) continue;
                            if (!status.equals("Published")) continue;

                            try {
                                Date date = format.parse(timeStr);
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);

                                if (!cal.before(fromCal) && !cal.after(toCal)) {
                                    userPublishedCount.put(userId, userPublishedCount.getOrDefault(userId, 0) + 1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // 3️⃣ Lấy Top 5
                        List<Map.Entry<String, Integer>> topUsers = new ArrayList<>(userPublishedCount.entrySet());
                        topUsers.sort((a, b) -> b.getValue() - a.getValue());
                        if (topUsers.size() > 5) topUsers = topUsers.subList(0, 5);

                        // 4️⃣ Chuẩn bị dữ liệu cho chart
                        List<BarEntry> entries = new ArrayList<>();
                        List<String> labels = new ArrayList<>();
                        int index = 0;

                        for (Map.Entry<String, Integer> entry : topUsers) {
                            entries.add(new BarEntry(index, entry.getValue()));
                            labels.add(userMap.getOrDefault(entry.getKey(), entry.getKey())); // hiển thị tên hoặc userId
                            index++;
                        }

                        BarDataSet dataSet = new BarDataSet(entries, "Top 5 User Published");
                        dataSet.setColor(Color.rgb(155, 89, 182));
                        dataSet.setValueTextSize(12f);

                        BarData data = new BarData(dataSet);
                        data.setBarWidth(0.7f);

                        horizontalBarChart.setData(data);

                        XAxis xAxis = horizontalBarChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setGranularity(1f);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setDrawGridLines(false);
                        xAxis.setTextSize(12f);

                        horizontalBarChart.getAxisLeft().setEnabled(false);
                        horizontalBarChart.getAxisRight().setDrawGridLines(false);
                        horizontalBarChart.getLegend().setEnabled(true);
                        horizontalBarChart.getDescription().setEnabled(false);
                        horizontalBarChart.setFitBars(true);
                        horizontalBarChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Hiển thị đẹp cho người dùng: dd/MM/yyyy
                    String displayDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
                    target.setText(displayDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }


}

