package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageEndScanReferencePoint extends Message {
	private ReferencePoint referencePoint;

	public MessageEndScanReferencePoint(ReferencePoint referencePoint) {
		super("END_SCAN_REFERENCE_POINT");
		this.referencePoint = referencePoint;
	}

	public ReferencePoint getFingerprint() {
		return this.referencePoint;
	}
}
