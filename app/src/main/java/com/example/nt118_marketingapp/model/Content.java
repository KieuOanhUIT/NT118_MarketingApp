package com.example.nt118_marketingapp.model;
public class Content {
    private String contentID; // Firebase key
    private String Title;
    private String Type;
    private String Channel;
    private String Tag;
    private String CreatedTime;
    private String Status;
    private String Url;
    private String EditorLink;
    private String PublishedTime;
    private String ModifiedTime;
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

    public String getTitle() { return Title; }
    public String getType() { return Type; }
    public String getChannel() { return Channel; }
    public String getTag() { return Tag; }
    public String getCreatedTime() { return CreatedTime; }
    public String getStatus() { return Status; }
    public String getUrl() { return Url; }
    public String getEditorLink() { return EditorLink; }
    public String getPublishedTime() { return PublishedTime; }
    public String getModifiedTime() { return ModifiedTime; }
    public String getUserId() { return UserId; }
    public String getContentID() { return contentID; }

    public void setTitle(String title) { this.Title = title; }
    public void setType(String type) { this.Type = type; }
    public void setChannel(String channel) { this.Channel = channel; }
    public void setTag(String tag) { this.Tag = tag; }
    public void setCreatedTime(String createdTime) { this.CreatedTime = createdTime; }
    public void setStatus(String status) { this.Status = status; }
    public void setUrl(String url) { this.Url = url; }
    public void setEditorLink(String editorLink) { this.EditorLink = editorLink; }
    public void setPublishedTime(String publishedTime) { this.PublishedTime = publishedTime; }
    public void setModifiedTime(String modifiedTime) { this.ModifiedTime = modifiedTime; }
    public void setUserId(String userId) { this.UserId = userId; }
    public void setContentID(String contentID) { this.contentID = contentID; }
}


