package com.example.multiroomlocalization;

import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReferencePointHolder extends RecyclerView.ViewHolder {

    TextView referencePointName;
    Switch dndSwitch;
    Spinner spinnerBluetooth;
    View view;

    public ReferencePointHolder(@NonNull View itemView) {
        super(itemView);
        referencePointName = itemView.findViewById(R.id.roomId);
        dndSwitch = itemView.findViewById(R.id.switchDnd);
        spinnerBluetooth = itemView.findViewById(R.id.spinnerBluetooth);
        view = itemView;

    }
}
