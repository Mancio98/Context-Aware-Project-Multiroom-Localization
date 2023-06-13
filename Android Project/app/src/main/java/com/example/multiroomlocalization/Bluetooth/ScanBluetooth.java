package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.MainActivity.btUtility;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;

import com.example.multiroomlocalization.MainActivity;

import java.util.Set;

public class ScanBluetooth {

    private Context context;
    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    protected Set<BluetoothDevice> pairedDevices;

    private OnDeviceFoundCallback foundCallback;
    public interface OnDeviceFoundCallback {
        void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device);

    }
    public void setFoundCallback(OnDeviceFoundCallback foundCallback) {
        this.foundCallback = foundCallback;
    }

    public ScanBluetooth(Context context, Activity myActivity) {
        this.context = context;
        this.activity = myActivity;
        BluetoothManager bluetoothManager = myActivity.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        this.pairedDevices = new ArraySet<>();
    }

    public Set<BluetoothDevice> getPairedDevices() {
        pairedDevices = btUtility.getBondedDevices(bluetoothAdapter, activity);
        return pairedDevices;
    }


    //setup adapter to display list on my view and start scanning
    public void setupBluetoothAndScan() {

        setupReceiver();

        if(btUtility.enableBluetooth(bluetoothAdapter))
            startScan();
        else
            Toast.makeText(activity,"Enable Bluetooth to continue!",Toast.LENGTH_LONG).show();
    }

    private void startScan(){
        if (btUtility.checkPermission(activity, new MainActivity.BluetoothPermCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {

                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }

                bluetoothAdapter.startDiscovery();
            }
        })) {

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            //if false error
            bluetoothAdapter.startDiscovery();
        }
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
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //when i found a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(btUtility.checkPermission(activity, new MainActivity.BluetoothPermCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onGranted() {
                        if (device != null) {

                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address


                            foundCallback.onFound(deviceName, deviceHardwareAddress, device);


                            //add device on the list that user is looking
                        }
                    }
                })) {
                    //check if is not already paired, so is not already on bonded list
                    if (device != null) {

                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address


                        foundCallback.onFound(deviceName, deviceHardwareAddress, device);


                        //add device on the list that user is looking
                    }
                }

                //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");

                //when scanning is started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");


                //use it when i call fetchforuuid method to get fresh uuid (probably i will delete it)
            }

        }
    };

    public void interruptScan(){
        if(btUtility.checkPermission(activity, new MainActivity.BluetoothPermCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {
                if (bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();
            }
        })) {
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
        }
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
