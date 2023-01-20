package com.example.multiroomlocalization.messages.localization;

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