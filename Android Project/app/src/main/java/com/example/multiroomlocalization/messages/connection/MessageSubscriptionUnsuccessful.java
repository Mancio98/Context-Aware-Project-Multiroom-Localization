package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageSubscriptionUnsuccessful extends Message {
    public static String type = "SUBSCRIPTION_UNSUCCESSFUL";

    public MessageSubscriptionUnsuccessful() {
        super(type);
    }
}
