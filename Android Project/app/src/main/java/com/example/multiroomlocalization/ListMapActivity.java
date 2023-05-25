package com.example.multiroomlocalization;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListMapActivity extends AppCompatActivity {

    MapListAdapter adapter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map_available);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMap);

        ArrayList<Map> list = new ArrayList<>();

        list.add(new Map("00000", "YES"));
        list.add(new Map("00001", "YES"));
        list.add(new Map("00002", "NO"));
        list.add(new Map("00003", "NO"));
        list.add(new Map("00004", "YES"));

        adapter = new MapListAdapter(list,getApplicationContext(),null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(ListMapActivity.this));


    }



}
