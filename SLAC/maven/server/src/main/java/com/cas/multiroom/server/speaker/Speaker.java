package com.cas.multiroom.server.speaker;


public class Speaker {
    public String mac;
    public String name;

    public Speaker(String mac, String name) {
    	this.mac = mac;
        this.name = name;
    }

    public String getMAC() {
        return this.mac;
    }

    public String getName() {
        return this.name;
    }
}