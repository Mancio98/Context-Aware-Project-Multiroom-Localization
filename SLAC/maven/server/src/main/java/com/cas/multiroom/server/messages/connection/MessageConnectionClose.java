package com.cas.multiroom.server.messages.connection;


import com.cas.multiroom.server.messages.Message;


public class MessageConnectionClose extends Message {

    public MessageConnectionClose() {
        super("CONNECTION_CLOSE");
    }
}
