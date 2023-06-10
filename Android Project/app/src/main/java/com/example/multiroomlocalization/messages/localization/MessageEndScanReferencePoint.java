package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageEndScanReferencePoint extends Message {
   // private ReferencePoint referencePoint;

    public MessageEndScanReferencePoint(){//ReferencePoint referencePoint) {
        super("END_SCAN_REFERENCE_POINT");
        //this.referencePoint = referencePoint;
    }

   /* public ReferencePoint getFingerprint() {
        return this.referencePoint;
    }
*/
}
