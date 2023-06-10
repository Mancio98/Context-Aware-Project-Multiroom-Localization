package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageUnsuccessfulLogin extends Message {
    public static String type="UNSUCCESSFUL_LOGIN";

    public MessageUnsuccessfulLogin() {
        super(type);
    }
}