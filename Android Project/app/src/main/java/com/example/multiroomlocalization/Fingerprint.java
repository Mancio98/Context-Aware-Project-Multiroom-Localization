package com.example.multiroomlocalization;

import java.util.ArrayList;
import java.util.HashMap;

public class Fingerprint {

    //private final HashMap<String, ScanResult> fingerprint;
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
