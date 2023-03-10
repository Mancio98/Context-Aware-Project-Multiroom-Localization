package com.cas.multiroom.server.messages.connection;


import com.cas.multiroom.server.messages.Message;


public class MessageConnectionBack extends Message {
    private String id;
    private boolean accepted;
    
    public MessageConnectionBack(String id, boolean accepted) {
        super("CONNECTION_BACK");
        this.id = id;
        this.accepted = accepted;
    }
    
    public String getId() {
        return this.id;
    }

    public boolean getName() {
        return this.accepted;
    }
}