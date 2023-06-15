package com.example.multiroomlocalization.Bluetooth;


import static com.example.multiroomlocalization.LoginActivity.btUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;

import com.example.multiroomlocalization.R;
import com.example.multiroomlocalization.speaker.Speaker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class ConnectBluetoothManager {


    private BluetoothDevice myDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private final Activity myActivity;
    private BluetoothA2dp bluetoothA2DP;
    private Method disconnectA2dp;

    private final ScanBluetoothService bluetoothService;
    private String macDeviceToConnect;
    private boolean requestDisconnectionA2DP = false;
    private boolean requestDisconnectionHeadset = false;
    private Speaker speakerToConnect;
    private boolean isSearching = false;
    public ConnectBluetoothManager(Activity activity, ScanBluetoothService bluetoothService) {

        bluetoothAdapter = activity.getSystemService(BluetoothManager.class).getAdapter();
        myActivity = activity;
        this.bluetoothService = bluetoothService;

        connectProxy();
    }

    private final ScanBluetoothService.OnDeviceFoundCallback callback = new ScanBluetoothService.OnDeviceFoundCallback() {
        @Override
        public void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device) {

            if(deviceHardwareAddress != null) {
                if (macDeviceToConnect.equals(deviceHardwareAddress) ) {

                    isSearching = false;
                    myDevice = device;
                    bluetoothService.interruptScan();
                    startConnection();
                }
            }
            else {
                showRequestConnectionDialog();
            }

        }
    };

    private void startBluetoothConnection(Speaker macDevice) {

        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @Override
            public void onEnabled() {
                macDeviceToConnect = macDevice.getMac();

                if(isSearching)
                    bluetoothService.newDeviceConnectionCallback(callback);
                else {
                    isSearching = true;
                    bluetoothService.addDeviceFoundCallbackAndScan(callback);
                }
            }

        });

    }


    //if device is not paired, i pair it
    private void ifNotBondedPair() {

        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {
                if (BluetoothDevice.BOND_NONE == myDevice.getBondState())
                    myDevice.createBond();
            }

        });

    }


    private void startConnection(){

        ifNotBondedPair();

        try {
            connectA2dp.invoke(bluetoothA2DP, myDevice);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private Method connectA2dp;
    private boolean requestConnection = false;
    // listener for service implementing a2dp profile proxy
    private final BluetoothProfile.ServiceListener profileListenerA2DP = new BluetoothProfile.ServiceListener() {


        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2DP = (BluetoothA2dp) proxy;
                try {

                    //get hidden method to connect device to proxy
                    connectA2dp = bluetoothA2DP.getClass().getMethod("connect", BluetoothDevice.class);
                    //get hidden method to disconnect device to proxy
                    disconnectA2dp = bluetoothA2DP.getClass().getMethod("disconnect", BluetoothDevice.class);
                    if(requestDisconnectionA2DP){
                        disconnectDevicesA2DP();
                        requestDisconnectionA2DP = false;
                    }

                    if(requestConnection) {
                        ifNotConnectedConnect(speakerToConnect);
                        requestConnection = false;
                    }

                } catch ( NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }

        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {


                btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onEnabled() {

                        disconnectDevicesA2DP();

                    }
                });

                bluetoothA2DP = null;
                myDevice = null;


            }
        }
    };
    private BluetoothHeadset bluetoothHeadset;
    private Method disconnectHeadset;
    private final BluetoothProfile.ServiceListener profileListenerHeadset = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
                try {

                    //get hidden method to disconnect device to proxy
                    disconnectHeadset = bluetoothHeadset.getClass().getMethod("disconnect", BluetoothDevice.class);

                    if(requestDisconnectionHeadset) {
                        disconnectDevicesHeadset();
                        requestDisconnectionHeadset = false;
                    }

                } catch ( NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }

        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {


                btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onEnabled() {

                        disconnectDevicesHeadset();

                    }
                });

                bluetoothHeadset= null;

            }
        }
    };

    private void connectProxy(){

        // Establish connection to the proxy.

        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @Override
            public void onEnabled() {
                bluetoothAdapter.getProfileProxy(myActivity, profileListenerA2DP, BluetoothProfile.A2DP);
                bluetoothAdapter.getProfileProxy(myActivity, profileListenerHeadset, BluetoothProfile.HEADSET);
            }
        });



    }

    public void connectDevice(Speaker device){
        speakerToConnect = device;
        if(bluetoothA2DP != null)
            ifNotConnectedConnect(device);
        else
            requestConnection = true;
    }

    private void ifNotConnectedConnect(Speaker device) {

        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {

                boolean connected = false;

                List<BluetoothDevice> listConnected = bluetoothA2DP.getConnectedDevices();
                for(int i=0; i < listConnected.size(); i++) {

                    if (listConnected.get(i).getAddress().equals(device.getMac()))
                        connected = true;
                    else {
                        try {
                            disconnectA2dp.invoke(bluetoothA2DP, listConnected.get(i));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
                disconnectDevicesHeadset();
                if(!connected){
                    startBluetoothConnection(device);
                }

            }
        });

    }


    private void disconnectDevicesA2DP(){
        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {

                if ( disconnectA2dp != null && bluetoothA2DP != null) {
                    bluetoothA2DP.getConnectedDevices().forEach(device -> {
                        try {

                            disconnectA2dp.invoke(bluetoothA2DP, device);

                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                else {
                    requestDisconnectionA2DP = true;
                }

            }
        });
    }
    private void disconnectDevicesHeadset(){

        btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {

                if ( disconnectHeadset != null && bluetoothHeadset != null) {
                    bluetoothHeadset.getConnectedDevices().forEach(device -> {
                        try {
                            disconnectHeadset.invoke(bluetoothHeadset, device);

                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    requestDisconnectionHeadset = true;
                }


            }
        });
    }

    private void showRequestConnectionDialog(){
        isSearching = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setMessage(R.string.dialog_request_connection_bt)
                .setPositiveButton(R.string.dialog_conferma, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(BluetoothAdapter.checkBluetoothAddress(macDeviceToConnect)) {
                            dialog.dismiss();
                            myDevice = bluetoothAdapter.getRemoteDevice(macDeviceToConnect);

                            startConnection();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }
    private void closeProxy(){

        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2DP);
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
        bluetoothA2DP = null;
        bluetoothHeadset = null;
        isSearching = false;
    }

    // Disconnect to service and device.
    public void disconnectEverything(boolean alsoProxy) {
        isSearching = false;
        disconnectDevicesA2DP();
        disconnectDevicesHeadset();
        if(alsoProxy)
            closeProxy();
    }
}