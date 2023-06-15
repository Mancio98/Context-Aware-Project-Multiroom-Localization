package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;
//import server.localization.ReferencePoint;


public class MessageReferencePointResult extends Message {

	private final ReferencePoint referencePoint;

	public MessageReferencePointResult(ReferencePoint referencePoint) {
		super("REFERENCE_POINT_RESULT");
		this.referencePoint = referencePoint;
	}

	public ReferencePoint getFingerprint() {
		return this.referencePoint;
	}
}

