package com.example.multiroomlocalization;

public class Speaker {
    public String mac;
    public String room;
    public Speaker(String mac, String room) {
        this.mac = mac;

        this.room = room;
    }

    public String getMac() {
        return mac;
    }

    public String getRoom() {
        return room;
    }
}
