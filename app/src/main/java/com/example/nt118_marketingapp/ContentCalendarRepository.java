package com.example.nt118_marketingapp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Repository giả để cung cấp dữ liệu mẫu cho Content Calendar
 * Trong thực tế, sẽ lấy dữ liệu từ database hoặc API
 */
public class ContentCalendarRepository {
    
    private static ContentCalendarRepository instance;
    private Map<String, ContentCount> contentMap; // Key: "yyyy-MM-dd_HH"
    
    private ContentCalendarRepository() {
        contentMap = new HashMap<>();
        generateSampleData();
    }
    
    public static synchronized ContentCalendarRepository getInstance() {
        if (instance == null) {
            instance = new ContentCalendarRepository();
        }
        return instance;
    }
    
    /**
     * Tạo dữ liệu mẫu cho demo
     */
    private void generateSampleData() {
        Random random = new Random();
        LocalDate today = LocalDate.now();
        
        // Tạo data cho tuần hiện tại và 2 tuần trước/sau
        for (int weekOffset = -2; weekOffset <= 2; weekOffset++) {
            LocalDate weekStart = today.plusWeeks(weekOffset).with(java.time.DayOfWeek.MONDAY);
            
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate day = weekStart.plusDays(dayOffset);
                
                // Tạo content cho một số giờ ngẫu nhiên
                for (int hour = 6; hour <= 22; hour++) {
                    // 40% xác suất có content
                    if (random.nextDouble() < 0.4) {
                        int count = random.nextInt(5) + 1; // 1-5 content
                        String key = getKey(day, hour);
                        contentMap.put(key, new ContentCount(day, hour, count));
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách content count cho một tuần
     * @param weekStart Ngày bắt đầu tuần (thứ 2)
     * @return Danh sách ContentCount
     */
    public List<ContentCount> getContentCountsForWeek(LocalDate weekStart) {
        List<ContentCount> result = new ArrayList<>();
        
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate day = weekStart.plusDays(dayOffset);
            
            for (int hour = 0; hour < 24; hour++) {
                String key = getKey(day, hour);
                ContentCount contentCount = contentMap.get(key);
                
                if (contentCount != null) {
                    result.add(contentCount);
                } else {
                    // Nếu không có data, trả về count = 0
                    result.add(new ContentCount(day, hour, 0));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Lấy số lượng content cho một ngày và giờ cụ thể
     */
    public int getContentCount(LocalDate day, int hour) {
        String key = getKey(day, hour);
        ContentCount contentCount = contentMap.get(key);
        return contentCount != null ? contentCount.getCount() : 0;
    }
    
    /**
     * Tạo key cho map
     */
    private String getKey(LocalDate day, int hour) {
        return day.toString() + "_" + hour;
    }
    
    /**
     * Thêm content mới (sẽ được gọi khi tạo content từ form)
     */
    public void addContent(LocalDate day, int hour) {
        String key = getKey(day, hour);
        ContentCount existing = contentMap.get(key);
        
        if (existing != null) {
            existing.setCount(existing.getCount() + 1);
        } else {
            contentMap.put(key, new ContentCount(day, hour, 1));
        }
    }
}
