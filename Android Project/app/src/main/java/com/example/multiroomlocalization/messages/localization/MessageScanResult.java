package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;
<<<<<<< HEAD
import com.example.multiroomlocalization.localization.ScanResult;
import com.example.multiroomlocalization.localization.Fingerprint;


public class MessageScanResult extends Message {
	private ScanResult[] fingerprint;

	public MessageScanResult(ScanResult[] fingerprint) {
		super("SCAN_INFO");
        this.fingerprint = fingerprint;
	}
=======
import com.example.multiroomlocalization.ScanResult;
import com.example.multiroomlocalization.Fingerprint;


public class MessageScanResult extends Message {
    private ScanResult[] fingerprint;

    public MessageScanResult(ScanResult[] fingerprint) {
        super("SCAN_INFO");
        this.fingerprint = fingerprint;
    }
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd

    public ScanResult[] getFingerprint() {
        return this.fingerprint;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
