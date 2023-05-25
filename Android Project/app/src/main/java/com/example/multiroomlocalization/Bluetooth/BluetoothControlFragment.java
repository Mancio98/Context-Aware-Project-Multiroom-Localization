package com.example.multiroomlocalization.Bluetooth;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.checkPermission;
import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.getBondedDevices;
import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.scan;
import static com.example.multiroomlocalization.MainActivity.btUtility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.multiroomlocalization.ListRoomsElement;
import com.example.multiroomlocalization.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothControlFragment {

    private final FragmentActivity myActivity;
    private final ListView listRooms;

    private final BluetoothDevice foundDevice = null;
    private final String[] roomsArray;
    private ListBluetoothAdapter roomsAdapter;
    private final ScanBluetooth scanBluetoothManager;
    private ArrayList<BluetoothDevice> discoveredDevices;
    public BluetoothControlFragment(Fragment fragment) {

        myActivity = fragment.requireActivity();

        listRooms = (ListView) myActivity.findViewById(R.id.rooms_list);

        roomsArray = myActivity.getResources().getStringArray(R.array.rooms_array);

        scanBluetoothManager = new ScanBluetooth(fragment.getContext(), myActivity, receiver);
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

        scanBluetoothManager.interruptScan();

        return deviceForRoom;
    }


    public void startScanning(){
        scanBluetoothManager.setupBluetoothAndScan();
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
                        Log.i("devices_scan", deviceHardwareAddress);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            System.out.println(device.getBluetoothClass().doesClassMatch(BluetoothClass.PROFILE_A2DP));

                            System.out.println(device.getBluetoothClass().doesClassMatch(BluetoothClass.PROFILE_HID));
                            System.out.println(device.getBluetoothClass().doesClassMatch(BluetoothClass.PROFILE_HEADSET));
                        }
                        else{
                            System.out.println("device type: "+device.getBluetoothClass().getDeviceClass());
                            System.out.println("device uuid: "+ Arrays.toString(device.getUuids()));


                        }


                        if(!discoveredDevices.contains(device)) {
                            discoveredDevices.add(device);
                            roomsAdapter.addBluetoothDevice(device);
                        }
                         //add device on the list that user is looking
                    }


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
                roomsAdapter = new ListBluetoothAdapter(context, R.id.rooms_list, arrayRoomsElem,
                        myActivity, scanBluetoothManager.getPairedDevices());

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

                } else {
                    System.out.println("uuidExtra is still null");
                }

            }
        }
    };


    public void closeControl(){
        scanBluetoothManager.interruptScan();
        scanBluetoothManager.unregisterReceiver();
    }
}
