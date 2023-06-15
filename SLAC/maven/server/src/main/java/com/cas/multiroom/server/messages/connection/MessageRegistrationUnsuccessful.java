package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.database.User;
import com.cas.multiroom.server.messages.Message;

public class MessageRegistrationUnsuccessful extends Message {

	public String description;
	
    public MessageRegistrationUnsuccessful(String description) {
        super("REGISTRATION UNSUCCESSFUL");
        this.description = description;
    }
    
    public String getDescription()
    {
    	return this.description;
    }
}
