package com.example.multiroomlocalization;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;

import java.util.ArrayList;
import java.util.Set;

public class ReferencePointListAdapter extends RecyclerView.Adapter<ReferencePointHolder> {

    private final ArrayList<ReferencePoint> listReferencePoint;
    private final ArrayList<Speaker> itemsBluetooth;

    private final Context context;
    private final ScanBluetoothService serviceBluetooth;

    public ReferencePointListAdapter(ArrayList<ReferencePoint> arrList, Context context, ScanBluetoothService serviceBluetooth){


        listReferencePoint = arrList;
        this.context = context;
        this.serviceBluetooth = serviceBluetooth;
        this.itemsBluetooth = new ArrayList<>();
        populateItemsBluetooth();
    }


    private void populateItemsBluetooth(){
        serviceBluetooth.getPairedDevices(new ScanBluetoothService.getPairedCallback() {
            @Override
            public void onResult(Set<BluetoothDevice> list) {

                itemsBluetooth.clear();
                itemsBluetooth.addAll(Speaker.getListSpeakerFromDevice(list));
                notifyDataSetChanged();
                serviceBluetooth.addDeviceFoundCallbackAndScan(new ScanBluetoothService.OnDeviceFoundCallback() {
                    @Override
                    public void onFound(String deviceName, String deviceHardwareAddress, BluetoothDevice device) {

                        if(deviceName == null)
                            return;

                        boolean found = false;
                        int i = 0;
                        while (!found && i < itemsBluetooth.size()) {
                            if (itemsBluetooth.get(i).getMac().equals(deviceHardwareAddress))
                                found = true;
                            i++;
                        }
                        if (!found) {
                            itemsBluetooth.add(new Speaker(deviceName,deviceHardwareAddress));
                            notifyDataSetChanged();

                        }
                    }
                });

            }
        });



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

        ReferencePoint currentData = listReferencePoint.get(position);

        holder.referencePointName.setText(currentData.getId());

        ArrayList<String> speakerName = new ArrayList<>();
        speakerName.add("No Music");
        ArrayList<String> speakerMac = new ArrayList<>();
        speakerMac.add("");

        itemsBluetooth.forEach((speaker) -> {
            speakerName.add(speaker.getName());
            speakerMac.add(speaker.getMac());

        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, speakerName);
        holder.spinnerBluetooth.setAdapter(adapter);


        holder.spinnerBluetooth.setSelection(0);


        if (currentData.getSpeaker() != null){

            int index = speakerMac.indexOf(currentData.getSpeaker().getMac());

            holder.spinnerBluetooth.setSelection(index);

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

    public void closeBluetoothScan(){
        serviceBluetooth.interruptScan();
    }

    @Override
    public int getItemCount() {
        return listReferencePoint.size();
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull ReferencePointHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

    }

}