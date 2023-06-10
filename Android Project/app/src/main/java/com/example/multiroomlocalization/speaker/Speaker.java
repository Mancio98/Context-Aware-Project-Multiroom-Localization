package com.example.multiroomlocalization.speaker;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Set;

public class Speaker {
    private final String name;

    private String mac;
    private String room;


    public Speaker(BluetoothDevice device){
        this.name = device.getName();
        this.mac = device.getAddress();

    }
    public Speaker(String name, String mac) {
        this.name = name;
        this.mac = mac;

    }
    public Speaker(String name, String mac, String room) {
        this.name = name;
        this.mac = mac;
        this.room = room;

    }

    public static ArrayList<Speaker> getListSpeakerFromDevice(Set<BluetoothDevice> list){
        ArrayList<Speaker> listSpeaker = new ArrayList<>();
        list.forEach((device) -> {
            listSpeaker.add(new Speaker(device));
        });
        return listSpeaker;
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
