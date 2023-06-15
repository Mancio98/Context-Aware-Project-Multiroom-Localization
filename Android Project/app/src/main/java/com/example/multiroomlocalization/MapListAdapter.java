package com.example.multiroomlocalization;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.messages.connection.MessageKeepAlive;
import com.example.multiroomlocalization.messages.connection.MessageLogin;
import com.example.multiroomlocalization.messages.localization.MessageImage;
import com.example.multiroomlocalization.messages.localization.MessageMapDetails;
import com.example.multiroomlocalization.messages.localization.MessageMapRequest;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MapListAdapter extends RecyclerView.Adapter<MapListHolder> {

    ArrayList<Map> list;
    Context context;
    ClientSocket client;

    public MapListAdapter(ArrayList<Map> arrList, Context context,ClientSocket client){
        this.list = arrList;
        this.context = context;
        this.client = client;
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
        //TODO far vedere a luca
        //position = holder.getBindingAdapterPosition();
        Map currentData = list.get(position);
        holder.idMap.setText("ID: " + currentData.id);
        holder.mapName.setText(currentData.name);
        if(currentData.isReady != true){
            holder.isReady.setText("Not ready");
            holder.selectMap.setEnabled(false);
            holder.selectMap.setText("NON DISPONIBILE");
        }
        else holder.isReady.setText("Ready");

        holder.selectMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                System.out.println("CLICCATO QUA : " + list.get(position).id);

                                ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                                    @Override
                                    public void onComplete(String result) {
                                        Intent changeActivity;
                                        changeActivity = new Intent(context,ActivityLive.class);
                                        changeActivity.putExtra("ReferencePoint",result);
                                        System.out.println("result");
                                        System.out.println(result);
                                        context.startActivity(changeActivity);
                                    }
                                };

                                Gson gson = new Gson();

                                MessageMapRequest message = new MessageMapRequest(list.get(position).id);
                                String json = gson.toJson(message);
                                client.sendMessageMapRequest(callback,json);

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
