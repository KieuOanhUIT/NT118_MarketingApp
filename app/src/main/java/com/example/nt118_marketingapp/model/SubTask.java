package com.example.nt118_marketingapp.model;

/**
 * SubTask Model - represents a subtask in Firebase
 * Database schema:
 * - SubTaskId (PK): varchar(5)
 * - ContentId (FK): varchar(5)
 * - UserId (FK): varchar(5)
 * - Title: varchar(255)
 * - IsDone: bool
 * - Deadline: datetime
 */
public class SubTask {
    private String SubTaskId;
    private String ContentId;
    private String UserId;
    private String Title;
    private Boolean IsDone;
    private String Deadline; // Format: dd/MM/yyyy HH:mm
    
    // Empty constructor required for Firebase
    public SubTask() {
    }
    
    public SubTask(String subTaskId, String contentId, String userId, String title, Boolean isDone, String deadline) {
        this.SubTaskId = subTaskId;
        this.ContentId = contentId;
        this.UserId = userId;
        this.Title = title;
        this.IsDone = isDone != null ? isDone : false;
        this.Deadline = deadline;
    }
    
    // Getters and Setters
    public String getSubTaskId() {
        return SubTaskId;
    }
    
    public void setSubTaskId(String subTaskId) {
        this.SubTaskId = subTaskId;
    }
    
    public String getContentId() {
        return ContentId;
    }
    
    public void setContentId(String contentId) {
        this.ContentId = contentId;
    }
    
    public String getUserId() {
        return UserId;
    }
    
    public void setUserId(String userId) {
        this.UserId = userId;
    }
    
    public String getTitle() {
        return Title;
    }
    
    public void setTitle(String title) {
        this.Title = title;
    }
    
    public Boolean getIsDone() {
        return IsDone != null ? IsDone : false;
    }
    
    public void setIsDone(Boolean isDone) {
        this.IsDone = isDone;
    }
    
    public String getDeadline() {
        return Deadline;
    }
    
    public void setDeadline(String deadline) {
        this.Deadline = deadline;
    }
}
