package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

public class MessageNewReferencePoint  extends Message {
    private ReferencePoint referencePoint;

    public MessageNewReferencePoint(ReferencePoint referencePoint) {
        super("NEW_REFERENCE_POINT");
        this.referencePoint = referencePoint;
    }

    public ReferencePoint getReferencePoint() {
        return this.referencePoint;
    }

}
