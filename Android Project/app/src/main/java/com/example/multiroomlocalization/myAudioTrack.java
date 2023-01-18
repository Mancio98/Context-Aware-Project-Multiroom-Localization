package com.example.multiroomlocalization;

public class myAudioTrack {

    private String path;
    private String title;
    private String author;
    private String duration;

    public myAudioTrack(String path, String title, String author, String duration) {
        this.path = path;
        this.title = title;
        this.author = author;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDuration() {
        return duration;
    }
}
