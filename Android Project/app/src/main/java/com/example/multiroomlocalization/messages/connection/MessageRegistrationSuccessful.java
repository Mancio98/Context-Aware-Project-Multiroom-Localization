package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageRegistrationSuccessful extends Message {

    public static String type = "REGISTRATION SUCCESSFUL";

    public MessageRegistrationSuccessful() {
        super(type);
    }
}
