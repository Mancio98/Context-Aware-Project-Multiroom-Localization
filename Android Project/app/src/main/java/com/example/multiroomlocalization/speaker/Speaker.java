package com.example.multiroomlocalization.speaker;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Set;

public class Speaker {
    private final String name;

    private final String mac;
    private String room;


    public Speaker(String name, String mac) {
        this.name = name;
        this.mac = mac;

    }
    public Speaker(String name, String mac, String room) {
        this.name = name;
        this.mac = mac;
        this.room = room;

    }

    @SuppressLint("MissingPermission")
    public static ArrayList<Speaker> getListSpeakerFromDevice(Set<BluetoothDevice> list){
        ArrayList<Speaker> listSpeaker = new ArrayList<>();

        list.forEach((device) -> {

            if(device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                    device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER)
                listSpeaker.add(new Speaker(device.getName(),device.getAddress()));
        });
        return listSpeaker;
    }
    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;

    }



}
