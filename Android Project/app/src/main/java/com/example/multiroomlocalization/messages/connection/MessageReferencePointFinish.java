package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageReferencePointFinish extends Message {
    public static String type = "REFERENCE_POINT_FINISH";
    public MessageReferencePointFinish() {
        super(type);
    }
}
