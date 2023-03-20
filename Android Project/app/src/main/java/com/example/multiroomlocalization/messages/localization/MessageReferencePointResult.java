package com.example.multiroomlocalization.messages.localization;

<<<<<<< HEAD
import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;
//import server.localization.ReferencePoint;


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
=======
import com.example.multiroomlocalization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

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
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
