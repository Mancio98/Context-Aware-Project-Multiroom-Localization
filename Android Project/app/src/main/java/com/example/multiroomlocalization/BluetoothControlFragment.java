package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.BluetoothUtility.checkPermission;
import static com.example.multiroomlocalization.BluetoothUtility.getBondedDevices;
import static com.example.multiroomlocalization.BluetoothUtility.scan;
import static com.example.multiroomlocalization.MainActivity.btUtility;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothControlFragment {

    private final FragmentActivity myActivity;
    private final ListView listRooms;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private ConnectBluetoothThread connectBtThread;
    private final BluetoothDevice foundDevice = null;
    private final String[] roomsArray;
    private ListBluetoothAdapter roomsAdapter;

    public BluetoothControlFragment(Fragment fragment) {

        myActivity = fragment.requireActivity();

        //get bluetooth adapter, main object to perform all actions
        BluetoothManager bluetoothManager = myActivity.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        listRooms = (ListView) myActivity.findViewById(R.id.rooms_list);

        roomsArray = myActivity.getResources().getStringArray(R.array.rooms_array);
    }

    //return rooms choices both for discovered and paired devices (to finish)
    public ArrayList<ListRoomsElement> getRoomsChoices() {

        Set<BluetoothDevice> devicesChosen = new HashSet<>();
        ArrayList<ListRoomsElement> deviceForRoom = new ArrayList<>();
        for(int i=0; i<listRooms.getAdapter().getCount(); i++) {
            ListRoomsElement elem = (ListRoomsElement) listRooms.getAdapter().getItem(i);

            if(elem.getDevice() != null)
                if(!devicesChosen.add(elem.getDevice()))
                    return null;
                else
                    deviceForRoom.add(elem);

        }


        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        return deviceForRoom;
    }

    //setup adapter to display list on my view and start scanning
    public void setupBluetoothAndScan() {

        setupReceiver();


        if(btUtility.enableBluetooth(bluetoothAdapter)) {
            populateBondedDevices();
            scan(myActivity,bluetoothAdapter);
            // Register for broadcasts when a device is discovered.

        }
        else
            Toast.makeText(myActivity,"Enable Bluetooth to continue!",Toast.LENGTH_LONG).show();


    }

    //method to setup my listener for bluetooth actions
    private void setupReceiver(){

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        try {
            myActivity.registerReceiver(receiver, filter);
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
                checkPermission(myActivity);
                //check if is not already paired, so is not already on bonded list
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (deviceName != null) {
                        Log.i("devices_scan", deviceName);
                        if(!discoveredDevices.contains(device)) {
                            discoveredDevices.add(device);
                            roomsAdapter.addBluetoothDevice(device);
                        }
                         //add device on the list that user is looking
                    }
                    Log.i("devices_scan", deviceHardwareAddress);

                }

            //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");

            //when scanning is started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");

                List<ListRoomsElement> arrayRoomsElem = new ArrayList<>();
                for(String room : roomsArray)
                    arrayRoomsElem.add(new ListRoomsElement(room));
                roomsAdapter = new ListBluetoothAdapter(context, R.id.rooms_list, arrayRoomsElem, myActivity, pairedDevices);

                listRooms.setAdapter(roomsAdapter);

                discoveredDevices = new ArrayList<>();
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

    }

    public void closeControl(){


        checkPermission(myActivity);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        try{
            LocalBroadcastManager.getInstance(myActivity).unregisterReceiver(receiver);
            myActivity.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
