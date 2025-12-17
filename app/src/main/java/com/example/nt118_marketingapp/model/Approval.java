package com.example.nt118_marketingapp.model;

public class Approval {

    private String ApprovedAt;
    private String ContentId;
    private String Reason;
    private String UserId;

    // 🔹 BẮT BUỘC: constructor rỗng cho Firebase
    public Approval() {
    }

    public Approval(String approvedAt, String contentId, String reason, String userId) {
        ApprovedAt = approvedAt;
        ContentId = contentId;
        Reason = reason;
        UserId = userId;
    }

    public String getApprovedAt() {
        return ApprovedAt;
    }

    public void setApprovedAt(String approvedAt) {
        ApprovedAt = approvedAt;
    }

    public String getContentId() {
        return ContentId;
    }

    public void setContentId(String contentId) {
        ContentId = contentId;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
