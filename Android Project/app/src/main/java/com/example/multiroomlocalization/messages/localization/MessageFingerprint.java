package com.example.multiroomlocalization.messages.localization;

<<<<<<< HEAD
import com.example.multiroomlocalization.localization.Fingerprint;
=======
import com.example.multiroomlocalization.Fingerprint;
<<<<<<< HEAD
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
=======
import com.example.multiroomlocalization.ScanResult;
>>>>>>> 8b3202b6c7b5adce2bf4a7f9343fdbdcf29d45e3
import com.example.multiroomlocalization.messages.Message;

import java.util.List;


public class MessageFingerprint extends Message {
<<<<<<< HEAD
<<<<<<< HEAD
	private Fingerprint fingerprint;

	public MessageFingerprint(Fingerprint fingerprint) {
		super("FINGERPRINT");
        this.fingerprint = fingerprint;
	}
=======
    public Fingerprint fingerprint;
=======
    public List<ScanResult> fingerprint;
>>>>>>> 8b3202b6c7b5adce2bf4a7f9343fdbdcf29d45e3

    public MessageFingerprint(List<ScanResult> scanResult) {
        super("FINGERPRINT");
        this.fingerprint = scanResult;
    }
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd

    public List<ScanResult> getFingerprint() {
        return this.fingerprint;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
