package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.database.User;
import com.cas.multiroom.server.messages.Message;

public class MessageRegistrationSuccessful extends Message {

    public MessageRegistrationSuccessful() {
        super("REGISTRATION_SUCCESSFUL");
    }
}
