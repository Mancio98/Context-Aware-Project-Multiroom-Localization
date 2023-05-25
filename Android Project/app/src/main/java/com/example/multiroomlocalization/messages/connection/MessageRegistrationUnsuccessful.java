package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

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
