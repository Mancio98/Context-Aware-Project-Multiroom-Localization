package server.localization;

<<<<<<< HEAD

import java.util.HashMap;


public class Fingerprint {
    public final HashMap <String, ScanResult> fingerprint;

    public Fingerprint() {
        this.fingerprint = new HashMap<String, ScanResult>();
    }

    public void add(ScanResult scan) {
        this.fingerprint.put(scan.getBSSID(), scan);
    }

    public HashMap<String, ScanResult> getMap() {
        return this.fingerprint;
    }
=======
import java.util.ArrayList;

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

>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
}
