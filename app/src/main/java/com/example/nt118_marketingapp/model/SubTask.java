package com.example.nt118_marketingapp.model;

import com.google.firebase.database.PropertyName;

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
    @PropertyName("SubTaskId")
    private String SubTaskId;
    
    @PropertyName("ContentId")
    private String ContentId;
    
    @PropertyName("UserId")
    private String UserId;
    
    @PropertyName("Title")
    private String Title;
    
    @PropertyName("IsDone")
    private Boolean IsDone;
    
    @PropertyName("Deadline")
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
    @PropertyName("SubTaskId")
    public String getSubTaskId() {
        return SubTaskId;
    }
    
    @PropertyName("SubTaskId")
    public void setSubTaskId(String subTaskId) {
        this.SubTaskId = subTaskId;
    }
    
    @PropertyName("ContentId")
    public String getContentId() {
        return ContentId;
    }
    
    @PropertyName("ContentId")
    public void setContentId(String contentId) {
        this.ContentId = contentId;
    }
    
    @PropertyName("UserId")
    public String getUserId() {
        return UserId;
    }
    
    @PropertyName("UserId")
    public void setUserId(String userId) {
        this.UserId = userId;
    }
    
    @PropertyName("Title")
    public String getTitle() {
        return Title;
    }
    
    @PropertyName("Title")
    public void setTitle(String title) {
        this.Title = title;
    }
    
    @PropertyName("IsDone")
    public Boolean getIsDone() {
        return IsDone != null ? IsDone : false;
    }
    
    @PropertyName("IsDone")
    public void setIsDone(Boolean isDone) {
        this.IsDone = isDone;
    }
    
    @PropertyName("Deadline")
    public String getDeadline() {
        return Deadline;
    }
    
    @PropertyName("Deadline")
    public void setDeadline(String deadline) {
        this.Deadline = deadline;
    }
}
