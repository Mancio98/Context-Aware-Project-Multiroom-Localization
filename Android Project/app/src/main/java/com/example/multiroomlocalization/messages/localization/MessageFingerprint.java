package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.localization.ScanResult;
import com.example.multiroomlocalization.messages.Message;

import java.util.List;

public class MessageFingerprint extends Message {

    public List<ScanResult> fingerprint;

    private long timestamp;
    public MessageFingerprint(List<ScanResult> scanResult, long timestamp) {
        super("FINGERPRINT");
        this.fingerprint = scanResult;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

