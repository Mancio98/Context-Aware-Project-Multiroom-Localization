package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageStartScanReferencePoint extends Message {
	//private ReferencePoint referencePoint;

	public MessageStartScanReferencePoint(ReferencePoint referencePoint) {
		super("START_SCAN_REFERENCE_POINT");
		//this.referencePoint = referencePoint;
	}
	
	/*
	public ReferencePoint getReferencePoint() {
		return this.referencePoint;
	}
	*/
}
