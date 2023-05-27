package com.example.multiroomlocalization.socket;

import android.app.Activity;
import android.os.AsyncTask;

import com.example.multiroomlocalization.ScanService;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import android.widget.Toast;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.ScanResult;
import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.User;
import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.messages.localization.MessageReferencePointResult;
import com.example.multiroomlocalization.messages.music.MessageRequestPlaylist;
import com.example.multiroomlocalization.messages.speaker.MessageListSpeaker;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientSocket extends Thread implements Serializable {

    private int port = 10785;//8777;

    private Socket socket;
    private DataInputStream dataIn;
    private static DataOutputStream dataOut;
    private Handler mHandler = new Handler();

    private ScanService scanService;
    private int intervalScan = 10000;
    private String ip ="6.tcp.eu.ngrok.io"; //"10.0.2.2";// "192.168.1.51";
    WifiManager wifiManager;
    Context context;

    public static OutputStream getDataOutputStream() {
        return dataOut;
    }

    @Override
    public void run() {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        try {
            socket = new Socket(ip, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Socket Connesso", Toast.LENGTH_LONG).show();
                }
            });

        }
        catch(Exception e){
            System.out.println("catch");
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }

        scanService = new ScanService(context);

        /*scanService = new ScanService(context);

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


    public void setAddress(String addNgrok,Integer portNgrok){
        ip = addNgrok;
        port = portNgrok;
    }

    public TaskRunner<String> createMessageStartMappingPhase(int len){
        return new TaskRunner<String>(new MessageStartMappingPhase(len));
    }
    public TaskRunner<Void> createByte(byte[] bb){
        return new TaskRunner<Void>(new MessageByte(bb));
    };

    public TaskRunner<Void> createMessageStartScanReferencePoint(){
        return new TaskRunner<Void>(new MessageStartScanReferencePoint());
    }

    public TaskRunner<Void>createMessageNewReferencePoint(int x, int y, ReferencePoint ref){
        return new TaskRunner<Void>(new MessageNewReferencePoint(x,y,ref));
    }

    public TaskRunner<String> createMessageEndMappingPhase(String password){
        return new TaskRunner<String>(new MessageEndMappingPhase(password));
    }

    public TaskRunner<Void> createMessageEndScanReferencePoint(){
        return new TaskRunner<Void>(new MessageEndScanReferencePoint());
    }

    public TaskRunner<Void> createMessageFingerprint(List<ScanResult> fingerprint){
        return new TaskRunner<Void>(new MessageFingerprint(fingerprint));
    }

    public TaskRunner<String> createMessageRegistration(User user){
        return new TaskRunner<String>(new MessageRegistration(user));
    }

    public TaskRunner<String> createMessageLogin(User user){
        return new TaskRunner<String>(new MessageLogin(user));
    }

    public TaskRunner<String> createMessageReqPlaylist(){
        return new TaskRunner<String>(new RequestPlaylist());
    }

    public TaskRunner<Void> createMessageSendListSpeaker(ArraySet<Speaker> listSpeaker){
        return new TaskRunner<Void>(new SendSpeakerList(listSpeaker));
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


    public class MessageStartScanReferencePoint implements Callable<Void> {

        public MessageStartScanReferencePoint(){}
        @Override
        public Void call() throws Exception {
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

    public class MessageStartMappingPhase implements Callable<String>{

        int len;
        String response;

        MessageStartMappingPhase(int len){this.len = len;}
        @Override
        public String call() throws Exception {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase(len);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                response = dataIn.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return response;
        }
    }

    public class MessageByte implements Callable<Void>{
        byte[] bb;

        MessageByte(byte[] bb){this.bb = bb;}
        @Override
        public Void call() throws Exception {
            DataOutputStream bytesOut = new DataOutputStream(new BufferedOutputStream(dataOut));
            try {
                dataOut.write(bb);
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageEndMappingPhase implements Callable<String>{
        private String password;
        private String response;

        public MessageEndMappingPhase(String pass){ this.password = pass;}
        @Override
        public String call() throws Exception {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase message = new com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase(password);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                response = dataIn.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return response;
        }
    }

    public class MessageEndScanReferencePoint implements Callable<Void>{

        public MessageEndScanReferencePoint(){}

        @Override
        public Void call() throws Exception {
            Gson gson = new Gson();
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

    public class MessageNewReferencePoint implements Callable<Void>{

        ReferencePoint referencePoint;
        int x;
        int y;

        public MessageNewReferencePoint(int x,int y,ReferencePoint ref){this.x = x;this.y=y; this.referencePoint = ref; }

        @Override
        public Void call() throws Exception {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint message = new com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint(x,y,referencePoint);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class MessageFingerprint implements Callable<Void>{

        List<ScanResult> fingerprint;

        public MessageFingerprint(List<ScanResult> finger){
            this.fingerprint = finger;
        }

        @Override
        public Void call() throws Exception {
            Gson gson = new Gson();
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

    public class MessageRegistration implements Callable<String>{

        User user;
        String registrationResult;

        public MessageRegistration(User user){
            this.user = user;
        }

        @Override
        public String call() throws Exception {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.connection.MessageRegistration message = new com.example.multiroomlocalization.messages.connection.MessageRegistration(user);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                registrationResult = dataIn.readUTF();
                System.out.println(registrationResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return registrationResult;
        }
    }

    public class MessageLogin implements Callable<String> {

        User user;
        String loginResult;

        public MessageLogin(User user){
            this.user = user;
        }

        @Override
        public String call() throws Exception {
            Gson gson = new Gson();
            try {
                com.example.multiroomlocalization.messages.connection.MessageLogin message = new com.example.multiroomlocalization.messages.connection.MessageLogin(user);
                String json = gson.toJson(message);
                dataOut.writeUTF(json);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                loginResult = dataIn.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return loginResult;
        }
    }


    public class RequestPlaylist implements Callable<String> {

        private final Handler handler = new Handler(Looper.myLooper());
        public RequestPlaylist() { }

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
            Gson gson = new Gson();
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

}
