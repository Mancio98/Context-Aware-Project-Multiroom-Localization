package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageAcknowledgment extends Message {
	
    public MessageAcknowledgment() {
        super("ACKNOWLEDGMENT");
    }
}
