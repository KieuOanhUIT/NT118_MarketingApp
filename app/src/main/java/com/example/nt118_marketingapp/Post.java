package com.example.nt118_marketingapp;

public class Post {
    private String title;
    private String author;
    private String deadline;
    private String status;

    public Post(String title, String author, String deadline, String status) {
        this.title = title;
        this.author = author;
        this.deadline = deadline;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }
}
