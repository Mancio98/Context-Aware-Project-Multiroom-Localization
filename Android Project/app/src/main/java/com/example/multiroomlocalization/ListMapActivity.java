package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.LoginActivity.btUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.connection.MessageMapSubscription;
import com.example.multiroomlocalization.messages.connection.MessageSubscriptionSuccessful;
import com.example.multiroomlocalization.messages.connection.MessageUpdateMapList;
import com.example.multiroomlocalization.messages.music.MessageSettings;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.example.multiroomlocalization.speaker.Speaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Base64;

public class ListMapActivity extends AppCompatActivity implements ServiceConnection {

    MapListAdapter adapter;
    ArrayList<Map> mapList;
    ClientSocket clientSocket;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private boolean idEmpty=true;
    private boolean passwordEmpty = true;

    private String idMap;
    ArrayList<String> listIdArray = new ArrayList<>();

    private Activity activity;
    private ArrayList<Speaker> listSpeaker;
    private ReferencePointListAdapter adapterReferencePointList;

    private ScanBluetoothService scanBluetoothService;
    private boolean isBound = false;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map_available);
        clientSocket = LoginActivity.client;

        activity = this;
        RecyclerView recyclerView = findViewById(R.id.recyclerViewMap);
        FloatingActionButton fab = findViewById(R.id.fabAddMap);
        TextView text = findViewById(R.id.textViewNoMap);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mapList = extras.getParcelableArrayList("Map");
            for(int i =0; i<mapList.size();i++){
                listIdArray.add(mapList.get(i).id);

            }
            System.out.println("listIdArray");
            System.out.println(listIdArray);
            if (mapList.size()>0){
                text.setVisibility(View.INVISIBLE);
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CLOSE&#95;ALL");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ListMapActivity.this.finish();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        adapter = new MapListAdapter(mapList,ListMapActivity.this,clientSocket);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(ListMapActivity.this));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialogBuilder = new AlertDialog.Builder(ListMapActivity.this);
                final View popup = getLayoutInflater().inflate(R.layout.popup_add_map, null);
                dialogBuilder.setView(popup);
                EditText inputIdMap = (EditText) popup.findViewById(R.id.editTextIdMapInput);
                EditText inputPasswordMap = (EditText) popup.findViewById(R.id.editTextPasswordMapInput);
                Button buttonConferma = (Button) popup.findViewById(R.id.buttonConfermaIdPassMap);

                buttonConferma.setEnabled(false);


                inputIdMap.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if(charSequence.toString().trim().length()==0){
                            idEmpty =true;
                            buttonConferma.setEnabled(false);
                        } else {
                            idEmpty =false;
                            if(!passwordEmpty){
                                buttonConferma.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

                inputPasswordMap.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if(charSequence.toString().trim().length()==0){
                            passwordEmpty=true;
                            buttonConferma.setEnabled(false);
                        } else {
                            passwordEmpty=false;
                            if(!idEmpty){
                                buttonConferma.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

                buttonConferma.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                            @Override
                            public void onComplete(String result) {
                                idMap = inputIdMap.getText().toString();
                                dialog.cancel();
                                dialogBuilder = new AlertDialog.Builder(ListMapActivity.this);
                                final View popup = getLayoutInflater().inflate(R.layout.referencepointlist_view, null);
                                dialogBuilder.setView(popup);

                                RecyclerView recyclerView = (RecyclerView) popup.findViewById(R.id.recyclerViewReferencePoint);

                                Gson gson = new Gson();
                                ArrayList<ReferencePoint> referencePointArrayList = gson.fromJson(result, MessageSubscriptionSuccessful.class).getReferencePointArrayList();



                                adapterReferencePointList = new ReferencePointListAdapter(referencePointArrayList, getApplicationContext(),scanBluetoothService);

                                recyclerView.setAdapter(adapterReferencePointList);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                                Button buttonConferma = (Button) popup.findViewById(R.id.buttonConfermaSettings);

                                buttonConferma.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        adapterReferencePointList.closeBluetoothScan();
                                        ArrayList<Settings> arrListSettings = new ArrayList<>();

                                        for(int i=0;i<referencePointArrayList.size();i++) {
                                            System.out.println("DND");
                                            System.out.println(referencePointArrayList.get(i).getDnd());
                                            System.out.println("SPEAKER");
                                            System.out.println(referencePointArrayList.get(i).getSpeaker().getName());
                                            arrListSettings.add(new Settings(referencePointArrayList.get(i).getId(),referencePointArrayList.get(i).getSpeaker(),referencePointArrayList.get(i).getDnd() ));
                                        }

                                        dialog.cancel();
                                        dialogBuilder = new AlertDialog.Builder(ListMapActivity.this);
                                        final View popup = getLayoutInflater().inflate(R.layout.popup_input_name_map, null);
                                        dialogBuilder.setView(popup);

                                        EditText mapNameInput = popup.findViewById(R.id.editTextNameMap);
                                        Button button = popup.findViewById(R.id.buttonConfermaMapName);
                                        button.setEnabled(false);
                                        mapNameInput.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                                            @Override
                                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                button.setEnabled(charSequence.toString().trim().length() != 0);
                                            }

                                            @Override
                                            public void afterTextChanged(Editable editable) {}
                                        });

                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                String mapName = mapNameInput.getText().toString();

                                                Gson gson = new Gson();
                                                MessageSettings message = new MessageSettings(arrListSettings,idMap,mapName);
                                                String json = gson.toJson(message);

                                                ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                                                    @Override
                                                    public void onComplete(String result) {
                                                        Gson gson = new Gson();
                                                        Map map = gson.fromJson(result, MessageUpdateMapList.class).getMap();
                                                        mapList.add(map);
                                                        listIdArray.add(map.id);
                                                        text.setVisibility(View.INVISIBLE);
                                                        adapter.notifyItemInserted(mapList.indexOf(map));
                                                        dialog.cancel();
                                                    }
                                                };

                                                clientSocket.sendMessageSettings(json,callback);
                                            }
                                        });

                                        dialog = dialogBuilder.create();
                                        dialog.setCanceledOnTouchOutside(false);
                                        dialog.setCancelable(false);
                                        dialog.show();

                                    }
                                });

                                dialog = dialogBuilder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        };

                        ClientSocket.Callback<String> callbackUnsuccessful = new ClientSocket.Callback<String>() {
                            @Override
                            public void onComplete(String result) {
                                Toast.makeText(ListMapActivity.this, "ERRORE: DATI NON CORRETTI", Toast.LENGTH_LONG).show();
                            }
                        };

                        if(!listIdArray.contains(inputIdMap.getText().toString())){

                            String encoded = Base64.getEncoder().encodeToString(inputPasswordMap.getText().toString().getBytes());
                            System.out.println(encoded);

                            Gson gson = new Gson();
                            MessageMapSubscription message = new MessageMapSubscription(inputIdMap.getText().toString(),encoded);
                            String json = gson.toJson(message);

                            clientSocket.sendMessageMapSubscription(json,callbackSuccessful,callbackUnsuccessful);
                        }
                        else {
                            dialog.cancel();
                            Toast.makeText(ListMapActivity.this, "MAPPA GIÁ PRESENTE IN LISTA",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Button buttonCreateMap = (Button) popup.findViewById(R.id.buttonCreateMap);
                buttonCreateMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        Intent changeActivity;
                        changeActivity = new Intent(ListMapActivity.this,MainActivity.class);
                        changeActivity.putExtra("listMap",mapList);
                        finish();
                        startActivity(changeActivity);
                    }
                });

                dialog = dialogBuilder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        btUtility = new BluetoothUtility(this, activity);
        Intent intent = new Intent(this, ScanBluetoothService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {


        ScanBluetoothService.LocalBinder binder = (ScanBluetoothService.LocalBinder) service;
        scanBluetoothService = binder.getService();
        scanBluetoothService.setContext(getApplicationContext());

        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBound = false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
    }
}
