package server.messages.localization;

import server.messages.Message;
import server.localization.Fingerprint;


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