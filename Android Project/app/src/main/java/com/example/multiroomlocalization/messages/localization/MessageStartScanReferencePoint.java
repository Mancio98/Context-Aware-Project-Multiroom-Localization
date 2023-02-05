package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

public class MessageStartScanReferencePoint  extends Message {
    //private ReferencePoint referencePoint;

    public MessageStartScanReferencePoint(){//ReferencePoint referencePoint) {
        super("START_SCAN_REFERENCE_POINT");
        //this.referencePoint = referencePoint;
    }

	/*
	public ReferencePoint getReferencePoint() {
		return this.referencePoint;
	}
	*/

}
