package com.example.multiroomlocalization;

public class Speaker {

    private String mac;
    private String name;

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
