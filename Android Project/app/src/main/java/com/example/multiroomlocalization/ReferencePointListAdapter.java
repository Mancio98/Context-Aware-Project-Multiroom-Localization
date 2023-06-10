package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.checkPermission;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.Bluetooth.ScanBluetooth;
import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;

import java.util.ArrayList;
import java.util.Arrays;

public class ReferencePointListAdapter extends RecyclerView.Adapter<ReferencePointHolder> {

    private Activity activity;
    private ArrayList<ReferencePoint> list;
    private ArrayList<Speaker> itemsBluetooth;

    private Context context;
    private ScanBluetooth scanBluetoothManager;
    public ReferencePointListAdapter(ArrayList<ReferencePoint> arrList, Context context, Activity activity){
        this.list = arrList;
        this.context = context;
        this.activity = activity;
        this.scanBluetoothManager = new ScanBluetooth(context, activity, receiver);

        this.itemsBluetooth = Speaker.getListSpeakerFromDevice(scanBluetoothManager.getPairedDevices());
    }


    @NonNull
    @Override
    public ReferencePointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        View referencePointRow
                = inflater
                .inflate(R.layout.referencepoint_row,
                        parent, false);


        ReferencePointHolder viewHolder = new ReferencePointHolder(referencePointRow);

        viewHolder.dndSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });

        scanBluetoothManager.setupBluetoothAndScan();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReferencePointHolder holder, int position) {
        ReferencePoint currentData = list.get(position);

        holder.referencePointName.setText(currentData.getId());

        ArrayList<String> speakerName = new ArrayList<>();
        speakerName.add("No Music");
        for (int i=0; i< itemsBluetooth.size();i++){
            speakerName.add(itemsBluetooth.get(i).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, speakerName);

        holder.spinnerBluetooth.setAdapter(adapter);
        System.out.println(currentData.getSpeaker());

        if (currentData.getSpeaker() != null){
            if(currentData.getSpeaker().getName() != null){
                holder.spinnerBluetooth.setSelection(speakerName.indexOf(currentData.getSpeaker().getName()));
            }
            else holder.spinnerBluetooth.setSelection(0);

        }

        holder.spinnerBluetooth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    currentData.setSpeaker(new Speaker(null, null, null));
                }
                else {
                    currentData.setSpeaker(itemsBluetooth.get(i-1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.dndSwitch.setChecked(currentData.getDnd());
        holder.dndSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentData.setDnd(b);
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
                checkPermission(activity);
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
                        } else {
                            System.out.println("device type: " + device.getBluetoothClass().getDeviceClass());
                            System.out.println("device uuid: " + Arrays.toString(device.getUuids()));

                        }


                        boolean found = false;
                        int i = 0;
                        while (!found && i < itemsBluetooth.size()) {
                            if (itemsBluetooth.get(i).getMac().equals(deviceHardwareAddress))
                                found = true;
                        }
                        if (!found) {
                            itemsBluetooth.add(new Speaker(deviceName, deviceHardwareAddress));
                            notifyDataSetChanged();
                        }

                    }
                    //add device on the list that user is looking
                }

                //when scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.i("devices_scan", "finished");

                //when scanning is started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Log.i("devices_scan", "started");


                //use it when i call fetchforuuid method to get fresh uuid (probably i will delete it)
            }

        }
    };

    public void interruptScan(){
        scanBluetoothManager.interruptScan();
        scanBluetoothManager.unregisterReceiver();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(
            RecyclerView recyclerView)

    {

        super.onAttachedToRecyclerView(recyclerView);
    }

}