package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.MainActivity.BT_SCAN;
import static com.example.multiroomlocalization.MainActivity.btUtility;

import android.annotation.SuppressLint;
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
    private ArrayAdapter<String> myAdapter;


    public ReferencePointListAdapter(ArrayList<ReferencePoint> arrList, Context context, Activity activity){
        this.list = arrList;
        this.context = context;
        this.activity = activity;
        this.scanBluetoothManager = new ScanBluetooth(context, activity);
        scanBluetoothManager.setFoundCallback(new ScanBluetooth.OnDeviceFoundCallback() {
            @Override
            public void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device) {

                if(deviceName == null)
                    return;

                boolean found = false;
                int i = 0;
                while (!found && i < itemsBluetooth.size()) {
                    if (itemsBluetooth.get(i).getMac().equals(deviceHardwareAddress))
                        found = true;
                }
                if (!found) {
                    itemsBluetooth.add(new Speaker(deviceName,deviceHardwareAddress));
                    myAdapter.add(deviceName);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });

        this.itemsBluetooth = new ArrayList<>();
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


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReferencePointHolder holder, int position) {
        ReferencePoint currentData = list.get(position);

        holder.referencePointName.setText(currentData.getId());

        ArrayList<String> speakerName = new ArrayList<>();
        speakerName.add("No Music");
        itemsBluetooth = Speaker.getListSpeakerFromDevice(scanBluetoothManager.getPairedDevices());

        itemsBluetooth.forEach((speaker) -> {
            speakerName.add(speaker.getName());
            System.out.println(speaker.getName());
        });

        myAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, speakerName);

        holder.spinnerBluetooth.setAdapter(myAdapter);
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

        scanBluetoothManager.setupBluetoothAndScan();

    }



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