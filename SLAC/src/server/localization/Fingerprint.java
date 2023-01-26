package server.localization;

import java.util.ArrayList;

public class Fingerprint {
    private final ArrayList<ScanResult> fingerprint;

    public Fingerprint() {
        this.fingerprint = new ArrayList<>();
    }

    public void add(ScanResult scan) {
        this.fingerprint.add(scan);
    }

    public ArrayList<ScanResult> getMap() {
        return this.fingerprint;
    }
}
