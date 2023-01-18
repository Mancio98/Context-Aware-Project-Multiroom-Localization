package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.MainActivity.BT_CONNECT_AND_SCAN;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.core.app.ActivityCompat;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothUtility {


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
    public static boolean enableBluetooth(BluetoothAdapter bluetoothAdapter, ActivityResultCaller caller){

        BetterActivityResult<Intent, ActivityResult> activityLauncher;
        activityLauncher = BetterActivityResult.registerActivityForResult(caller);

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
}
