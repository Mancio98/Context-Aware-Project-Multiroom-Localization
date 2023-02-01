package server.messages.localization;

import server.messages.Message;
import server.localization.ScanResult;


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
