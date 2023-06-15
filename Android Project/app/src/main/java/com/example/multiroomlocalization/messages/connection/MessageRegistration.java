package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.User;
import com.example.multiroomlocalization.messages.Message;

public class MessageRegistration extends Message {
    private final User user;

    public MessageRegistration(User user) {
        super("REGISTRATION");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
