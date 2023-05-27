package com.example.multiroomlocalization.messages.speaker;


import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.messages.Message;

public class MessageChangeReferencePoint extends Message {
	
	public Speaker speaker;
	
	public MessageChangeReferencePoint(Speaker speaker) {
		super("CHANGE_REFERENCE_POINT");
		this.speaker = speaker;
	}
	
	public Speaker getSpeaker() {
		return this.speaker;
	}
	
	public void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
	}
}
