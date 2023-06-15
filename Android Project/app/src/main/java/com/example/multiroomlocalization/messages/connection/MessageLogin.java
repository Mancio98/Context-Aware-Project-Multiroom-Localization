package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.User;
import com.example.multiroomlocalization.messages.Message;

public class MessageLogin extends Message {
    private final User user;

    public MessageLogin(User user) {
        super("LOGIN");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
