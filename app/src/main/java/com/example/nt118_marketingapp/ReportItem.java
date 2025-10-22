package com.example.nt118_marketingapp;

public class ReportItem {
    private String channel;
    private String month;
    private int posts;

    public ReportItem(String channel, String month, int posts) {
        this.channel = channel;
        this.month = month;
        this.posts = posts;
    }
    public String getChannel() { return channel; }
    public String getMonth() { return month; }
    public int getPosts() { return posts; }
}

