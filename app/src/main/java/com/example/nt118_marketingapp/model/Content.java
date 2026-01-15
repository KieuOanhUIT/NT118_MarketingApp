package com.example.nt118_marketingapp.model;

import com.google.firebase.database.PropertyName;

public class Content {
    @PropertyName("ContentId")
    private String ContentId; // Firebase key - MUST match Firebase field name exactly
    
    @PropertyName("Title")
    private String Title;
    
    @PropertyName("Type")
    private String Type;
    
    @PropertyName("Channel")
    private String Channel;
    
    @PropertyName("Tag")
    private String Tag;
    
    @PropertyName("CreatedTime")
    private String CreatedTime;
    
    @PropertyName("Status")
    private String Status;
    
    @PropertyName("Url")
    private String Url;
    
    @PropertyName("EditorLink")
    private String EditorLink;
    
    @PropertyName("PublishedTime")
    private String PublishedTime;
    
    @PropertyName("ModifiedTime")
    private String ModifiedTime;
    
    @PropertyName("UserId")
    private String UserId;

    public Content() {}

    public Content(String title, String type, String channel, String tag, String createdTime, 
                   String status, String url, String editorLink, String userId) {
        this.Title = title;
        this.Type = type;
        this.Channel = channel;
        this.Tag = tag;
        this.CreatedTime = createdTime;
        this.Status = status;
        this.Url = url;
        this.EditorLink = editorLink;
        this.UserId = userId;
        this.PublishedTime = "";
        this.ModifiedTime = createdTime;
    }

    @PropertyName("Title")
    public String getTitle() { return Title; }
    
    @PropertyName("Type")
    public String getType() { return Type; }
    
    @PropertyName("Channel")
    public String getChannel() { return Channel; }
    
    @PropertyName("Tag")
    public String getTag() { return Tag; }
    
    @PropertyName("CreatedTime")
    public String getCreatedTime() { return CreatedTime; }
    
    @PropertyName("Status")
    public String getStatus() { return Status; }
    
    @PropertyName("Url")
    public String getUrl() { return Url; }
    
    @PropertyName("EditorLink")
    public String getEditorLink() { return EditorLink; }
    
    @PropertyName("PublishedTime")
    public String getPublishedTime() { return PublishedTime; }
    
    @PropertyName("ModifiedTime")
    public String getModifiedTime() { return ModifiedTime; }
    
    @PropertyName("UserId")
    public String getUserId() { return UserId; }
    
    @PropertyName("ContentId")
    public String getContentId() { return ContentId; }

    @PropertyName("Title")
    public void setTitle(String title) { this.Title = title; }
    
    @PropertyName("Type")
    public void setType(String type) { this.Type = type; }
    
    @PropertyName("Channel")
    public void setChannel(String channel) { this.Channel = channel; }
    
    @PropertyName("Tag")
    public void setTag(String tag) { this.Tag = tag; }
    
    @PropertyName("CreatedTime")
    public void setCreatedTime(String createdTime) { this.CreatedTime = createdTime; }
    
    @PropertyName("Status")
    public void setStatus(String status) { this.Status = status; }
    
    @PropertyName("Url")
    public void setUrl(String url) { this.Url = url; }
    
    @PropertyName("EditorLink")
    public void setEditorLink(String editorLink) { this.EditorLink = editorLink; }
    
    @PropertyName("PublishedTime")
    public void setPublishedTime(String publishedTime) { this.PublishedTime = publishedTime; }
    
    @PropertyName("ModifiedTime")
    public void setModifiedTime(String modifiedTime) { this.ModifiedTime = modifiedTime; }
    
    @PropertyName("UserId")
    public void setUserId(String userId) { this.UserId = userId; }
    
    @PropertyName("ContentId")
    public void setContentId(String contentId) { this.ContentId = contentId; }
}


