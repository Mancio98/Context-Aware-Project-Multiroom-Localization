package com.example.multiroomlocalization.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.example.multiroomlocalization.Fingerprint;
import com.example.multiroomlocalization.ScanService;
import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageReferencePointResult;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSocket extends Thread {
    private final int port = 8777;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Handler mHandler = new Handler();

    private ScanService scanService;
    private int intervalScan = 10000;
    private String ip = "10.0.2.2";
    WifiManager wifiManager;
    Context context;

    @Override
    public void run() {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        try {
            socket = new Socket(ip, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

        }
        catch(IOException e) {
            e.printStackTrace();
        }

        scanService = new ScanService(context);

        scanService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {

                        Log.println(Log.VERBOSE, "", "PROVA");
                        new MessageSender(scanSuccess()).execute();
                        mHandler.postDelayed(scanRunnable, intervalScan);
                } else {
                        // scan failure handling
                        scanFailure();
                }
            }
        });

            mHandler.postDelayed(scanRunnable, 0);

    }

    public void setContext(Context context){
        this.context = context;
    }

    public void startScan(){
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    private MessageFingerprint scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        ArrayList<com.example.multiroomlocalization.ScanResult> scanResult = new ArrayList<>();
        for (ScanResult res : results ) {
            scanResult.add(new com.example.multiroomlocalization.ScanResult(res.BSSID, res.SSID,res.level));
            //System.out.println("QUI SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }


        Fingerprint fingerprint = new Fingerprint(scanResult);
        // CHIAMARE METODO PER PER LE FINGERPRINT

        MessageFingerprint messageFingerprint = new MessageFingerprint(fingerprint);

        return messageFingerprint;
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();

        //INVIO MESSAGGIO

        for ( ScanResult res : results ) {
            Log.println(Log.VERBOSE, "", "ERRORE SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
            //System.out.println("ERRORE SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }
    }



    //This thread inizialize the socket and requires for a connection to the server
    public class Connect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(ip, port);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());

                // DA INSERIRE IL PRIMO SCAMBIO MESSAGGI
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class Disconnect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                dataOut.writeUTF("");
                dataOut.flush();
                socket.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //classe per l'invio dei messaggi JSON all'applicativo python
    public class MessageSender extends AsyncTask<Void,Void,Void> {

        private Message message;

        protected MessageSender(Message message){
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                String json = gson.toJson(message);
                Log.println(Log.VERBOSE,"",json);
                //System.out.println(json);
                dataOut.writeUTF(json);//gson.toJson(messageFingerprint));
                dataOut.flush();
                String result = dataIn.readUTF();

                //System.out.print(result);
                Log.println(Log.VERBOSE,"",result);

                MessageReferencePointResult messageResult = gson.fromJson(result, MessageReferencePointResult.class);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }




    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanService.startScan();

        }
    };

    private void stopScan() {
        mHandler.removeCallbacks(scanRunnable);
    }

}

