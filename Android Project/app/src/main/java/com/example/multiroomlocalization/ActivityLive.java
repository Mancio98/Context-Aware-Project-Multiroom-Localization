package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.BT_CONNECT_AND_SCAN;
import static com.example.multiroomlocalization.MainActivity.btPermissionCallback;
import static com.example.multiroomlocalization.LoginActivity.btUtility;

import android.Manifest;
import android.app.Activity;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.app.NotificationManager;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothManager;
//import com.example.multiroomlocalization.Bluetooth.ScanBluetooth;
import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.socket.ClientSocket;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.provider.MediaStore;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.localization.MessageMapDetails;
import com.example.multiroomlocalization.messages.music.MessageSettings;
import com.example.multiroomlocalization.speaker.Speaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ActivityLive extends AppCompatActivity implements ServiceConnection {
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
    private Activity activity;
    private ConnectBluetoothManager connectBluetoothThread;
    boolean serviceBound = false;
    private HashMap<String,ArrayList<com.example.multiroomlocalization.ScanResult>> resultScan = new HashMap<>();
    private ArrayList<com.example.multiroomlocalization.ScanResult> scanResultArrayList = new ArrayList<com.example.multiroomlocalization.ScanResult>();
    private final Gson gson = new Gson();
    private ControlAudioService audioServiceManager;
    //private ScanBluetooth scanBluetoothManager;
    private ArrayList<Speaker> listSpeaker;
    private ReferencePointListAdapter adapterReferencePointList;
    private ScanBluetoothService scanBluetoothService;
    private boolean isBound = false;

    private boolean first = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.layout_live_activity);
        clientSocket = LoginActivity.client;

        imageview = findViewById(R.id.mapView);
        FloatingActionButton settings = findViewById(R.id.settingsButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();
            referencePoints = gson.fromJson((String) extras.get("ReferencePoint"),MessageMapDetails.class).getReferencePointArrayList();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CLOSE&#95;ALL");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ActivityLive.this.finish();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

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
                clientSocket.setSenderFingerprint(true);
                btUtility.checkPermission(new MainActivity.BluetoothPermCallback() {
                    @Override
                    public void onGranted() {
                        dialogBuilder = new AlertDialog.Builder(ActivityLive.this);
                        final View popup = getLayoutInflater().inflate(R.layout.referencepointlist_view, null);
                        dialogBuilder.setView(popup);

                        RecyclerView recyclerView = (RecyclerView) popup.findViewById(R.id.recyclerViewReferencePoint);

                        adapterReferencePointList = new ReferencePointListAdapter(referencePoints, getApplicationContext(), scanBluetoothService);

                        recyclerView.setAdapter(adapterReferencePointList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        System.out.println(referencePoints.size());
                        Button buttonConferma = (Button) popup.findViewById(R.id.buttonConfermaSettings);

                        buttonConferma.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapterReferencePointList.closeBluetoothScan();
                                clientSocket.setSenderFingerprint(false);
                                ArrayList<Settings> arrListSettings = new ArrayList<>();

                                for (int i = 0; i < referencePoints.size(); i++) {
                                    arrListSettings.add(new Settings(referencePoints.get(i).getId(), referencePoints.get(i).getSpeaker(), referencePoints.get(i).getDnd()));
                                }

                                Gson gson = new Gson();
                                MessageSettings message = new MessageSettings(arrListSettings, null, null);
                                String json = gson.toJson(message);

                                clientSocket.sendMessageSettings(json, null);
                                dialog.cancel();
                            }
                        });

                        dialog = dialogBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();

                        audioServiceManager.initBluetoothManagerIfNot(scanBluetoothService);
                    }
                });
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
                if(gson.fromJson(result,MessageChangeReferencePoint.class).getReferencePoint() == null){
                    stopScan();
                    dialogBuilder = new AlertDialog.Builder(ActivityLive.this);
                    final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
                    dialogBuilder.setView(popup);
                    TextView text = popup.findViewById(R.id.textPopup);
                    text.setText("Errore : Misurazioni non corrette");

                    Button button = popup.findViewById(R.id.buttonPopup);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog = dialogBuilder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                }
                else {
                    currentRef = gson.fromJson(result, MessageChangeReferencePoint.class).getReferencePoint();
                    System.out.println("NEW REFERENCE POINT");
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                        if (currentRef.getDnd()) {
                            System.out.println("DND TRUE");
                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        } else {
                            System.out.println("DND FALSE");
                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                        }
                    }

                    for (int i = 0; i < referencePoints.size(); i++) {
                        int x = (referencePoints.get(i).getX() * imageViewWidth) / 100;
                        int y = (referencePoints.get(i).getY() * imageViewHeight) / 100;

                        imageview.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                        drawIconRoom(referencePoints.get(i), x, y, referencePoints.get(i).getId().equals(currentRef.getId()));

                    }

                    btUtility.enableBluetooth(null, new BluetoothUtility.OnEnableBluetooth() {
                        @Override
                        public void onEnabled() {
                            audioServiceManager.initBluetoothManagerIfNot(scanBluetoothService);
                            audioServiceManager.connectToSpeaker(currentRef.getSpeaker());
                        }
                    });

                }
            }
        };

        clientSocket.setCallbackChangeReferencePoint(callback);

        audioServiceManager = new ControlAudioService(activity, (View)findViewById(R.id.activity_live_layout));

        audioServiceManager.connectMediaBrowser();
    }

    @Override
    protected void onStart(){
        super.onStart();

        btUtility = new BluetoothUtility(this, activity);

        Intent intent = new Intent(this, ScanBluetoothService.class);

        bindService(intent, this, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //btUtility.enableBluetooth(bl);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 1);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isBound) {
            unbindService(this);
            isBound = false;
        }
        stopScan();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    //listener to get if user granted permission for bluetooth connect and scan (only for sdk > 30)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case BT_CONNECT_AND_SCAN:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(grantResults.length > 1){
                        if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            btPermissionCallback.onGranted();


                            Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btPermissionCallback.onGranted();


                        Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(this, "BT Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 2);
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("CASE2");
                    scanService.registerReceiver(broadcastReceiverScan);
                    mHandler.postDelayed(scanRunnable, 3000);
                }
                break;
        }
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
            System.out.println("first");
            System.out.println(first);
            if(first){
                mHandler.postDelayed(scanRunnable, intervalScan+5000);
                first = false;
            }
            else {
                mHandler.postDelayed(scanRunnable, intervalScan);
            }

        }
    };

    private void stopScan() {
        scanService.registerReceiver(null);
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
            if (clientSocket!=null) clientSocket.sendMessageFingerprint(json);
        }

        private void scanFailure() {
            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
            for ( android.net.wifi.ScanResult res : results ){
                com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID,res.SSID,res.level);
                System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
                listScan.add(scan);
            }
            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan);
            String json = gson.toJson(message);
            if(clientSocket!=null) clientSocket.sendMessageFingerprint(json);
        }


    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();

        audioServiceManager.disconnectMediaBrowser();
        if(connectBluetoothThread != null)
            connectBluetoothThread.disconnectEverything(true);
        connectBluetoothThread = null;
        activity = null;
        clientSocket = null;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
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

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(ActivityLive.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(ActivityLive.this, new String[]{permission}, requestCode);
        } else {
            switch (requestCode) {
                case 1:
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 2);
                    break;
                case 2:
                    System.out.println("CASO 2");
                    scanService.registerReceiver(broadcastReceiverScan);
                    mHandler.postDelayed(scanRunnable, 3000);
                    break;
            }
        }
    }

}
