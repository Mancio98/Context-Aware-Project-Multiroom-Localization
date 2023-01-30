package com.example.multiroomlocalization;

import android.bluetooth.BluetoothDevice;
import android.widget.Spinner;

public class ListRoomsElement {

    String name;
    BluetoothDevice device;
    Spinner spinner;
    public ListRoomsElement(String name) {
        this.name = name;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    public String getName() {
        return name;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
