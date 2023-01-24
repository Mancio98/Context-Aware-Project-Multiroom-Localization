package server.localization;

public class ScanResult {
<<<<<<< HEAD
    private final String BSSID;
    private final String SSID;
    private final double level;

    public ScanResult(String BSSID, String SSID, double level) {
=======

    public final String BSSID;
    public final String SSID;
    public final int level;

    public ScanResult(String BSSID, String SSID, int level) {
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
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

<<<<<<< HEAD
    public double getLevel() {
=======
    public int getLevel() {
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
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
<<<<<<< HEAD
=======

>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
}