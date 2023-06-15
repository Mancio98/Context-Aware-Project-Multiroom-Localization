package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageSubscriptionUnsuccessful extends Message {
    public static String type = "SUBSCRIPTION_UNSUCCESSFUL";

    public MessageSubscriptionUnsuccessful() {
        super(type);
    }
}
