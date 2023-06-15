package com.cas.multiroom.server.localization;


import java.util.ArrayList;


public class Fingerprint {
    public final ArrayList<ScanResult> fingerprint;
    public long timestamp;

    public Fingerprint(long timestamp) {
        this.fingerprint = new ArrayList<>();
        this.timestamp = timestamp;
    }

    public Fingerprint(ArrayList<ScanResult> scanResults) {
        this.fingerprint = scanResults;
    }

    public void add(ScanResult scan) {
        this.fingerprint.add(scan);
    }

    public ArrayList<ScanResult> getScanResultList() {
        return this.fingerprint;
    }
}
