package com.example.multiroomlocalization;

public class Speaker {
    private final String name;
    public String mac;
    public String room;
    public Speaker(String mac, String room, String name) {
        this.mac = mac;
        this.name = name;
        this.room = room;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }
}
