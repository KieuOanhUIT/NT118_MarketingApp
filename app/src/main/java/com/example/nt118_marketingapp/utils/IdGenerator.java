package com.example.nt118_marketingapp.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Utility class để generate ID theo format: PREFIX + số thứ tự
 * 
 * Format:
 * - Content: C001, C002, C003, ...
 * - SubTask: ST001, ST002, ST003, ...
 * - Notification: N001, N002, N003, ...
 * - User: U001, U002, U003, ...
 * - Approval: A001, A002, A003, ...
 * 
 * Sử dụng Firebase Counter để đảm bảo tính duy nhất
 */
public class IdGenerator {
    
    private static final DatabaseReference counterRef = 
        FirebaseDatabase.getInstance().getReference("Counters");
    
    /**
     * Callback interface để nhận ID đã generate
     */
    public interface IdCallback {
        void onIdGenerated(String id);
        void onError(String error);
    }
    
    /**
     * Generate Content ID: C001, C002, ...
     */
    public static void generateContentId(IdCallback callback) {
        generateId("Content", "C", 3, callback);
    }
    
    /**
     * Generate SubTask ID: ST001, ST002, ...
     */
    public static void generateSubTaskId(IdCallback callback) {
        generateId("SubTask", "ST", 3, callback);
    }
    
    /**
     * Generate Notification ID: N001, N002, ...
     */
    public static void generateNotificationId(IdCallback callback) {
        generateId("Notification", "N", 3, callback);
    }
    
    /**
     * Generate User ID: U001, U002, ...
     */
    public static void generateUserId(IdCallback callback) {
        generateId("User", "U", 3, callback);
    }
    
    /**
     * Generate Approval ID: A001, A002, ...
     */
    public static void generateApprovalId(IdCallback callback) {
        generateId("Approval", "A", 3, callback);
    }
    
    /**
     * Core method để generate ID với format tùy chỉnh
     * 
     * @param counterName Tên counter trong Firebase (Content, SubTask, ...)
     * @param prefix Prefix của ID (C, ST, N, ...)
     * @param digitLength Số chữ số (3 → 001, 002, ...)
     * @param callback Callback để nhận kết quả
     */
    private static void generateId(String counterName, String prefix, int digitLength, IdCallback callback) {
        counterRef.child(counterName).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Long currentValue = currentData.getValue(Long.class);
                if (currentValue == null) {
                    currentData.setValue(1L);
                } else {
                    currentData.setValue(currentValue + 1);
                }
                return com.google.firebase.database.Transaction.success(currentData);
            }
            
            @Override
            public void onComplete(com.google.firebase.database.DatabaseError error, boolean committed, 
                                 DataSnapshot currentData) {
                if (error != null) {
                    callback.onError("Lỗi generate ID: " + error.getMessage());
                } else if (committed) {
                    Long counterValue = currentData.getValue(Long.class);
                    if (counterValue != null) {
                        String formattedId = formatId(prefix, counterValue, digitLength);
                        callback.onIdGenerated(formattedId);
                    } else {
                        callback.onError("Lỗi: Counter value null");
                    }
                } else {
                    callback.onError("Transaction không committed");
                }
            }
        });
    }
    
    /**
     * Format số thành ID với prefix và padding zeros
     * 
     * @param prefix Prefix (C, ST, N, ...)
     * @param number Số thứ tự
     * @param digitLength Số chữ số
     * @return Formatted ID (VD: C001, ST042, N123)
     */
    private static String formatId(String prefix, long number, int digitLength) {
        String format = "%s%0" + digitLength + "d";
        return String.format(format, prefix, number);
    }
    
    /**
     * Reset counter (chỉ dùng cho testing/debug)
     * KHÔNG nên dùng trong production!
     */
    public static void resetCounter(String counterName, ResetCallback callback) {
        counterRef.child(counterName).setValue(0L)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    /**
     * Get current counter value (để kiểm tra)
     */
    public static void getCurrentCounter(String counterName, CounterCallback callback) {
        counterRef.child(counterName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long value = snapshot.getValue(Long.class);
                callback.onCounterValue(value != null ? value : 0L);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    
    public interface ResetCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface CounterCallback {
        void onCounterValue(long value);
        void onError(String error);
    }
}
