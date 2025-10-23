package com.example.nt118_marketingapp;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    EditText edtFromDate, edtToDate;
    Button btnApply;
    RecyclerView recyclerView;
    BarChart barChart;
    HorizontalBarChart horizontalBarChart;
    ReportAdapter adapter;
    List<ReportItem> reportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        edtFromDate = findViewById(R.id.edtFromDate);
        edtToDate = findViewById(R.id.edtToDate);
        btnApply = findViewById(R.id.btnApply);
        recyclerView = findViewById(R.id.recyclerReport);
        barChart = findViewById(R.id.barChart);
        horizontalBarChart = findViewById(R.id.horizontalBarChart);

        // RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportList = getDummyData();
        adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);

        setupBarChart();
        setupHorizontalBarChart();

        // chọn ngày
        edtFromDate.setOnClickListener(v -> showDatePicker(edtFromDate));
        edtToDate.setOnClickListener(v -> showDatePicker(edtToDate));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> target.setText(year + "-" + (month + 1) + "-" + day),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private List<ReportItem> getDummyData() {
        List<ReportItem> list = new ArrayList<>();
        list.add(new ReportItem("Facebook", "Tháng 1", 120));
        list.add(new ReportItem("Instagram", "Tháng 1", 85));
        list.add(new ReportItem("Blog", "Tháng 1", 15));
        return list;
    }

    private void setupBarChart() {
        List<BarEntry> keHoach = Arrays.asList(
                new BarEntry(1, 100), new BarEntry(2, 110),
                new BarEntry(3, 120), new BarEntry(4, 130), new BarEntry(5, 140));

        List<BarEntry> thucHien = Arrays.asList(
                new BarEntry(1, 80), new BarEntry(2, 95),
                new BarEntry(3, 110), new BarEntry(4, 125), new BarEntry(5, 135));

        BarDataSet set1 = new BarDataSet(keHoach, "Kế hoạch");
        set1.setColor(Color.rgb(99, 132, 255));
        BarDataSet set2 = new BarDataSet(thucHien, "Thực hiện");
        set2.setColor(Color.rgb(255, 99, 132));

        BarData data = new BarData(set1, set2);
        data.setBarWidth(0.3f);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);    // Ẩn mô tả

        barChart.groupBars(0.5f, 0.2f, 0.02f);
        barChart.invalidate();
    }


    private void setupHorizontalBarChart() {
        // 1️⃣ Dữ liệu điểm (giá trị)
        List<BarEntry> entries = Arrays.asList(
                new BarEntry(0, 50),
                new BarEntry(1, 60),
                new BarEntry(2, 70),
                new BarEntry(3, 80),
                new BarEntry(4, 99)
        );

        // 2️⃣ Danh sách tên (label)
        List<String> names = Arrays.asList("Nguyễn A", "Trần B", "Lê C", "Phạm D", "Hoàng E");

        // 3️⃣ Tạo dataset
        BarDataSet dataSet = new BarDataSet(entries, "Top cá nhân");
        dataSet.setColor(Color.rgb(155, 89, 182));
        dataSet.setValueTextSize(12f);

        // 4️⃣ Gắn dữ liệu vào chart
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f); // chiều rộng cột
        horizontalBarChart.setData(data);

        // 5️⃣ Thiết lập nhãn cho trục X
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(names));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(names.size());
        xAxis.setTextSize(12f);

        // 6️⃣ Tùy chỉnh trục Y
        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        YAxis rightAxis = horizontalBarChart.getAxisRight();
        leftAxis.setEnabled(false); // ẩn trục trái
        rightAxis.setDrawGridLines(false);

        // 7️⃣ Ẩn chú giải (legend) nếu muốn
        Legend legend = horizontalBarChart.getLegend();
        legend.setEnabled(true);

        // 8️⃣ Làm mới biểu đồ
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.setFitBars(true);
        horizontalBarChart.invalidate();
    }

}

