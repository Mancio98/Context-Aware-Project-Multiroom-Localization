package server.localization;


import java.util.HashMap;


public class Fingerprint {
    private final HashMap <String, ScanResult> fingerprint;

    public Fingerprint() {
        this.fingerprint = new HashMap<String, ScanResult>();
    }

    public void add(ScanResult scan) {
        this.fingerprint.put(scan.getBSSID(), scan);
    }

    public HashMap<String, ScanResult> getMap() {
        return this.fingerprint;
    }
}
