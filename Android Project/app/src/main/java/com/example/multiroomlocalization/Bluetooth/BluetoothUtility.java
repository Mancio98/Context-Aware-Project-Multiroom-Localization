package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.MainActivity.btPermissionCallback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.example.multiroomlocalization.BetterActivityResult;
import com.example.multiroomlocalization.MainActivity;

public class BluetoothUtility {

    private static BetterActivityResult<Intent, ActivityResult> activityLauncher;
    public static final int BT_CONNECT_AND_SCAN = 101;
    private OnEnableBluetooth callbackOnEnable;

    private Activity activity;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private BluetoothAdapter bluetoothAdapter;

    public interface OnEnableBluetooth {

        void onEnabled();

        default void onDisabled(Activity activity) {
            Toast.makeText(activity, "Enable bluetooth is mandatory", Toast.LENGTH_LONG).show();
        }
    }

    public BluetoothUtility (ActivityResultCaller caller, Activity activity){
        this.activity = activity;
        //activityLauncher = BetterActivityResult.registerActivityForResult(caller);
        initActivityResult(caller);
        bluetoothAdapter = activity.getSystemService(BluetoothManager.class).getAdapter();
    }



    //method to query user to enable bluetooth
    public void enableBluetooth(BluetoothAdapter bluetoothAdapters, OnEnableBluetooth callback) {


        callbackOnEnable = callback;

        checkPermission(new MainActivity.BluetoothPermCallback() {
            @Override
            public void onGranted() {

                if (!bluetoothAdapter.isEnabled()) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                    someActivityResultLauncher.launch(enableBtIntent);
                }
                else
                    callback.onEnabled();
            }
        });


    }

    //method to get list of already paired devices
    public void getBondedDevices(BluetoothAdapter bluetoothAdapter, ScanBluetoothService.getPairedCallback callback) {


        enableBluetooth(bluetoothAdapter, new OnEnableBluetooth() {
                @SuppressLint("MissingPermission")
                @Override
                public void onEnabled() {
                    System.out.println("onEnabled");
                    callback.onResult(bluetoothAdapter.getBondedDevices());
                }


            });

    }

    private void initActivityResult(ActivityResultCaller caller){

         someActivityResultLauncher = caller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == Activity.RESULT_OK) {
                callbackOnEnable.onEnabled();

            } else {

                callbackOnEnable.onDisabled(activity);

            }
        });

    }


    //method to check if user have permitted bluetooth functionalities
    public void checkPermission(MainActivity.BluetoothPermCallback callback) {


        int requestCode = BT_CONNECT_AND_SCAN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                btPermissionCallback = callback;
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        requestCode);


            }else
                callback.onGranted();
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                btPermissionCallback = callback;
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        requestCode);

            }else
                callback.onGranted();
        }
        else if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                btPermissionCallback = callback;
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);

        } else
            callback.onGranted();


    }


}
