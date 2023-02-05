package server.messages.localization;

import server.messages.Message;
import server.localization.Fingerprint;


public class MessageFingerprint extends Message {
<<<<<<< HEAD
	public Fingerprint fingerprint;

	public MessageFingerprint(Fingerprint fingerprint) {
		super("FINGERPRINT");
        this.fingerprint = fingerprint;
	}
=======
    public Fingerprint fingerprint;

    public MessageFingerprint(Fingerprint fingerprint) {
        super("FINGERPRINT");
        this.fingerprint = fingerprint;
    }
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b

    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
