package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageKeepAlive extends Message {
    private static String type = "KEEP_ALIVE";
    public MessageKeepAlive(){
        super(type);
    }
}
