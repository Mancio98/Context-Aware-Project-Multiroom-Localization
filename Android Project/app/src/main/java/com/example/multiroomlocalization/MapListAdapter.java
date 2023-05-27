package com.example.multiroomlocalization;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MapListAdapter extends RecyclerView.Adapter<MapListHolder> {

    ArrayList<Map> list;
    Context context;
    ClickListener listener;

    public MapListAdapter(ArrayList<Map> arrList, Context context, ClickListener listener){
        this.list = arrList;
        this.context = context;
        this.listener = listener;

    }

    @NonNull
    @Override
    public MapListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        View mapRow
                = inflater
                .inflate(R.layout.map_row,
                        parent, false);

        MapListHolder viewHolder
                = new MapListHolder(mapRow);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MapListHolder holder,int position) {
        Map currentData = list.get(position);
        holder.idMap.setText(currentData.id);
        if(currentData.isReady == true){
            holder.isReady.setText("Ready");
        }
        else holder.isReady.setText("Not ready");

        holder.selectMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO CAMBIARE ACTIVITY CON MAPPA SCELTA

                                System.out.println("CLICCATO QUA : " + list.get(position).id);
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
