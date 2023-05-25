package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.MainActivity.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.core.app.ActivityCompat;

import com.example.multiroomlocalization.BetterActivityResult;
import com.example.multiroomlocalization.MainActivity;
import com.example.multiroomlocalization.speaker.Speaker;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothUtility {
    BetterActivityResult<Intent, ActivityResult> activityLauncher;
    public static final int BT_CONNECT_AND_SCAN = 101;
    public BluetoothUtility(ActivityResultCaller caller) {

        activityLauncher = BetterActivityResult.registerActivityForResult(caller);
    }

    //method for scan bluetooth devices around me
    public static void scan(Activity activity, BluetoothAdapter bluetoothAdapter){
        checkPermission(activity);
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        //if false error
        Log.i("BT", String.valueOf(bluetoothAdapter.startDiscovery()));
    }

    //method to query user to enable bluetooth
    public boolean enableBluetooth(BluetoothAdapter bluetoothAdapter){


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            AtomicBoolean enabled = new AtomicBoolean(false);
            activityLauncher.launch(enableBtIntent, result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    enabled.set(true);
                else
                    enabled.set(false); // if false cannot use my application
            });

            return enabled.get();
        }

        return true;
    }

    //method to get list of already paired devices
    public static Set<BluetoothDevice> getBondedDevices(BluetoothAdapter bluetoothAdapter, Activity activity){
        checkPermission(activity);
        return bluetoothAdapter.getBondedDevices();
    }

    //method to check if user have permitted bluetooth functionalities
    public static boolean checkPermission(Activity activity) {



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        BT_CONNECT_AND_SCAN);
                return false;
            }
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        BT_CONNECT_AND_SCAN);
                return false;
            }
        }
        else
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        BT_CONNECT_AND_SCAN);
                return false;
            }

        return true;
    }

    private BroadcastReceiver connectA2dpReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            Log.d("a2dp", "receive intent for action : " + action);
            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    //setIsA2dpReady(true);
                    Log.i("connesso2", "diocane");
                    //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                    //setIsA2dpReady(false);
                }
            } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                if (state == BluetoothA2dp.STATE_PLAYING) {
                    Log.d("a2dp", "A2DP start playing");
                    Toast.makeText(activity, "A2dp is playing", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("a2dp", "A2DP not playing");
                    Toast.makeText(activity, "A2dp not playing", Toast.LENGTH_SHORT).show();
                }
            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){

                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
                if (state == BluetoothDevice.BOND_BONDED) {
                    Log.d("bonded", "bonded");

                } else {
                    Log.d("bonded", "not bonded");

                }
            }
            else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                // This is when we can be assured that fetchUuidsWithSdp has completed.
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        System.out.println("uuidExtra - " + p);
                    }
                    /*
                    if (connectBluetoothThread != null){
                        connectBluetoothThread.start();
                    }*/

                } else {
                    System.out.println("uuidExtra is still null");
                }

            }

        }
    };

    public BroadcastReceiver getConnectA2dpReceiver() {
        return connectA2dpReceiver;
    }
}
