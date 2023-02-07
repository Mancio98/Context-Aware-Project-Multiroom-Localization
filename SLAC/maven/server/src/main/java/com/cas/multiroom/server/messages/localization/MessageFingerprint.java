package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.messages.Message;

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