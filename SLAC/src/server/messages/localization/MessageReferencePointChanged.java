package server.messages.localization;

import server.messages.Message;
import server.localization.ReferencePoint;


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
