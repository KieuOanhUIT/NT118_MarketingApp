package com.example.nt118_marketingapp;

public class Post {

    private String ContentId;
    private String Title;
    private String FullName;
    private String PublishedTime;
    private String Status;

    public Post() {

    }

    public Post(String contentId, String title, String fullName, String publishedTime, String status ) {
        ContentId = contentId;
        Title = title;
        FullName = fullName;
        PublishedTime = publishedTime;
        Status = status;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPublishedTime() {
        return PublishedTime;
    }

    public void setPublishedTime(String publishedTime) {
        PublishedTime = publishedTime;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getContentId() {
        return ContentId;
    }

    public void setContentId(String contentId) {
        ContentId = contentId;
    }
}
