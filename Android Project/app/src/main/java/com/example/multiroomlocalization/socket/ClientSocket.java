package com.example.multiroomlocalization.socket;

import android.app.Activity;

import com.example.multiroomlocalization.LoginActivity;
import com.example.multiroomlocalization.ScanService;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.multiroomlocalization.messages.connection.MessageAcknowledge;
import com.example.multiroomlocalization.messages.connection.MessageImageFinish;
import com.example.multiroomlocalization.messages.connection.MessageMappingPhaseFinish;
import com.example.multiroomlocalization.messages.connection.MessageReferencePointFinish;
import com.example.multiroomlocalization.messages.connection.MessageRegistrationSuccessful;
import com.example.multiroomlocalization.messages.connection.MessageRegistrationUnsuccessful;
import com.example.multiroomlocalization.messages.connection.MessageSuccessfulLogin;
import com.example.multiroomlocalization.messages.connection.MessageUnsuccessfulLogin;
import com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.messages.music.MessageRequestPlaylist;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientSocket extends Thread implements Serializable {

    private int port = 18042;

    private Socket socket;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    private Handler mHandler = new Handler();

    private ScanService scanService;
    private int intervalScan = 10000;
    private String ip ="4.tcp.eu.ngrok.io"; //"10.0.2.2";// "192.168.1.51";
    private WifiManager wifiManager;
    private Context context;
    private Gson gson = new Gson();
    private IncomingMsgHandler incomingMsgHandler;
    private Callback<String> reqPlaylistCallback;
    private Callback<String> fingerPrintCallback;
    private Callback<String> startMappingPhaseCallback;
    private Callback<String> loginSuccessfulCallback;
    private Callback<String> loginUnsuccessfulCallback;
    private Callback<String> endMappingPhaseCallback;
    private Callback<String> registrationSuccessfulCallback;
    private Callback<String> registrationUnsuccessfulCallback;
    private Callback<String> imageCallback;
    private Callback<String> endReferencePointCallback;
    private Callback<String> callbackChangeReferencepoint;

    public interface Callback<R> {
        void onComplete(R result);
    }

    public IncomingMsgHandler getIncomingMsgHandler(){
        return incomingMsgHandler;
    }

    private class IncomingMsgHandler extends Thread {
        Handler handler = new Handler(Looper.getMainLooper());
        @Override
        public void run() {
            super.run();

            while(!isInterrupted()){
                try {
                    System.out.println("Attivo");
                    String msg = dataIn.readUTF();

                    msgHandler(msg);
                } catch (IOException e) {
                    interrupt();
                    e.printStackTrace();
                }
            }
        }
        public IncomingMsgHandler(Handler handler) {
            //this.handler = handler;
        }

        @Override
        public void interrupt() {
            super.interrupt();
        }

        private void msgHandler(String msg){

            System.out.println("RICEVUTO MESSAGGIO");
            System.out.println(msg);
            String messageType = gson.fromJson(msg, JsonObject.class).get("type").getAsString();

            if(messageType.equals(MessageChangeReferencePoint.type)){
                Speaker speakerToChange = gson.fromJson(msg, MessageChangeReferencePoint.class)
                        .getReferencePoint().getSpeaker();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                       // MainActivity.getInstance().connectBluetoothDevice(speakerToChange);
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
            else if(messageType.equals(MessageAcknowledge.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { startMappingPhaseCallback.onComplete(msg);
                    }
                });
            }

            else if(messageType.equals(MessageSuccessfulLogin.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { loginSuccessfulCallback.onComplete(msg);
                    }
                });
            }
            else if (messageType.equals(MessageUnsuccessfulLogin.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { loginUnsuccessfulCallback.onComplete(msg);
                    }
                });
            }
            else if (messageType.equals(MessageRegistrationSuccessful.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { registrationSuccessfulCallback.onComplete(msg);
                    }
                });
            }
            else if (messageType.equals(MessageRegistrationUnsuccessful.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { registrationUnsuccessfulCallback.onComplete(msg);
                    }
                });
            }
            else if (messageType.equals((MessageImageFinish.type))){
                handler.post(new Runnable() {
                    @Override
                    public void run() { imageCallback.onComplete(msg);
                    }
                });
            }
            else if(messageType.equals(MessageMappingPhaseFinish.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { endMappingPhaseCallback.onComplete(msg);
                    }
                });
            }
            else if(messageType.equals(MessageReferencePointFinish.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { endReferencePointCallback.onComplete(msg);
                    }
                });
            }
            else if(messageType.equals(MessageChangeReferencePoint.type)){
                handler.post(new Runnable() {
                    @Override
                    public void run() { callbackChangeReferencepoint.onComplete(msg);
                    }
                });
            }

        }
    }
    public ClientSocket(Context context) { this.context = context; }



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
        incomingMsgHandler = new IncomingMsgHandler(LoginActivity.handler);
        incomingMsgHandler.start();

    }

    public void startIncomingMsgHandler(){
        incomingMsgHandler = new IncomingMsgHandler(LoginActivity.handler);
        incomingMsgHandler.start();
    }

    public void setAddress(String addNgrok,Integer portNgrok){
        ip = addNgrok;
        port = portNgrok;
    }


  /*  public TaskRunner<Void> createByte(byte[] bb,Handler handler,Callback<String> callback){
        imageCallback = callback;
        return new TaskRunner<Void>(new MessageByte(bb),handler);
    };
*/
    public void sendImage(byte[] bb,Callback<String> callback){
        imageCallback = callback;
        sendMessage(null,true,bb);
    }

    public void addCallbackChangeReferencePoint(Callback<String> callback){
        callbackChangeReferencepoint = callback;
    }
    public void sendMessageListSpeaker(String message){
        sendMessage(message,false,null);
    }
    public void sendMessageReqPlaylist(Callback<String> callback, String message){
        reqPlaylistCallback = callback;
        sendMessage(message,false,null);
    }

    public void sendMessageStartMappingPhase(Callback<String> callback, String message){
        startMappingPhaseCallback = callback;
        sendMessage(message,false,null);
    }

    public void sendMessageLogin(Callback<String> callback,Callback<String> callback2, String message){
        loginSuccessfulCallback = callback;
        loginUnsuccessfulCallback = callback2;
        sendMessage(message,false,null);
    }

    public void sendMessageEndMappingPhase(Callback<String> callback,String message){
        endMappingPhaseCallback = callback;
        sendMessage(message,false,null);
    }

    public void sendMessageStartScanReferencePoint(String message){sendMessage(message,false,null);}

    public void sendMessageNewReferencePoint(String message){sendMessage(message,false,null);}

    public void sendMessageEndScanReferencePoint(String message,Callback<String>callback){
        endReferencePointCallback = callback;
        sendMessage(message,false,null);
    }

    public void sendMessageFingerprint(String message){ sendMessage(message,false,null);}

    public void sendMessageRegistration(Callback<String> callback,Callback<String> callback2,String message){
        registrationSuccessfulCallback = callback;
        registrationUnsuccessfulCallback = callback2;
        sendMessage(message,false,null);
    }

