package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.localization.ReferencePoint;


public class MessageReferencePointChanged extends Message {
	private ReferencePoint referencePoint;

	public MessageReferencePointChanged(ReferencePoint referencePoint) {
		super("REFERENCE_POINT_CHANGED");
        this.referencePoint = referencePoint;
	}

    public ReferencePoint getFingerprint() {
        return this.referencePoint;
    }
}
