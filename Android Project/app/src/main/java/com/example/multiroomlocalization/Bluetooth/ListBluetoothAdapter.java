package com.example.multiroomlocalization.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.ListRoomsElement;
import com.example.multiroomlocalization.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListBluetoothAdapter extends ArrayAdapter<ListRoomsElement> {


    private final Context myContext;
    private final ArrayList<BluetoothDevice> arrayDevices;
    private final Activity myActivity;

    private final ArrayList<ArrayAdapter<String>> spinnerAdapters = new ArrayList<>();
    private final Set<BluetoothDevice> pairedDevices;


    public ListBluetoothAdapter(@NonNull Context context, int resource, @NonNull List<ListRoomsElement> objects, Activity myActivity, Set<BluetoothDevice> pairedDevices) {
        super(context, resource, objects);
        this.myContext = context;
        this.arrayDevices = new ArrayList<>();
        arrayDevices.addAll(pairedDevices);
        this.myActivity = myActivity;
        this.pairedDevices = pairedDevices;

    }


    protected void addBluetoothDevice(BluetoothDevice device){

        arrayDevices.add(device);

        BluetoothUtility.checkPermission(myActivity);
        spinnerAdapters.forEach(arrayAdapter ->{
            arrayAdapter.add(device.getName());
            arrayAdapter.notifyDataSetChanged();
        } );

    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(myContext).inflate(R.layout.list_rooms_bt_layout, parent, false);

        ListRoomsElement room = getItem(position);

        TextView name = (TextView) listItem.findViewById(R.id.name_room);

        name.setText(room.getName());

        Spinner spinner = (Spinner) listItem.findViewById(R.id.spinner_btdevices);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext, android.R.layout.simple_spinner_item);
        adapter.add("NONE");

        BluetoothUtility.checkPermission(myActivity);
        for(BluetoothDevice device : pairedDevices) {
            adapter.add(device.getName());
        }

        spinnerAdapters.add(adapter);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Apply the adapter to the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i > 0)
                    room.setDevice(arrayDevices.get(i-1));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                room.setDevice(null);
            }
        });
        spinner.setAdapter(adapter);

        return listItem;
    }


}
