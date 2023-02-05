package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageConnectionEnd  extends Message {
    private String id;
    private String name;

    public MessageConnectionEnd(String id, String name) {
        super("CONNECTION_END");
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
