package com.example.multiroomlocalization.socket;

import android.os.AsyncTask;

import com.example.multiroomlocalization.MainActivity;
import com.example.multiroomlocalization.ScanService;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.ScanResult;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.User;
import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.messages.connection.MessageConnectionClose;
import com.example.multiroomlocalization.messages.localization.MessageReferencePointResult;
import com.example.multiroomlocalization.messages.music.MessageRequestPlaylist;
import com.example.multiroomlocalization.messages.speaker.MessageListSpeaker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientSocket extends Thread {

    private final int port = 15124;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Handler mHandler = new Handler();

    private ScanService scanService;
    private int intervalScan = 10000;
    private String ip ="4.tcp.eu.ngrok.io";// "10.0.2.2";
    private WifiManager wifiManager;
    private Context context;
    private Gson gson = new Gson();
    private IncomingMsgHandler incomingMsgHandler;
    private Callback<String> reqPlaylistCallback;
    private Callback<String> fingerPrintCallback;

    public interface Callback<R> {
        void onComplete(R result);
    }
    private class IncomingMsgHandler extends Thread {

        Handler handler = new Handler(Looper.getMainLooper());
        @Override
        public void run() {
            super.run();

            while(isAlive()){
                try {
                    System.out.println("sono attivo diocane");
                    String msg = dataIn.readUTF();

                    msgHandler(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
        }

        private void msgHandler(String msg){

            System.out.println(msg);
            String messageType = gson.fromJson(msg, JsonObject.class).get("type").getAsString();

            if(messageType.equals(MessageChangeReferencePoint.type)){
                Speaker speakerToChange = gson.fromJson(msg, MessageChangeReferencePoint.class)
                        .getReferencePoint().getSpeaker();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.getInstance().connectBluetoothDevice(speakerToChange);
                    }
                });

            }
            else if(messageType.equals(MessageRequestPlaylist.type)){

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        reqPlaylistCallback.onComplete(msg);
                    }
                });

            }
        }
    }
    public ClientSocket(Context context) { this.context = context; }

    @Override
    public void run() {
        super.run();
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
        incomingMsgHandler = new IncomingMsgHandler();
        incomingMsgHandler.start();
       //
        /*

        scanService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                        System.out.println("QUI: " + i);
                        i++;
                        //new MessageSender(scanSuccess()).execute();
                        mHandler.postDelayed(scanRunnable, intervalScan);
                } else {
                        // scan failure handling
                        //scanFailure();
                        System.out.println("QUA: " + i);
                        i++;
                       // new MessageSender(scanFailure()).execute();
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
        return new MessageNewReferencePoint(ref);
    }

    public AsyncTask<Void,Void,Void> createMessageEndMappingPhase(){
        return new MessageEndMappingPhase();
    }

    public AsyncTask<Void,Void,Void> createMessageEndScanReferencePoint(){
        return new MessageEndScanReferencePoint();
    }

    public AsyncTask<Void,Void,Void> createMessageFingerprint(List<ScanResult> fingerprint){
        return new MessageFingerprint(fingerprint);
    }

    public AsyncTask<Void,Void,Void> createMessageRegistration(User user){
        return new MessageRegistration(user);
    }

    public AsyncTask<Void,Void,Void> createMessageLogin(User user){
        return new MessageLogin(user);
    }

    public TaskRunner<String> createMessageReqPlaylist(Callback<String> callback){
        reqPlaylistCallback = callback;
        return new TaskRunner<String>(new RequestPlaylist());
    }

    public TaskRunner<Void> createMessageSendListSpeaker(ArraySet<Speaker> listSpeaker){
        return new TaskRunner<Void>(new SendSpeakerList(listSpeaker));
    }

    public TaskRunner<String> createMessageSendFingerprint(List<ScanResult> list){
        return new TaskRunner<String>(new SendFingerprint(list));
    }

    public void sendMessageListSpeaker(String message){
        sendMessage(message);
    }
    public void sendMessageFingerPrint(String message){
        sendMessage(message);

    }
    public void sendMessageReqPlaylist(Callback<String> callback, String message){

        reqPlaylistCallback = callback;
        sendMessage(message);
    }

    private void sendMessage(String message){

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dataOut.writeUTF(message);
                    dataOut.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    public void setContext(Context context){
        this.context = context;
    }

    /*
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
    }*/


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

            if(message!=null) {
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

        public MessageStartScanReferencePoint(){
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint();//referencePoint);
                String json = gson.toJson(message);
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

            try {
                com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase();
                String json = gson.toJson(message);
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

            try {
                com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase();
                String json = gson.toJson(message);
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

            try {
                com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint();
                String json = gson.toJson(message);
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

            try {
                com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint(referencePoint);
                String json = gson.toJson(message);
                System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageFingerprint extends AsyncTask<Void,Void,Void>{

        List<ScanResult> fingerprint;

        public MessageFingerprint(List<ScanResult> finger){
            this.fingerprint = finger;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                com.example.multiroomlocalization.messages.localization.MessageFingerprint message = new com.example.multiroomlocalization.messages.localization.MessageFingerprint(fingerprint);
                String json = gson.toJson(message);

                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class SendFingerprint implements Callable<String> {

        List<ScanResult> fingerPrint;

        public SendFingerprint(List<ScanResult> fingerprint) {
            this.fingerPrint = fingerprint;
        }

        @Override
        public String call() {

            try {
                com.example.multiroomlocalization.messages.localization.MessageFingerprint message = new com.example.multiroomlocalization.messages.localization.MessageFingerprint(fingerPrint);
                String json = gson.toJson(message);
                System.out.println(json);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }


            String jsonResp;
            try {
                 jsonResp = dataIn.readUTF();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return jsonResp;
        }


    }

    public class MessageRegistration extends AsyncTask<Void,Void,Void>{

        User user;

        public MessageRegistration(User user){
            this.user = user;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                com.example.multiroomlocalization.messages.connection.MessageRegistration message = new com.example.multiroomlocalization.messages.connection.MessageRegistration(user);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageLogin extends AsyncTask<Void,Void,Void>{

        User user;

        public MessageLogin(User user){
            this.user = user;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                com.example.multiroomlocalization.messages.connection.MessageLogin message = new com.example.multiroomlocalization.messages.connection.MessageLogin(user);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public class RequestPlaylist implements Callable<String> {

        private final Handler handler = new Handler(Looper.myLooper());
        public RequestPlaylist() {

        }

        @Override
        public String call() {
            Gson gson = new Gson();
            try {
                MessageRequestPlaylist message = new MessageRequestPlaylist();
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            String jsonPlaylist;

            try {
                jsonPlaylist = dataIn.readUTF();
                System.out.println(jsonPlaylist);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return jsonPlaylist;
        }

    }

    public class SendSpeakerList implements Callable<Void> {

        private final ArraySet<Speaker> listSpeaker;

        public SendSpeakerList(ArraySet<Speaker> list) {
            this.listSpeaker = list;
        }

        @Override
        public Void call() {

            try {
                MessageListSpeaker message = new MessageListSpeaker(this.listSpeaker);
                String json = gson.toJson(message);
                System.out.println("json: "+json);
                dataOut.writeUTF(json);
                dataOut.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


    }

    public static class TaskRunner<R> {
        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Callable<R> callable;

        public TaskRunner(Callable<R> callable) {

            this.callable = callable;
        }

        public interface Callback<R> {
            void onComplete(R result);
        }

        public void executeAsync(Callback<R> callback) {
            executor.execute(() -> {
                final R result;
                try {
                    result = this.callable.call();
                    if( callback != null)
                        handler.post(() -> {
                            callback.onComplete(result);
                        });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

    public void closeConnection() {
        Executor executor = Executors.newSingleThreadExecutor();
        MessageConnectionClose msg = new MessageConnectionClose();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = gson.toJson(msg);
                    dataOut.writeUTF(json);
                    dataOut.flush();
                    socket.close();
                    dataIn.close();
                    dataOut.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        incomingMsgHandler.interrupt();

    }
}
