package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.messages.Message;
<<<<<<< HEAD
import com.cas.multiroom.server.localization.Fingerprint;


public class MessageFingerprint extends Message {
<<<<<<< HEAD
	public Fingerprint fingerprint;

	public MessageFingerprint(Fingerprint fingerprint) {
		super("FINGERPRINT");
        this.fingerprint = fingerprint;
	}
=======
    public Fingerprint fingerprint;

    public MessageFingerprint(Fingerprint fingerprint) {
        super("FINGERPRINT");
        this.fingerprint = fingerprint;
    }
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b

    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
=======

import java.util.List;

import com.cas.multiroom.server.localization.ScanResult;


public class MessageFingerprint extends Message {
    public List<ScanResult> fingerprint;

    public MessageFingerprint(List<ScanResult> scanResult) {
        super("FINGERPRINT");
        this.fingerprint = scanResult;
    }

    public List<ScanResult> getFingerprint() {
        return this.fingerprint;
    }
    
}
>>>>>>> luca-branch
