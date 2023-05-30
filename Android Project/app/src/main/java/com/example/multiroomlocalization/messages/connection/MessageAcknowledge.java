package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageAcknowledge extends Message {

    public static String type = "ACKNOWLEDGMENT";

    public MessageAcknowledge() {
        super(type);
    }
}
