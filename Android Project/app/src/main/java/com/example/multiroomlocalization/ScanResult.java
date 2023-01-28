package com.example.multiroomlocalization;

public class ScanResult {

    private final String BSSID;
    private final String SSID;
    private final double level;

    public ScanResult(String BSSID, String SSID, double level) {
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

    public double getLevel() {
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
