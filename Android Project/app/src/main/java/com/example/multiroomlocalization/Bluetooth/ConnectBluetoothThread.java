package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.checkPermission;


import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;

import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectBluetoothThread extends Thread {

    private BluetoothSocket mySocket;
    private BluetoothDevice myDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private final Activity myActivity;
    private BluetoothA2dp bluetoothA2DP;
    private Method disconnectA2dp;
    public ConnectBluetoothThread(Activity activity, BluetoothAdapter adapter) {

        bluetoothAdapter = adapter;
        myActivity = activity;

    }

    private UUID getUuid() {

        checkPermission(myActivity);
        ParcelUuid[] uuids = myDevice.getUuids();

        if( uuids != null) {
            for(ParcelUuid uuid : uuids)
                Log.i("uuid1",uuid.getUuid().toString());
            return myDevice.getUuids()[0].getUuid();
        }
        else
            return null;
    }

    private void createSocket(){


        checkPermission(myActivity);
        try {
            UUID uuid = getUuid();
            if(uuid != null) {
                Log.i("uuid", String.valueOf(uuid));
                mySocket = myDevice.createRfcommSocketToServiceRecord(UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb"));
            }
            else
                Log.i("uuid","not found");
        } catch (IOException e) {
            Log.e("conn", "Socket's create() method failed", e);
            e.printStackTrace();
        }

    }
    private void establishConnection() {

        checkPermission(myActivity);

        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.

            mySocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Log.e("connect", "Could not connect", connectException);
            try {
                mySocket.close();
            } catch (IOException closeException) {
                Log.e("connect", "Could not close the client socket", closeException);
            }
        }
    }

    public void connectDevice(BluetoothDevice device) {

        myDevice = device;

        start();

    }

    //if device is not paired, i pair it
    private boolean ifNotBondedPair() {

        checkPermission(myActivity);

        if(BluetoothDevice.BOND_NONE == myDevice.getBondState()) {
            myDevice.createBond();
            return true;
        }
        else
            return false;
    }

    boolean mIsA2dpReady = false;

    void setIsA2dpReady(boolean ready) {
        mIsA2dpReady = ready;
        Toast.makeText(myActivity, "A2DP ready ? " + (ready ? "true" : "false"), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void run() {
        super.run();

        ifNotBondedPair();
        connectProxyA2DP();
    }

    // listener for service implementing a2dp profile proxy
    private final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2DP = (BluetoothA2dp) proxy;
                try {
                    //get hidden method to connect device to proxy
                    Method connectA2dp = bluetoothA2DP.getClass().getMethod("connect", BluetoothDevice.class);
                    //get hidden method to disconnect device to proxy
                    disconnectA2dp = bluetoothA2DP.getClass().getMethod("disconnect", BluetoothDevice.class);
                    //call connect
                    connectA2dp.invoke(bluetoothA2DP,myDevice);
                } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                try {
                    disconnectA2dp.invoke(bluetoothAdapter,myDevice);
                    bluetoothA2DP = null;
                    myDevice = null;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private void connectProxyA2DP(){

        // Establish connection to the proxy.
        bluetoothAdapter.getProfileProxy(myActivity, profileListener, BluetoothProfile.A2DP);

    }


    // Disconnect to service and device.
    public void disconnect() {

        checkPermission(myActivity);
        if(bluetoothA2DP.getConnectedDevices().size() > 0)
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2DP);

    }
}
