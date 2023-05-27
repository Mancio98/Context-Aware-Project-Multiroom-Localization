package com.example.multiroomlocalization;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListMapActivity extends AppCompatActivity {

    MapListAdapter adapter;
    ArrayList<Map> mapList;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map_available);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMap);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mapList = extras.getParcelableArrayList("Map");

        }

        ArrayList<Map> list = new ArrayList<>();

        list.add(new Map("00000", true));
        list.add(new Map("00001", true));
        list.add(new Map("00002", false));
        list.add(new Map("00003", false));
        list.add(new Map("00004", true));

        adapter = new MapListAdapter(mapList,getApplicationContext(),null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(ListMapActivity.this));


    }



}
