package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageKeepAlive extends Message{
    private static String type = "KEEP_ALIVE";
    public MessageKeepAlive(){
        super(type);
    }
}
