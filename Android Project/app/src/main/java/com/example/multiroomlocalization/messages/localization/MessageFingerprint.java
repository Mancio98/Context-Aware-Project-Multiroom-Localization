package com.example.multiroomlocalization.messages.localization;

<<<<<<< HEAD
import com.example.multiroomlocalization.localization.Fingerprint;
=======
import com.example.multiroomlocalization.Fingerprint;
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
import com.example.multiroomlocalization.messages.Message;


public class MessageFingerprint extends Message {
<<<<<<< HEAD
	private Fingerprint fingerprint;

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
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd

    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
