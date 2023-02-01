package server.messages.localization;

import server.localization.ReferencePoint;
import server.messages.Message;


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
