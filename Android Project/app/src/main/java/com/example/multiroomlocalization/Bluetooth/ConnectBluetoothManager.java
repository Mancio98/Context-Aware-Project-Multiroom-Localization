package com.example.multiroomlocalization.Bluetooth;


import static com.example.multiroomlocalization.MainActivity.btUtility;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;


import com.example.multiroomlocalization.MainActivity;
import com.example.multiroomlocalization.speaker.Speaker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConnectBluetoothManager {


    private BluetoothDevice myDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private final Activity myActivity;
    private BluetoothA2dp bluetoothA2DP;
    private Method disconnectA2dp;

    private final ScanBluetooth scanBluetoothManager;
    private String macDeviceToConnect;

    public ConnectBluetoothManager(Activity activity) {

        bluetoothAdapter = activity.getSystemService(BluetoothManager.class).getAdapter();
        myActivity = activity;
        scanBluetoothManager = new ScanBluetooth(activity.getApplicationContext(),myActivity);
        scanBluetoothManager.setFoundCallback(new ScanBluetooth.OnDeviceFoundCallback() {
            @Override
            public void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device) {
                if (macDeviceToConnect.equals(deviceHardwareAddress)) {
                    myDevice = device;
                    scanBluetoothManager.interruptScan();
                    startConnection();
                }
            }
        });
    }

    private void startBluetoothConnection(Speaker macDevice) {

        btUtility.enableBluetooth(bluetoothAdapter, myActivity, new BluetoothUtility.OnEnableBluetooth() {
            @Override
            public void onEnabled() {
                macDeviceToConnect = macDevice.getMac();
                scanBluetoothManager.setupBluetoothAndScan();
            }

        });

    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //when i found a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btUtility.enableBluetooth(bluetoothAdapter, myActivity, new BluetoothUtility.OnEnableBluetooth() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onEnabled() {
                        if (device != null) {
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            System.out.println("name: " + device.getName() + " mac: " + deviceHardwareAddress);


                            if (macDeviceToConnect.equals(deviceHardwareAddress)) {
                                myDevice = device;
                                scanBluetoothManager.interruptScan();
                                startConnection();
                            }

                        }
                    }

                });


                //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");
                scanBluetoothManager.unregisterReceiver();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");

                //use it when i call fetchforuuid method to get fresh uuid (probably i will delete it)
            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                // This is when we can be assured that fetchUuidsWithSdp has completed.
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        System.out.println("uuidExtra - " + p);
                    }

                } else {
                    System.out.println("uuidExtra is still null");
                }

            }
        }
    };




    //if device is not paired, i pair it
    private void ifNotBondedPair() {

        btUtility.enableBluetooth(bluetoothAdapter, myActivity, new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {
                if (BluetoothDevice.BOND_NONE == myDevice.getBondState())
                    myDevice.createBond();
            }

        });

    }

    boolean mIsA2dpReady = false;

    void setIsA2dpReady(boolean ready) {
        mIsA2dpReady = ready;
        Toast.makeText(myActivity, "A2DP ready ? " + (ready ? "true" : "false"), Toast.LENGTH_SHORT).show();
    }


    private void startConnection(){

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

    public void connectDevice(Speaker device){

        disconnect();
        startBluetoothConnection(device);
    }
    // Disconnect to service and device.
    public void disconnect() {


        btUtility.checkPermission(myActivity, new MainActivity.BluetoothPermCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {
                if (bluetoothA2DP != null) {
                    if (bluetoothA2DP.getConnectedDevices().size() > 0)
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2DP);
                }
            }
        });

    }
}
