package com.example.multiroomlocalization;

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

import java.util.ArrayList;

public class ListBluetoothAdapter extends ArrayAdapter<ListBluetoothElement> {


    private final Context myContext;


    public ListBluetoothAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        myContext = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(myContext).inflate(R.layout.list_bt_layout, parent, false);

        ListBluetoothElement device = getItem(position);

        TextView name = (TextView) listItem.findViewById(R.id.name_btdevice);

        name.setText(device.getNameDevice());
        Spinner spinner = (Spinner) listItem.findViewById(R.id.spinner_rooms);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myContext,
                R.array.rooms_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                device.setRoom(adapter.getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                device.setRoom(adapter.getItem(0));
            }
        });
        spinner.setAdapter(adapter);
        device.setSpinner(spinner);

        return listItem;
    }


}
