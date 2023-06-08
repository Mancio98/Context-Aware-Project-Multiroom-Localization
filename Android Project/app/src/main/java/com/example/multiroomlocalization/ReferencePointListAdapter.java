package com.example.multiroomlocalization;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;

import java.util.ArrayList;

public class ReferencePointListAdapter extends RecyclerView.Adapter<ReferencePointHolder> {

    ArrayList<ReferencePoint> list;
    ArrayList<Speaker> itemsBluetooth;

    Context context;

    public ReferencePointListAdapter(ArrayList<ReferencePoint> arrList,Context context,ArrayList<Speaker> items){
        this.list = arrList;
        this.context = context;
        this.itemsBluetooth = items;
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