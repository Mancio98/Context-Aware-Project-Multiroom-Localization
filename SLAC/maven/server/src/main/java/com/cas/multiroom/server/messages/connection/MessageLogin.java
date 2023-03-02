package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.database.User;
import com.cas.multiroom.server.messages.Message;

public class MessageLogin extends Message {
    private User user;

    public MessageLogin(User user) {
        super("LOGIN");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}