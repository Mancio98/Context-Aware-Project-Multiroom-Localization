package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.database.User;
import com.cas.multiroom.server.messages.Message;

public class MessageRegistration extends Message {
    private User user;

    public MessageRegistration(User user) {
        super("REGISTRATION");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
