package com.example.multiroomlocalization.messages;

import java.io.Serializable;


public abstract class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;

	public Message(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

}
