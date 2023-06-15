package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.messages.Message;

import java.util.List;

import com.cas.multiroom.server.localization.ScanResult;


public class MessageFingerprint extends Message {
    public List<ScanResult> fingerprint;
    public long timestamp;

    public MessageFingerprint(List<ScanResult> scanResult, long timestamp) {
        super("FINGERPRINT");
        this.fingerprint = scanResult;
        this.timestamp = timestamp;
    }

    public List<ScanResult> getFingerprint() {
        return this.fingerprint;
    }
    
    public long getTimestamp() {
    	return this.timestamp;
    }
}