package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageImageFinish extends Message {
    public static String type = "IMAGE_FINISH";
    public MessageImageFinish() {
        super(type);
    }
}
