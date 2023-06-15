package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.Fingerprint;
import com.example.multiroomlocalization.ScanResult;
import com.example.multiroomlocalization.messages.Message;

import java.util.List;


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

