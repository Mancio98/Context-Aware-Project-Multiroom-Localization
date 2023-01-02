package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.BluetoothUtility.checkPermission;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanBluetoothControl {

    private final FragmentActivity myActivity;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher;
    private ConnectBluetoothThread connectBtThread;
    private final BluetoothDevice foundDevice = null;
    private ListView listDiscovered;
    private ListView listBonded;
    private ListBluetoothAdapter discoverAdapter;

    public ScanBluetoothControl(Fragment fragment) {

        activityLauncher = BetterActivityResult.registerActivityForResult(fragment);

        myActivity = fragment.getActivity();
        if (myActivity == null)
            return;
        BluetoothManager bluetoothManager = myActivity.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void getRoomsChoices() {

        for(int i=0; i<listBonded.getAdapter().getCount(); i++) {
            ListBluetoothElement elem = (ListBluetoothElement) listBonded.getAdapter().getItem(i);

            Log.i(elem.getNameDevice(),elem.getRoom());
        }
        for(int i=0; i<listDiscovered.getAdapter().getCount(); i++) {
            ListBluetoothElement elem = (ListBluetoothElement) listDiscovered.getAdapter().getItem(i);

            Log.i(elem.getNameDevice(),elem.getRoom());
        }


        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public void setupBluetoothAndScan() {

        listBonded = (ListView) myActivity.findViewById(R.id.bonded_list);
        listDiscovered = (ListView) myActivity.findViewById(R.id.discovered_list);

        if(enableBluetooth()) {
            scanAndSaveChoice();
            // Register for broadcasts when a device is discovered.
            setupReceiver();
        }
        else
            Toast.makeText(myActivity,"Enable Bluetooth to continue!",Toast.LENGTH_LONG);


    }

    private boolean enableBluetooth(){

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                AtomicBoolean enabled = new AtomicBoolean(false);
                activityLauncher.launch(enableBtIntent, result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        enabled.set(true);
                    else
                        enabled.set(false);
                });

                return enabled.get();
            }

        return true;
    }
    private void setupReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        myActivity.registerReceiver(receiver, filter);
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                checkPermission(myActivity);
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (deviceName != null) {
                        Log.i("devices_scan", deviceName);
                        discoverAdapter.add(new ListBluetoothElement(deviceName, deviceHardwareAddress));
                        discoverAdapter.notifyDataSetChanged();
                    }
                    Log.i("devices_scan", deviceHardwareAddress);

                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");


            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");

                discoverAdapter = new ListBluetoothAdapter(context,R.id.bonded_list);
                listDiscovered.setAdapter(discoverAdapter);

            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                // This is when we can be assured that fetchUuidsWithSdp has completed.
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        System.out.println("uuidExtra - " + p);
                    }
                    if (connectBtThread != null){
                        //connectBtThread.start();
                    }

                } else {
                    System.out.println("uuidExtra is still null");
                }

            }
        }
    };

    protected void scanAndSaveChoice() {

        checkPermission(myActivity);

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ListBluetoothAdapter bondedAdapter = new ListBluetoothAdapter(myActivity,R.id.bonded_list);

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.

            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                bondedAdapter.add(new ListBluetoothElement(deviceName,deviceHardwareAddress));
                Log.i("devices", deviceName);
                Log.i("devices", deviceHardwareAddress);
            }
        }

        listBonded.setAdapter(bondedAdapter);

        if (bluetoothAdapter.isDiscovering()) {

            bluetoothAdapter.cancelDiscovery();
        }

        Log.i("BT", String.valueOf(bluetoothAdapter.startDiscovery()));

    }

    public void closeControl(){

        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        myActivity.unregisterReceiver(receiver);
    }
}
