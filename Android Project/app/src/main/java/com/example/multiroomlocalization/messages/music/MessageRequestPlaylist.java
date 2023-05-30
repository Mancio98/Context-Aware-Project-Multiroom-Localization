package com.example.multiroomlocalization.messages.music;

import com.example.multiroomlocalization.messages.Message;

public class MessageRequestPlaylist extends Message {
    public static String type = "REQ_PLAYLIST";
    public MessageRequestPlaylist() {
        super(type);

    }
}