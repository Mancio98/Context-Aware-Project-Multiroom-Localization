package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageConnectionBack  extends Message {
    private final String id;
    private final boolean accepted;

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

