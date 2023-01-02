package com.example.multiroomlocalization;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class ListBluetoothElement{

    private String nameDevice;
    private String mac;
    private String room;
    private Spinner spinner;


    public ListBluetoothElement(String nameDevice, String mac) {
        this.nameDevice = nameDevice;
        this.mac = mac;

    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;

    }

    public String getNameDevice() {
        return nameDevice;
    }

    public String getMac() {
        return mac;
    }

    public void setRoom(CharSequence item) {
        this.room = (String) item;
    }

    public String getRoom() {
        return room;
    }
}
