package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageChangeReferencePoint extends Message {
	
	public ReferencePoint referencePoint;
	
	public MessageChangeReferencePoint(ReferencePoint referencePoint) {
		super("CHANGE_REFERENCE_POINT");
		this.referencePoint = referencePoint;
	}
	
	public ReferencePoint getReferencePoint() {
		return this.referencePoint;
	}
	
	public void setReferencePoint(ReferencePoint referencePoint) {
		this.referencePoint = referencePoint;
	}
}
