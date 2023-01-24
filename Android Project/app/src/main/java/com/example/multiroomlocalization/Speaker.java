package com.example.multiroomlocalization;

public class Speaker {
    public String mac;
    public String name;

    public Speaker(String id, String name) {
        this.name = name;
    }

    public String getMAC() {
        return this.mac;
    }

    public String getName() {
        return this.name;
    }

}
