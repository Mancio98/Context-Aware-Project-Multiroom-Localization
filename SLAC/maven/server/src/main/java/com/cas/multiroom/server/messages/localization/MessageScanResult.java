package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.messages.Message;
import com.cas.multiroom.server.localization.ScanResult;


public class MessageScanResult extends Message {
	private ScanResult[] fingerprint;

	public MessageScanResult(ScanResult[] fingerprint) {
		super("SCAN_INFO");
        this.fingerprint = fingerprint;
	}

    public ScanResult[] getFingerprint() {
        return this.fingerprint;
    }
}
