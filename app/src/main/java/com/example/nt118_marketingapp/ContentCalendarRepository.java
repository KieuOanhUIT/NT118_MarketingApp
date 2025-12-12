package com.example.nt118_marketingapp;

import androidx.annotation.NonNull;

import com.example.nt118_marketingapp.model.Content;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository để lấy dữ liệu content từ Firebase
 */
public class ContentCalendarRepository {
    
    private static ContentCalendarRepository instance;
    private Map<String, ContentCount> contentMap; // Key: "yyyy-MM-dd_HH"
    private DatabaseReference contentRef;
    private List<ContentCalendarListener> listeners;
    
    private ContentCalendarRepository() {
        contentMap = new HashMap<>();
        listeners = new ArrayList<>();
        contentRef = FirebaseDatabase.getInstance().getReference("Content");
        loadDataFromFirebase();
    }
    
    public static synchronized ContentCalendarRepository getInstance() {
        if (instance == null) {
            instance = new ContentCalendarRepository();
        }
        return instance;
    }
    
    /**
     * Load dữ liệu từ Firebase
     */
    private void loadDataFromFirebase() {
        contentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contentMap.clear();
                
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String createdTime = child.child("CreatedTime").getValue(String.class);
                        if (createdTime != null && !createdTime.isEmpty()) {
                            // Parse timestamp "dd/MM/yyyy HH:mm"
                            LocalDateTime dateTime = parseDateTime(createdTime);
                            if (dateTime != null) {
                                LocalDate day = dateTime.toLocalDate();
                                int hour = dateTime.getHour();
                                
                                    String key = getKey(day, hour);
                                    ContentCount existing = contentMap.get(key);
                                
                                if (existing != null) {
                                    existing.setCount(existing.getCount() + 1);
                                } else {
                                    contentMap.put(key, new ContentCount(day, hour, 1));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid data
                    }
                }
                
                // Notify listeners
                notifyListeners();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
    
    /**
     * Parse datetime từ string "dd/MM/yyyy HH:mm"
     */
    private LocalDateTime parseDateTime(String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return LocalDateTime.parse(timestamp, formatter);
        } catch (Exception e) {
            // Nếu lỗi, thử parse format khác
            try {
                // Thử format chỉ có date "dd/MM/yyyy"
                String[] parts = timestamp.split(" ");
                if (parts.length > 0) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate date = LocalDate.parse(parts[0], dateFormatter);
                    int hour = 0;
                    if (parts.length > 1) {
                        String[] timeParts = parts[1].split(":");
                        if (timeParts.length > 0) {
                            hour = Integer.parseInt(timeParts[0]);
                        }
                    }
                    return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, 0);
                }
            } catch (Exception ex) {
                // Ignore
            }
            return null;
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
     * Lấy danh sách content chi tiết cho 1 ngày và giờ cụ thể
     */
    public void getContentList(LocalDate day, int hour, ContentListCallback callback) {
        List<Content> contentList = new ArrayList<>();
        
        contentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String createdTime = child.child("CreatedTime").getValue(String.class);
                        if (createdTime != null && !createdTime.isEmpty()) {
                            LocalDateTime dateTime = parseDateTime(createdTime);
                            if (dateTime != null) {
                                LocalDate contentDay = dateTime.toLocalDate();
                                int contentHour = dateTime.getHour();
                                
                                // Kiểm tra nếu content thuộc ngày và giờ được yêu cầu
                                if (contentDay.equals(day) && contentHour == hour) {
                                    Content content = child.getValue(Content.class);
                                    if (content != null) {
                                        // Set content ID
                                        content.setContentId(child.getKey());
                                        contentList.add(content);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid data
                    }
                }
                callback.onContentListLoaded(contentList);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onContentListLoaded(new ArrayList<>());
            }
        });
    }
    
    /**
     * Thêm listener để nhận thông báo khi data thay đổi
     */
    public void addListener(ContentCalendarListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Xóa listener
     */
    public void removeListener(ContentCalendarListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify tất cả listeners
     */
    private void notifyListeners() {
        for (ContentCalendarListener listener : listeners) {
            listener.onDataChanged();
        }
    }
    
    /**
     * Callback interface để nhận danh sách content
     */
    public interface ContentListCallback {
        void onContentListLoaded(List<Content> contentList);
    }
    
    /**
     * Listener interface để nhận thông báo khi data thay đổi
     */
    public interface ContentCalendarListener {
        void onDataChanged();
    }
}
