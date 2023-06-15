package com.cas.multiroom.server.database;

public class MyAudioTrack {

    private final String path;
    private final String title;
    private final String author;
    private final String duration;

    public MyAudioTrack(String path, String title, String author, String duration) {
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
