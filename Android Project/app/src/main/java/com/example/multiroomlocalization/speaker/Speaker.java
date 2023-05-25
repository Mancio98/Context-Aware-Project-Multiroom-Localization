package com.example.multiroomlocalization.speaker;

public class Speaker {
    private final String name;
    private String mac;
    private String room;
    private int type;

    public Speaker(String name, String mac, String room, int type) {
        this.name = name;
        this.mac = mac;
        this.room = room;
        this.type = type;
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

    public int getType() {
        return type;
    }
}
