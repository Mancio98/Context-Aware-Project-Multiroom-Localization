package com.example.multiroomlocalization.messages.connection;


import com.example.multiroomlocalization.messages.Message;

public class MessageConnectionClose extends Message {

    public MessageConnectionClose() {
        super("CONNECTION_CLOSE");
    }
}
