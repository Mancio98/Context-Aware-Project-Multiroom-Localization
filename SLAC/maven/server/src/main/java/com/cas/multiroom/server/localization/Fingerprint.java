package com.cas.multiroom.server.localization;


import java.util.ArrayList;


public class Fingerprint {
    public final ArrayList<ScanResult> fingerprint;

    public Fingerprint() {
        this.fingerprint = new ArrayList<>();
    }

    public Fingerprint(ArrayList<ScanResult> scanResults) {
        this.fingerprint = scanResults;
    }

    public void add(ScanResult scan) {
        this.fingerprint.add(scan);
    }

    public ArrayList<ScanResult> getMap() {
        return this.fingerprint;
    }
}
