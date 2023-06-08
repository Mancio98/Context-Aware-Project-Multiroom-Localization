package com.example.multiroomlocalization;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.connection.MessageSuccessfulLogin;
import com.example.multiroomlocalization.messages.connection.MessageUpdateMapList;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageMapDetails;
import com.example.multiroomlocalization.messages.music.MessageSettings;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.example.multiroomlocalization.speaker.Speaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityLive extends AppCompatActivity {
    private ArrayList<ReferencePoint> referencePoints;
    private int imageViewHeight;
    private int imageViewWidth;
    ClientSocket clientSocket;
    private ImageView imageview;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Canvas canvas;
    private ScanService scanService;
    private int intervalScan = 30000;
    private Handler mHandler = new Handler();
    private ReferencePoint currentRef;
    private Bitmap mutableBitmap;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_live_activity);
        clientSocket = LoginActivity.client;

        imageview = findViewById(R.id.mapView);
        FloatingActionButton settings = findViewById(R.id.settingsButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();
            referencePoints = gson.fromJson((String) extras.get("ReferencePoint"),MessageMapDetails.class).getReferencePointArrayList();
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(clientSocket.getBb(), 0, clientSocket.getBb().length);
        System.out.println("BITMAP SIZE");
        System.out.println(bitmap.getWidth());
        System.out.println(bitmap.getHeight());

        imageview.setImageBitmap(bitmap);

        ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageViewHeight = imageview.getHeight();
                imageViewWidth = imageview.getWidth();
                int xGlobal = imageview.getLeft();
                int yGlobal = imageview.getTop();

                System.out.println("Global");
                System.out.println("X: " + xGlobal);
                System.out.println("Y: " + yGlobal);
                System.out.println("Height: " + imageViewHeight + " Width: " + imageViewWidth);
            }
        };

        imageview.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        imageview.post(new Runnable() {
            @Override
            public void run() {
                imageViewHeight = imageview.getHeight();
                imageViewWidth = imageview.getWidth();

                Bitmap bitmapTemp = Bitmap.createScaledBitmap(((BitmapDrawable) imageview.getDrawable()).getBitmap(), imageViewWidth, imageViewHeight, true);
                mutableBitmap = bitmapTemp.copy(Bitmap.Config.ARGB_8888, true);
                canvas = new Canvas(mutableBitmap);

                for(int i=0; i<referencePoints.size(); i++){
                    int x = (referencePoints.get(i).getX()*imageViewWidth)/100;
                    int y = (referencePoints.get(i).getY()*imageViewHeight)/100;

                    drawIconRoom(referencePoints.get(i),x,y,false);
                }
                imageview.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
            }
        });



        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialogBuilder = new AlertDialog.Builder(ActivityLive.this);
                final View popup = getLayoutInflater().inflate(R.layout.referencepointlist_view, null);
                dialogBuilder.setView(popup);

                RecyclerView recyclerView = (RecyclerView) popup.findViewById(R.id.recyclerViewReferencePoint);

                //TODO TEMPSPEAKER DOVREBBERO ESSERE GLI SPEAKER ASSOCIATI AL TELEFONO
                //DENTRO L'ADAPTER NE AGGIUNGO SEMPRE UNO IN TESTA CIOÉ " NO MUSIC" EQUIVALENTE AD UNO SPEAKER CON TUTTO NULL PER INDICARE CHE NON VOGLIAMO LA MUSICA
                ArrayList<Speaker> tempSpeaker = new ArrayList<>();
                tempSpeaker.add(new Speaker("","","Prova1"));
                tempSpeaker.add(new Speaker("","","Prova2"));

                ReferencePointListAdapter adapter = new ReferencePointListAdapter(referencePoints,getApplicationContext(),tempSpeaker);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager( new LinearLayoutManager(getApplicationContext()));

                System.out.println(referencePoints.size());
                Button buttonConferma = (Button) popup.findViewById(R.id.buttonConfermaSettings);

                buttonConferma.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Settings> arrListSettings = new ArrayList<>();

                        for (int i = 0; i < referencePoints.size(); i++) {
                            arrListSettings.add(new Settings(referencePoints.get(i).getId(), referencePoints.get(i).getSpeaker(), referencePoints.get(i).getDnd()));
                        }

                        Gson gson = new Gson();
                        MessageSettings message = new MessageSettings(arrListSettings,null,null);
                        String json = gson.toJson(message);

                        clientSocket.sendMessageSettings(json,null);
                        dialog.cancel();
                    }
                });

                dialog = dialogBuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        scanService = new ScanService(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!scanService.getWifiManager().isScanThrottleEnabled()) {
                intervalScan = 5000;
                System.out.println("IntervalScan: " + intervalScan);
            } else {
                intervalScan = 30000;
                System.out.println("IntervalScan: " + intervalScan);
            }
        }

        ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
            @Override
            public void onComplete(String result) {
                Gson gson = new Gson();
                currentRef =  gson.fromJson(result, MessageChangeReferencePoint.class).getReferencePoint();

                for (int i=0;i<referencePoints.size();i++){
                    int x = (referencePoints.get(i).getX()*imageViewWidth)/100;
                    int y = (referencePoints.get(i).getY()*imageViewHeight)/100;

                    imageview.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                    drawIconRoom(referencePoints.get(i),x,y,referencePoints.get(i).getId().equals(currentRef.getId()));
                }
            }
        };

        clientSocket.setCallbackChangeReferencePoint(callback);
        scanService.registerReceiver(broadcastReceiverScan);
        mHandler.postDelayed(scanRunnable, 3000);

    }

    private @NonNull RectF getTextBackgroundSize(float x, float y, @NonNull String text, @NonNull TextPaint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float halfTextLength = paint.measureText(text) / 2 + 30;
        return new RectF((int) (x - halfTextLength), (int) (y + fontMetrics.top + 5), (int) (x + halfTextLength), (int) (y + fontMetrics.bottom + 5));
    }

    private void drawIconRoom(ReferencePoint referencePoint,int x, int y,boolean selected){
        TextPaint paint = new TextPaint();
        paint.setColor(Color.BLACK);

        paint.setTextSize(70);
        float halfTextLength = paint.measureText(referencePoint.getId()) / 2;
        RectF background = getTextBackgroundSize(x, y, referencePoint.getId(), paint);
        Paint bkgPaint = new Paint();
        bkgPaint.setColor(Color.WHITE);
        if(selected){
            paint.setColor(Color.BLACK);
            bkgPaint.setColor(Color.GREEN);
        }
        canvas.drawRoundRect(background,100,100, bkgPaint);
        bkgPaint.setStrokeWidth(10);
        bkgPaint.setColor(Color.BLACK);
        bkgPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(background,100,100,bkgPaint);

        canvas.drawText(referencePoint.getId(), x - halfTextLength, y, paint);

    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {

            scanService.startScan();
            mHandler.postDelayed(scanRunnable, intervalScan);
        }
    };

    private void stopScan() {
        mHandler.removeCallbacks(scanRunnable);
    }
    private BroadcastReceiver broadcastReceiverScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                // scan failure handling
                scanFailure();
            }
        }

        private void scanSuccess() {
            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
            for (android.net.wifi.ScanResult res : results) {
                com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID, res.SSID, res.level);
                System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID + " level: " + res.level);
                listScan.add(scan);
            }

            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan);
            String json = gson.toJson(message);
            clientSocket.sendMessageFingerprint(json);
        }

        private void scanFailure() {
            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
            for ( android.net.wifi.ScanResult res : results ) {
                com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID,res.SSID,res.level);
                System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
                listScan.add(scan);
            }

            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan);
            String json = gson.toJson(message);
            clientSocket.sendMessageFingerprint(json);
        }


    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }
}
