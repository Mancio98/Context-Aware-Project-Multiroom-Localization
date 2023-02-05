package com.example.multiroomlocalization.socket;

<<<<<<< HEAD
import android.os.AsyncTask;

import com.example.multiroomlocalization.localization.Fingerprint;
=======
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.multiroomlocalization.Fingerprint;
import com.example.multiroomlocalization.ReferencePoint;
import com.example.multiroomlocalization.ScanService;
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint;
import com.example.multiroomlocalization.messages.localization.MessageReferencePointResult;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
<<<<<<< HEAD
import java.io.ObjectInputStream;
=======
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSocket extends Thread {
<<<<<<< HEAD
    private final int port = 49152;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    private String ip;

    @Override
    public void run() {
=======
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
    int i=0;
    @Override
    public void run() {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
        try {
            socket = new Socket(ip, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e) {
            e.printStackTrace();
        }

<<<<<<< HEAD
<<<<<<< HEAD

        while(true) {
            Fingerprint fingerprint = new Fingerprint();
            // CHIAMARE METODO PER PER LE FINGERPRINT

            MessageFingerprint messageFingerprint = new MessageFingerprint(fingerprint);

            Gson gson = new Gson();
            try {
                dataOut.writeUTF(gson.toJson(messageFingerprint, MessageFingerprint.class));

                String result = dataIn.readUTF();
                System.out.print(result);

                MessageReferencePointResult messageResult = gson.fromJson(result, MessageReferencePointResult.class);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

=======
        scanService = new ScanService(context);
=======
        /*scanService = new ScanService(context);
>>>>>>> origin/luca-branch

        scanService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                        System.out.println("QUI: " + i);
                        i++;
                        new MessageSender(scanSuccess()).execute();
                        mHandler.postDelayed(scanRunnable, intervalScan);
                } else {
                        // scan failure handling
                        //scanFailure();
                        System.out.println("QUA: " + i);
                        i++;
                        new MessageSender(scanFailure()).execute();
                        mHandler.postDelayed(scanRunnable,0);
                }
            }
        });

            mHandler.postDelayed(scanRunnable, 0);*/
            //scanRunnable.run();

    }

    public AsyncTask<Void,Void,Void> createMessageStartMappingPhase(){
        return new MessageStartMappingPhase();
    }

    public AsyncTask<Void,Void,Void> createMessageStartScanReferencePoint(){
        return new MessageStartScanReferencePoint();
    }

    public AsyncTask<Void,Void,Void> createMessageNewReferencePoint(ReferencePoint ref){
        System.out.println("ENTRATO QUA");
        return new MessageNewReferencePoint(ref);
    }

    public AsyncTask<Void,Void,Void> createMessageEndMappingPhase(){
        return new MessageEndMappingPhase();
    }

    public AsyncTask<Void,Void,Void> createMessageEndScanReferencePoint(){
        return new MessageEndScanReferencePoint();
    }
    public void setContext(Context context){
        this.context = context;
    }

    /*public void startScan(){
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }*/

    private MessageFingerprint scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        ArrayList<com.example.multiroomlocalization.ScanResult> scanResult = new ArrayList<>();
        if(results.size()<1){
            return null;
        }
        for (ScanResult res : results ) {
            scanResult.add(new com.example.multiroomlocalization.ScanResult(res.BSSID, res.SSID,res.level));
            //System.out.println("QUI SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }


        Fingerprint fingerprint = new Fingerprint(scanResult);
        // CHIAMARE METODO PER PER LE FINGERPRINT

        MessageFingerprint messageFingerprint = new MessageFingerprint(fingerprint);

        return messageFingerprint;
    }

    private MessageFingerprint scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();

        //INVIO MESSAGGIO
        ArrayList<com.example.multiroomlocalization.ScanResult> scanResult = new ArrayList<>();
        for ( ScanResult res : results ) {
            scanResult.add(new com.example.multiroomlocalization.ScanResult(res.BSSID, res.SSID,res.level));
            Log.println(Log.VERBOSE, "", "ERRORE SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
            //System.out.println("ERRORE SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }

        Fingerprint fingerprint = new Fingerprint(scanResult);
        // CHIAMARE METODO PER PER LE FINGERPRINT

        MessageFingerprint messageFingerprint = new MessageFingerprint(fingerprint);

        return messageFingerprint;
    }



>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
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

<<<<<<< HEAD
        private String message;

        protected MessageSender(Message message) {
            Gson gson = new Gson();
            this.message = gson.toJson(message, MessageFingerprint.class);
=======
        private Message message;

        protected MessageSender(Message message){
            this.message = message;
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
        }

        @Override
        protected Void doInBackground(Void... voids) {
<<<<<<< HEAD

            return null;
        }
    }
}
=======
            if(message!=null) {
                Gson gson = new Gson();
                try {
                    String json = gson.toJson(message);
                    System.out.println(json);
                    dataOut.writeUTF(json);
                    dataOut.flush();

                    String result = dataIn.readUTF();

                    System.out.println(result);

                    MessageReferencePointResult messageResult = gson.fromJson(result, MessageReferencePointResult.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /*
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
        */

        /*
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
         */
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


    public class MessageStartScanReferencePoint extends AsyncTask<Void,Void,Void> {

        //ReferencePoint referencePoint;

        public MessageStartScanReferencePoint(){//ReferencePoint ref){
            //this.referencePoint = ref;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint();//referencePoint);
                String json = gson.toJson(message);//, com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint.class);
                //System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageStartMappingPhase extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase();
                String json = gson.toJson(message);//, com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase.class);
                //System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageEndMappingPhase extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase();
                String json = gson.toJson(message);//, com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase.class);
                //System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageEndScanReferencePoint extends AsyncTask<Void,Void,Void>{

        public MessageEndScanReferencePoint(){}

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint();
                String json = gson.toJson(message);//, com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint.class);
                //System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageNewReferencePoint extends AsyncTask<Void,Void,Void>{

        ReferencePoint referencePoint;

        public MessageNewReferencePoint(ReferencePoint ref){
            this.referencePoint = ref;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint(referencePoint);
                String json = gson.toJson(message);//, com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint.class);
                //System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}

>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
