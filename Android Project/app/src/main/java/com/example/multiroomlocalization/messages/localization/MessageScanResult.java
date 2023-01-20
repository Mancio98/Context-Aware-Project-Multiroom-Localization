package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.ScanResult;
import com.example.multiroomlocalization.Fingerprint;


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