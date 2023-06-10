package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageRegistrationUnsuccessful extends Message {

    public static String type = "REGISTRATION UNSUCCESSFUL";
    public String description;
	
    public MessageRegistrationUnsuccessful(String description) {
        super(type);
        this.description = description;
    }
    
    public String getDescription()
    {
    	return this.description;
    }
}
