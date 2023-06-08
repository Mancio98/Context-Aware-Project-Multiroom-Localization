package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageImage extends Message {

    public static String type = "IMAGE";
	private int n_byte;
	
    public MessageImage(int n_byte) {
        super(type);
        this.n_byte = n_byte;
    }
    
    public int getNByte() {
    	return this.n_byte;
    }
    
    public void setNByte(int n_byte) {
    	this.n_byte = n_byte;
    }
}
