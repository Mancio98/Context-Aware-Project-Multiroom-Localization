package com.example.multiroomlocalization.socket;

import android.os.AsyncTask;

import com.example.multiroomlocalization.localization.Fingerprint;
import com.example.multiroomlocalization.messages.Message;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageReferencePointResult;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSocket extends Thread {
    private final int port = 49152;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    private String ip;

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e) {
            e.printStackTrace();
        }


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

        private String message;

        protected MessageSender(Message message) {
            Gson gson = new Gson();
            this.message = gson.toJson(message, MessageFingerprint.class);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }
}