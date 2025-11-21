package com.example.nt118_marketingapp;

public class ReportItem {
    private String month;
    private String Channel;
    private int posts;

    public ReportItem( String month,String channel, int posts) {
        this.Channel = channel;
        this.month = month;
        this.posts = posts;
    }
    public String getChannel() { return Channel; }
    public String getMonth() { return month; }
    public int getPosts() { return posts; }
}

