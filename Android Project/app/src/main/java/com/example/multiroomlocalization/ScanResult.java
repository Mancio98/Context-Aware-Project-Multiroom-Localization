package com.example.multiroomlocalization;

public class ScanResult {

    public final String BSSID;
    public final String SSID;
    public final int level;

    public ScanResult(String BSSID, String SSID, int level) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.level = level;
    }

    public String getBSSID() {
        return this.BSSID;
    }

    public String getSSID() {
        return this.SSID;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
                "BSSID='" + this.BSSID + '\'' +
                ", SSID='" + this.SSID + '\'' +
                ", level=" + this.level +
                '}';
    }

}
