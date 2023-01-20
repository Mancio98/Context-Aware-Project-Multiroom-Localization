package com.example.multiroomlocalization;

import java.util.HashMap;
import java.util.List;

public class ReferencePoint {

    private String id;
    //private HashMap<String, List<ScanResult>> fingerprints;
    private Speaker speaker;

    private int x;
    private int y;

    public ReferencePoint(int x,int y, String label){
        this.x = x;
        this.y = y;
        this.id = label;
        //this.fingerprints = new HashMap<>();
        this.speaker = null;
    }

    public ReferencePoint(String label, int x, int y) { // HashMap<String, List<ScanResult>> fingerprints,
        this.id = label;
        //this.fingerprints = fingerprints;
        this.speaker = null;
        this.x = x;
        this.y = y;
    }

    public int getY() { return y; }

    public String getId() { return id; }

    public int getX() { return x; }

    public void setY(int y) { this.y = y; }

    public void setId(String label) { this.id = label; }

    public void setX(int x) { this.x = x; }

   // public HashMap<String, List<ScanResult>> getFingerprints() {
   //     return this.fingerprints;
   // }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return this.speaker;
    }


}
