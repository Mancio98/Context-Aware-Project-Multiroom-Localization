package com.example.multiroomlocalization;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MapListHolder extends RecyclerView.ViewHolder {

    TextView idMap;
    TextView isReady;
    Button selectMap;
    View view;

    public MapListHolder(@NonNull View itemView) {
        super(itemView);
        idMap = itemView.findViewById(R.id.MapId);
        isReady = itemView.findViewById(R.id.isReady);
        selectMap = itemView.findViewById(R.id.buttonSelectMap);
        view = itemView;

    }
}
