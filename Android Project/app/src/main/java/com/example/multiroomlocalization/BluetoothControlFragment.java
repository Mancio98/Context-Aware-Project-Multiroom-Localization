package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.BluetoothUtility.checkPermission;
import static com.example.multiroomlocalization.BluetoothUtility.enableBluetooth;
import static com.example.multiroomlocalization.BluetoothUtility.getBondedDevices;
import static com.example.multiroomlocalization.BluetoothUtility.scan;

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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Set;

public class BluetoothControlFragment {

    private final FragmentActivity myActivity;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ConnectBluetoothThread connectBtThread;
    private final BluetoothDevice foundDevice = null;
    private ListView listDiscovered;
    private ListView listBonded;
    private ListBluetoothAdapter discoverAdapter;

    public BluetoothControlFragment(Fragment fragment) {


        myActivity = fragment.getActivity();
        if (myActivity == null)
            return;

        //get bluetooth adapter, main object to perform all actions
        BluetoothManager bluetoothManager = myActivity.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    //return rooms choices both for discovered and paired devices (to finish)
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

    //setup adapter to display list on my view and start scanning
    public void setupBluetoothAndScan() {

        listBonded = (ListView) myActivity.findViewById(R.id.bonded_list);
        listDiscovered = (ListView) myActivity.findViewById(R.id.discovered_list);

        if(enableBluetooth(bluetoothAdapter,myActivity)) {
            populateBondedDevices();
            scan(myActivity,bluetoothAdapter);
            // Register for broadcasts when a device is discovered.
            setupReceiver();
        }
        else
            Toast.makeText(myActivity,"Enable Bluetooth to continue!",Toast.LENGTH_LONG);


    }

    //method to setup my listener for bluetooth actions
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

            //when i found a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                checkPermission(myActivity);
                //check if is not already paired, so is not already on bonded list
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (deviceName != null) {
                        Log.i("devices_scan", deviceName);
                        discoverAdapter.add(new ListBluetoothElement(deviceName, deviceHardwareAddress));
                        discoverAdapter.notifyDataSetChanged(); //add device on the list that user is looking
                    }
                    Log.i("devices_scan", deviceHardwareAddress);

                }

            //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");

            //when scanning is started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");

                discoverAdapter = new ListBluetoothAdapter(context,R.id.bonded_list);
                listDiscovered.setAdapter(discoverAdapter);
            //use it when i call fetchforuuid method to get fresh uuid (probably i will delete it)
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

    //populate adapter with list of bonded devices
    protected void populateBondedDevices() {

        pairedDevices = getBondedDevices(bluetoothAdapter,myActivity);
        ListBluetoothAdapter bondedAdapter = new ListBluetoothAdapter(myActivity,R.id.bonded_list);

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            checkPermission(myActivity);
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                bondedAdapter.add(new ListBluetoothElement(deviceName,deviceHardwareAddress));
                Log.i("devices", deviceName);
                Log.i("devices", deviceHardwareAddress);
            }
        }

        listBonded.setAdapter(bondedAdapter);

    }

    public void closeControl(){

        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        myActivity.unregisterReceiver(receiver);
    }
}
