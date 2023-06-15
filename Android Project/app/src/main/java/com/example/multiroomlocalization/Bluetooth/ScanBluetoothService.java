package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.LoginActivity.btUtility;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.multiroomlocalization.MainActivity;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class ScanBluetoothService extends Service {



    public class LocalBinder extends Binder {
        public ScanBluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return ScanBluetoothService.this;
        }
    }
    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;

    private OnDeviceFoundCallback currentFoundCallback;

    private LinkedList<OnDeviceFoundCallback> queueCallback;
    private Context context;
    public ScanBluetoothService() {}

    public interface OnDeviceFoundCallback {
        void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device);

    }

    public interface getPairedCallback {

        void onResult(Set<BluetoothDevice> list);
    }

    public void newDeviceConnectionCallback(OnDeviceFoundCallback callback){

        System.out.println("newDeviceConnection");
        btUtility.enableBluetooth(bluetoothAdapter, new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {
                if (!bluetoothAdapter.isDiscovering()) {
                    currentFoundCallback = callback;
                    bluetoothAdapter.startDiscovery();
                }
                else {
                    currentFoundCallback = null;
                    queueCallback.addFirst(callback);
                    interruptScan();
                }
            }

        });

    }

    public void addDeviceFoundCallbackAndScan(OnDeviceFoundCallback callback){
        System.out.println("addDeviceFoundCallback");
        if(callback!= null)
            queueCallback.add(callback);

        btUtility.enableBluetooth(bluetoothAdapter, new BluetoothUtility.OnEnableBluetooth() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEnabled() {
                if (!bluetoothAdapter.isDiscovering() && !queueCallback.isEmpty()) {

                    currentFoundCallback = queueCallback.poll();
                    bluetoothAdapter.startDiscovery();

                }
            }

        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    public void getPairedDevices(getPairedCallback callback) {

        btUtility.getBondedDevices(bluetoothAdapter, callback);

    }


    public void setContext(Context applicationContext) {
        context = applicationContext;
        setupService();
    }

    private void setupService() {
        bluetoothAdapter = getSystemService(BluetoothManager.class).getAdapter();
        queueCallback = new LinkedList<>();
        setupReceiver();
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
                if (device != null) {

                    btUtility.checkPermission(new MainActivity.BluetoothPermCallback() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onGranted() {

                            System.out.println(device.getName());
                            System.out.println(device.getBluetoothClass().getDeviceClass());
                            if(device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                                    device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER) {
                                String deviceName = device.getName();
                                String deviceHardwareAddress = device.getAddress(); // MAC address

                                //System.out.println(deviceName);

                                if(currentFoundCallback!= null && deviceHardwareAddress != null)
                                    currentFoundCallback.onFound(deviceName, deviceHardwareAddress, device);

                            }
                        }
                    });
                }

                //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if(currentFoundCallback != null) {
                    currentFoundCallback.onFound(null, null, null);
                    currentFoundCallback = null;
                }
                if(!queueCallback.isEmpty()) {
                    currentFoundCallback = queueCallback.poll();
                    bluetoothAdapter.startDiscovery();
                }
                Log.i("devices_scan", "finished");

                //when scanning is started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");



            }

        }
    };

    public void interruptScan(){
        btUtility.checkPermission(new MainActivity.BluetoothPermCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {
                if (bluetoothAdapter.isDiscovering()) {
                    currentFoundCallback = null;
                    bluetoothAdapter.cancelDiscovery();
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    public void unregisterReceiver(){

        try{

            context.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
