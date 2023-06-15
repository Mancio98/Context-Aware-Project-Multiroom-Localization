package com.example.multiroomlocalization.messages.speaker;


import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

public class MessageChangeReferencePoint extends Message {

	public ReferencePoint referencePoint;
	public static String type = "CHANGE_REFERENCE_POINT";

	public MessageChangeReferencePoint(ReferencePoint referencePoint) {
		super(type);
		this.referencePoint = referencePoint;
	}

	public ReferencePoint getReferencePoint() {
		return this.referencePoint;
	}

}
