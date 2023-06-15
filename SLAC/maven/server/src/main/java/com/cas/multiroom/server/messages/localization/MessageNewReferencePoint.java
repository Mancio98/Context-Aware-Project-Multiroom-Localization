package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageNewReferencePoint extends Message {
	private ReferencePoint referencePoint;

	public MessageNewReferencePoint(ReferencePoint referencePoint) {
		super("NEW_REFERENCE_POINT");
		this.referencePoint = referencePoint;
	}

	public ReferencePoint getReferencePoint() {
		return this.referencePoint;
	}
}
