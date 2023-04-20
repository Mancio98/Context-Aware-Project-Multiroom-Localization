package com.example.multiroomlocalization.messages.music;

import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.Music.MyAudioTrack;

import java.util.List;

public class MessagePlaylist extends Message {
    public List<MyAudioTrack> songs;

    public MessagePlaylist(List<MyAudioTrack> songs) {
        super("REQ_PLAYLIST");
        this.songs = songs;
    }

    public List<MyAudioTrack> getSong() {
        return this.songs;
    }

    public void setSong(List<MyAudioTrack> songs) {
        this.songs = songs;
    }
}
