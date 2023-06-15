package com.example.multiroomlocalization.Music;

public class MyAudioTrack {

    private final String path;
    private final String title;
    private final String author;


    public MyAudioTrack(String path, String title, String author) {
        this.path = path;
        this.title = title;
        this.author = author;
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


}
