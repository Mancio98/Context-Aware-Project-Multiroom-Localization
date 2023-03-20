package com.example.multiroomlocalization.messages.localization;


import com.example.multiroomlocalization.localization.Fingerprint;

import com.example.multiroomlocalization.messages.Message;


public class MessageFingerprint extends Message {
	private Fingerprint fingerprint;

	public MessageFingerprint(Fingerprint fingerprint) {
		super("FINGERPRINT");
        this.fingerprint = fingerprint;
	}


    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }

}

