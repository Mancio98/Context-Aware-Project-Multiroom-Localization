package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageUnsuccessfulLogin extends Message {
    public MessageUnsuccessfulLogin() {
        super("UNSUCCESSFUL_LOGIN");
    }
}