/*
    public TaskRunner<String> createMessageStartMappingPhase(int len){
        return new TaskRunner<String>(new MessageStartMappingPhaseTemp(len));
    }
*/



/*
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
*/

    public void setContext(Context context){
        this.context = context;
    }


    private void sendMessage(String message,Boolean image,byte[] bb){

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (image) {
                    DataOutputStream bytesOut = new DataOutputStream(new BufferedOutputStream(dataOut));
                    try {
                        dataOut.write(bb);
                        dataOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        dataOut.writeUTF(message);
                        dataOut.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
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


/*
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

    }
*/
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanService.startScan();

        }
    };

    private void stopScan() {
        mHandler.removeCallbacks(scanRunnable);
    }

/*
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

    public class MessageStartMappingPhaseTemp implements Callable<String>{

        int len;
        String response;

        MessageStartMappingPhaseTemp(int len){this.len = len;}
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
*/
   /* public static class MessageByte implements Callable<Void>{
        byte[] bb;

        public MessageByte(byte[] bb){this.bb = bb;}
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
*/
/*
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
*/
/*
    public static class TaskRunner<R> {
        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Callable<R> callable;

        public TaskRunner(Callable<R> callable,Handler handler) {
            this.callable = callable;
            //this.handler = handler;
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
*/

}
