package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.messages.Message;
import com.cas.multiroom.server.localization.Fingerprint;


public class MessageFingerprint extends Message {
    public Fingerprint fingerprint;

    public MessageFingerprint(Fingerprint fingerprint) {
        super("FINGERPRINT");
        this.fingerprint = fingerprint;
    }

    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }
}