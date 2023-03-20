package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;


public class MessageReferencePointResult extends Message {
	private ReferencePoint referencePoint;

	public MessageReferencePointResult(ReferencePoint referencePoint) {
		super("REFERENCE_POINT_RESULT");
		this.referencePoint = referencePoint;
	}

	public ReferencePoint getFingerprint() {
		return this.referencePoint;
	}
}
