package com.example.nt118_marketingapp.model;

public class Notification {
    private String id;
    private String CreatedTime;
    private boolean IsRead;
    private String Message;
    private String Type;
    private String UserId;

    public Notification() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreatedTime() { return CreatedTime; }
    public void setCreatedTime(String createdTime) { CreatedTime = createdTime; }

    public boolean isRead() { return IsRead; }
    public void setRead(boolean read) { IsRead = read; }

    public String getMessage() { return Message; }
    public void setMessage(String message) { Message = message; }

    public String getType() { return Type; }
    public void setType(String type) { Type = type; }

    public String getUserId() { return UserId; }
    public void setUserId(String userId) { UserId = userId; }
}
