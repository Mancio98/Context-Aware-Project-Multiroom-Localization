package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.BluetoothUtility.checkPermission;


import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import kotlin.jvm.internal.FunctionImpl;

public class ConnectBluetoothThread extends Thread {

    private BluetoothSocket mySocket;
    private final BluetoothDevice myDevice;
    private BluetoothAdapter bluetoothAdapter;
    private Activity myActivity;
    private BluetoothA2dp bluetoothA2DP;
    private Method connectA2dp;
    private Method disconnectA2dp;
    public ConnectBluetoothThread(BluetoothDevice device, Activity activity, BluetoothAdapter adapter) {

        bluetoothAdapter = adapter;
        myActivity = activity;
        myDevice = device;

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
            return;
        }
    }

    public void run() {

        ifNotBondedPair();
        connectProxyA2DP();

    }

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


    BroadcastReceiver connectA2dpReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            Log.d("a2dp", "receive intent for action : " + action);
            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    setIsA2dpReady(true);
                    Log.i("connesso", "diocane");


                } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                    setIsA2dpReady(false);
                }
            } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                if (state == BluetoothA2dp.STATE_PLAYING) {
                    Log.d("a2dp", "A2DP start playing");
                    Toast.makeText(myActivity, "A2dp is playing", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("a2dp", "A2DP stop playing");
                    Toast.makeText(myActivity, "A2dp is stopped", Toast.LENGTH_SHORT).show();
                }
            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){

                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
                if (state == BluetoothDevice.BOND_BONDED) {
                    Log.d("bonded", "bonded");


                } else {
                    Log.d("bonded", "not bonded");

                }
            }

        }
    };



    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2DP = (BluetoothA2dp) proxy;


                try {

                    connectA2dp = bluetoothA2DP.getClass().getMethod("connect", new Class[] {BluetoothDevice.class});
                    disconnectA2dp = bluetoothA2DP.getClass().getMethod("disconnect", new Class[] {BluetoothDevice.class});

                    connectA2dp.invoke(bluetoothA2DP,myDevice);
                } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2DP = null;
            }
        }
    };
    private void connectProxyA2DP(){


        // Establish connection to the proxy.
        bluetoothAdapter.getProfileProxy(myActivity, profileListener, BluetoothProfile.A2DP);

    }



    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2DP);
            mySocket.close();

        } catch (IOException e) {
            Log.e("connect", "Could not close the client socket", e);
        }


        interrupt();
    }
}
