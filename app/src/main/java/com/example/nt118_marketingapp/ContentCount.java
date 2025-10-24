package com.example.nt118_marketingapp;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model đại diện cho số lượng content tại một thời điểm cụ thể
 */
public class ContentCount {
    private LocalDate day;      // Ngày
    private int hour;            // Giờ (0-23)
    private int count;           // Số lượng content

    public ContentCount(LocalDate day, int hour, int count) {
        this.day = day;
        this.hour = hour;
        this.count = count;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentCount that = (ContentCount) o;
        return hour == that.hour && Objects.equals(day, that.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, hour);
    }
}
