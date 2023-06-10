package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.ActivityLive.btUtility;
import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.checkPermission;
import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.getBondedDevices;
import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.scan;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.ArraySet;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ScanBluetooth {

    private Context context;
    private Activity myActivity;
    private BluetoothAdapter bluetoothAdapter;
    protected Set<BluetoothDevice> pairedDevices;
    private final BroadcastReceiver receiver;


    public ScanBluetooth(Context context, Activity myActivity, BroadcastReceiver receiver) {
        this.context = context;
        this.myActivity = myActivity;
        this.receiver = receiver;
        BluetoothManager bluetoothManager = myActivity.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        this.pairedDevices = new ArraySet<>();
    }


    public Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }


    //setup adapter to display list on my view and start scanning
    public void setupBluetoothAndScan() {

        setupReceiver();

        if(btUtility.enableBluetooth(bluetoothAdapter)) {
            populateBondedDevices();
            scan(myActivity,bluetoothAdapter);
            // Register for broadcasts when a device is discovered.
        }
        else
            Toast.makeText(myActivity,"Enable Bluetooth to continue!",Toast.LENGTH_LONG).show();
    }

    private void setupReceiver(){

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        try {
            context.registerReceiver(receiver, filter);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //populate adapter with list of bonded devices
    private void populateBondedDevices() {

        pairedDevices = getBondedDevices(bluetoothAdapter,myActivity);

    }

    public void interruptScan(){
        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public void unregisterReceiver(){

        try{
            //LocalBroadcastManager.getInstance(myActivity).unregisterReceiver(receiver);
            context.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
