package com.example.multiroomlocalization;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSLAC extends Thread{

    ServerSocket ss;
    Socket s;
    BufferedReader in;
    BufferedWriter out;
    OutputStream outputStream;
    Context appContext;
    private int currentRoom;
    public ServerSLAC(Context applicationContext) {
        appContext = applicationContext;
        this.start();
    }

    @Override
    public void run() {
        try {

            ss = new ServerSocket(8777);
            Log.i("server","server running");
            s = ss.accept();

            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            outputStream = s.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(outputStream));

            while (!Thread.interrupted()) {

                String message = in.readLine();

                if (message != null) {
                    JSONObject msg = new JSONObject(message);

                }

            }

        } catch(IOException | JSONException e){

            e.printStackTrace();
        }

    }


    @Override
    public void interrupt() {

        try {

            if(s != null)
                s.close();
            ss.close();
            Log.i("server","server not running");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }
}
