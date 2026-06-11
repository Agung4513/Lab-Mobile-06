package com.example.habittracker;

public class SocialPost {
    private int id;
    private String username;
    private String time;
    private String title;
    private String body;

    private String photoPath;
    private String tag;

    public SocialPost() {}

    public SocialPost(int id, String username, String time, String title, String body) {
        this.id = id;
        this.username = username;
        this.time = time;
        this.title = title;
        this.body = body;
    }

    public SocialPost(int id, String username, String time, String title, String body, String photoPath, String tag) {
        this.id = id;
        this.username = username;
        this.time = time;
        this.title = title;
        this.body = body;
        this.photoPath = photoPath;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
