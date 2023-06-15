package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageImage extends Message {
	
	private int n_byte;
	
    public MessageImage(int n_byte) {
        super("IMAGE");
        this.n_byte = n_byte;
    }
    
    public int getNByte() {
    	return this.n_byte;
    }
    
    public void setNByte(int n_byte) {
    	this.n_byte = n_byte;
    }
}
