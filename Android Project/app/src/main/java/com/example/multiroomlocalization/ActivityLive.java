package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.BT_CONNECT_AND_SCAN;
import static com.example.multiroomlocalization.LoginActivity.btUtility;
import static com.example.multiroomlocalization.MainActivity.btPermissionCallback;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothManager;
import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.Music.ControlAudioService;
import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.localization.ScanResult;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageMapDetails;
import com.example.multiroomlocalization.messages.music.MessageSettings;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final Handler mHandler = new Handler();
    private ReferencePoint currentRef;
    private Bitmap mutableBitmap;
    private Activity activity;
    private ConnectBluetoothManager connectBluetoothThread;
    boolean serviceBound = false;
    private ControlAudioService audioServiceManager;

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

        imageview.setImageBitmap(bitmap);

        ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageViewHeight = imageview.getHeight();
                imageViewWidth = imageview.getWidth();
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
            } else {
                intervalScan = 30000;
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
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                        if (currentRef.getDnd()) {
                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        } else {
                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                        }
                    }

                    for (int i = 0; i < referencePoints.size(); i++) {
                        int x = (referencePoints.get(i).getX() * imageViewWidth) / 100;
                        int y = (referencePoints.get(i).getY() * imageViewHeight) / 100;

                        imageview.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                        drawIconRoom(referencePoints.get(i), x, y, referencePoints.get(i).getId().equals(currentRef.getId()));

                    }

                    btUtility.enableBluetooth(new BluetoothUtility.OnEnableBluetooth() {
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case BT_CONNECT_AND_SCAN:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(grantResults.length > 1){
                        if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            btPermissionCallback.onGranted();

                        }
                    } else {
                        btPermissionCallback.onGranted();
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

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanService.startScan();
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

    /* DEMO
    private ArrayList<ScanResult> demoScanCamera = new ArrayList<ScanResult>() {
        {
            add(new ScanResult("2c:91:ab:46:de:ed", "FRITZ!Box 7590 PW", -78.0));
            add(new ScanResult("2c:91:ab:46:de:ed","FRITZ!Box 7590 PW", -78.));
            add(new ScanResult("2c:91:ab:46:de:ee", "FRITZ!Box 7590 PW", -64.));
            add(new ScanResult("3c:37:12:d1:36:2a", "FRITZ!Box 7590 PW", -37.0));
            add(new ScanResult("3c:37:12:d1:36:2b", "FRITZ!Box 7590 PW", -44.0));
            add(new ScanResult("dc:15:c8:3d:d6:00", "FRITZ!Box 7590 PW", -76.0));
            add(new ScanResult("50:d4:f7:a4:47:61", "SKYWIFI_VGE3R", -85.0));
            add(new ScanResult("a4:cf:12:9c:9f:ed", "Gateway-9C9FEC", -84.0));
            add(new ScanResult("60:32:b1:12:a3:51", "BAGNI_MAURIZIO_5.0", -90.0));
            add(new ScanResult("4c:6e:6e:4b:75:c7", "BM-Privato", -91.0));
            add(new ScanResult("d8:0d:17:b7:38:22", "SKYWIFI_VGE3R", -80.0));
            add(new ScanResult("d8:0d:17:b7:38:21", "SKYWIFI_VGE3R", -77.0));
            add(new ScanResult("1c:61:b4:d9:4f:e3", "", -82.0));
            add(new ScanResult("28:80:88:53:b4:33", "Infostrada-5CBA98_2GEXT", -90.0));
            add(new ScanResult("e8:65:d4:1d:cc:19", "Gabriele", -91.0));
            add(new ScanResult("64:59:f8:e5:db:88", "Vodafone-34352184", -88.0));
            add(new ScanResult("1e:61:b4:a9:4f:e3", "Home's Lovers", -82.0));
            add(new ScanResult("1e:61:b4:a9:4f:e2", "Home's Lovers", -73.0));
            add(new ScanResult("5c:96:9d:6a:19:ea", "Red Wi-Fi de Ester", -74.0));
            add(new ScanResult("1c:61:b4:d9:4d:56", "", -88.0));
            add(new ScanResult("5c:96:9d:6a:19:e9", "Red Wi-Fi de Ester", -68.0));
            add(new ScanResult("9a:80:bb:98:05:6e", "4G-CPE_7300", -68.0));
            add(new ScanResult("56:64:b8:8b:02:96", "TIM-39593198", -92.0));
            add(new ScanResult("b2:22:7a:5b:9e:90", "DIRECT-90-HP Laser 107w", -90.0));
            add(new ScanResult("28:3b:82:5c:ba:99", "Infostrada-5CBA98", -84.0));
            add(new ScanResult("b0:0c:d1:05:97:04", "DIRECT-02-HP OfficeJet 6950", -73.0));
            add(new ScanResult("1e:61:b4:a9:4d:56", "Home's Lovers", -84.0));
            add(new ScanResult("28:3b:82:5c:ba:9b", "Infostrada-5CBA98", -85.0));
            add(new ScanResult("c0:a3:6e:c3:e2:54", "SKYWIFI_VGE3R", -63.0));
            add(new ScanResult("c0:a3:6e:c3:e2:55", "SKYWIFI_VGE3R", -62.0));
        }



    };

    private ArrayList<ScanResult> demoScanSoggiorno = new ArrayList<ScanResult>() {
        {
            add(new ScanResult("1e:61:b4:a9:4c:9f", "Home's Lovers", -88.0));
            add(new ScanResult("1e:61:b4:a9:4c:9e", "Home's Lovers", -85.0));
            add(new ScanResult("2c:91:ab:46:de:ed", "FRITZ!Box 7590 PW", -41.0));
            add(new ScanResult("2c:91:ab:46:de:ee", "FRITZ!Box 7590 PW", -47.0));
            add(new ScanResult("3c:37:12:d1:36:2a", "FRITZ!Box 7590 PW", -60.0));
            add(new ScanResult("3c:37:12:d1:36:2b", "FRITZ!Box 7590 PW", -70.0));
            add(new ScanResult("dc:15:c8:3d:d6:00", "FRITZ!Box 7590 PW", -66.0));
            add(new ScanResult("50:d4:f7:a4:47:62", "SKYWIFI_VGE3R", -83.0));
            add(new ScanResult("50:d4:f7:a4:47:61", "SKYWIFI_VGE3R", -79.0));
            add(new ScanResult("98:3b:67:b1:20:34", "WOW FI - FASTWEB", -75.0));
            add(new ScanResult("98:3b:67:b1:20:33", "FASTWEB-NMCGJK", -76.0));
            add(new ScanResult("98:3b:67:b1:20:37", "FASTWEB-NMCGJK", -85.0));
            add(new ScanResult("92:3b:67:b1:20:37", "", -85.0));
            add(new ScanResult("a4:cf:12:9c:9f:ed", "Gateway-9C9FEC", -74.0));
            add(new ScanResult("d8:0d:17:b7:38:22", "SKYWIFI_VGE3R", -65.0));
            add(new ScanResult("d8:0d:17:b7:38:21", "SKYWIFI_VGE3R", -63.0));
            add(new ScanResult("28:80:88:53:b4:33", "Infostrada-5CBA98_2GEXT", -84.0));
            add(new ScanResult("28:80:88:53:b4:34", "Infostrada-5CBA98_5GEXT", -85.0));
            add(new ScanResult("30:42:40:fd:66:82", "Wind3 HUB - FD6682", -88.0));
            add(new ScanResult("1c:61:b4:d9:4c:9f", "", -89.0));
            add(new ScanResult("64:59:f8:e5:db:88", "Vodafone-34352184", -80.0));
            add(new ScanResult("1e:61:b4:a9:4f:e3", "Home's Lovers", -87.0));
            add(new ScanResult("1e:61:b4:a9:4f:e2", "Home's Lovers", -80.0));
            add(new ScanResult("5c:96:9d:6a:19:ea", "Red Wi-Fi de Ester", -77.0));
            add(new ScanResult("1c:61:b4:d9:4d:56", "", -78.0));
            add(new ScanResult("1c:61:b4:d9:4d:57", "", -86.0));
            add(new ScanResult("98:00:6a:c4:fe:e9", "FASTWEB-D5UYRY", -89.0));
            add(new ScanResult("5c:96:9d:6a:19:e9", "Red Wi-Fi de Ester", -77.0));
            add(new ScanResult("9a:80:bb:98:05:6e", "4G-CPE_7300", -86.0));
            add(new ScanResult("28:3b:82:5c:ba:99", "Infostrada-5CBA98", -69.0));
            add(new ScanResult("b0:0c:d1:05:97:04", "DIRECT-02-HP OfficeJet 6950", -65.0));
            add(new ScanResult("1e:61:b4:a9:4d:56", "Home's Lovers", -76.0));
            add(new ScanResult("1e:61:b4:a9:4d:57", "Home's Lovers", -84.0));
            add(new ScanResult("28:3b:82:5c:ba:9b", "Infostrada-5CBA98", -64.0));
            add(new ScanResult("c0:a3:6e:c3:e2:54", "SKYWIFI_VGE3R", -67.0));
            add(new ScanResult("c0:a3:6e:c3:e2:55", "SKYWIFI_VGE3R", -66.0));
        }
    };
    
    private ArrayList<ScanResult> demoScanBagno = new ArrayList<ScanResult>(){{

        add(new ScanResult("1e:61:b4:a9:4c:9e", "Home's Lovers", -91.0));
        add(new ScanResult("2c:91:ab:46:de:ed", "FRITZ!Box 7590 PW", -61.0));
        add(new ScanResult("2c:91:ab:46:de:ee", "FRITZ!Box 7590 PW", -64.0));
        add(new ScanResult("3c:37:12:d1:36:2a", "FRITZ!Box 7590 PW", -65.0));
        add(new ScanResult("3c:37:12:d1:36:2b", "FRITZ!Box 7590 PW", -75.0));
        add(new ScanResult("dc:15:c8:3d:d6:00", "FRITZ!Box 7590 PW", -51.0));
        add(new ScanResult("50:d4:f7:a4:47:62", "SKYWIFI_VGE3R", -75.0));
        add(new ScanResult("50:d4:f7:a4:47:61", "SKYWIFI_VGE3R", -75.0));
        add(new ScanResult("98:3b:67:b1:20:34", "WOW FI - FASTWEB", -82.0));
        add(new ScanResult("98:3b:67:b1:20:33", "FASTWEB-NMCGJK", -81.0));
        add(new ScanResult("98:3b:67:b1:20:37", "FASTWEB-NMCGJK", -85.0));
        add(new ScanResult("92:3b:67:b1:20:37", "", -88.0));
        add(new ScanResult("60:45:cb:18:72:d8", "SuperGigetto", -91.0));
        add(new ScanResult("a4:cf:12:9c:9f:ed", "Gateway-9C9FEC", -81.0));
        add(new ScanResult("d8:0d:17:b7:38:22", "SKYWIFI_VGE3R", -77.0));
        add(new ScanResult("d8:0d:17:b7:38:21", "SKYWIFI_VGE3R", -74.0));
        add(new ScanResult("1c:61:b4:d9:4f:e2", "", -81.0));
        add(new ScanResult("28:80:88:53:b4:33", "Infostrada-5CBA98_2GEXT", -69.0));
        add(new ScanResult("e8:81:75:03:a8:80", "Wind3 HUB - 03A880", -83.0));
        add(new ScanResult("28:80:88:53:b4:34", "Infostrada-5CBA98_5GEXT", -86.0));
        add(new ScanResult("30:42:40:fd:66:82", "Wind3 HUB - FD6682", -86.0));
        add(new ScanResult("1c:61:b4:d9:4c:9e", "", -86.0));
        add(new ScanResult("64:59:f8:e5:db:88", "Vodafone-34352184", -93.0));
        add(new ScanResult("1e:61:b4:a9:4f:e3", "Home's Lovers", -88.0));
        add(new ScanResult("1e:61:b4:a9:4f:e2", "Home's Lovers", -83.0));
        add(new ScanResult("5c:96:9d:6a:19:ea", "Red Wi-Fi de Ester", -91.0));
        add(new ScanResult("1c:61:b4:d9:4d:56", "", -71.0));
        add(new ScanResult("1c:61:b4:d9:4d:57", "", -87.0));
        add(new ScanResult("5c:96:9d:6a:19:e9", "Red Wi-Fi de Ester", -91.0));
        add(new ScanResult("9a:80:bb:98:05:6e", "4G-CPE_7300", -90.0));
        add(new ScanResult("56:64:b8:8b:02:96", "TIM-39593198", -89.0));
        add(new ScanResult("32:6c:6f:61:6c:3d", "Redmi Note 10 5G", -93.0));
        add(new ScanResult("b2:22:7a:5b:9e:90", "DIRECT-90-HP Laser 107w", -87.0));
        add(new ScanResult("28:3b:82:5c:ba:99", "Infostrada-5CBA98", -91.0));
        add(new ScanResult("b0:0c:d1:05:97:04", "DIRECT-02-HP OfficeJet 6950", -88.0));
        add(new ScanResult("1e:61:b4:a9:4d:56", "Home's Lovers", -88.0));
        add(new ScanResult("1e:61:b4:a9:4d:57", "Home's Lovers", -86.0));
        add(new ScanResult("90:fd:73:ff:1b:8f", "Wind3 HUB - FF1B8F", -92.0));
        add(new ScanResult("28:3b:82:5c:ba:9b", "Infostrada-5CBA98", -71.0));
        add(new ScanResult("c0:a3:6e:c3:e2:54", "SKYWIFI_VGE3R", -74.0));
        add(new ScanResult("c0:a3:6e:c3:e2:55", "SKYWIFI_VGE3R", -89.0));
    }};

    private int demoCounter = 0; */
    private final BroadcastReceiver broadcastReceiverScan = new BroadcastReceiver() {
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
            List<ScanResult> listScan = new ArrayList<>();
            for (android.net.wifi.ScanResult res : results) {
                ScanResult scan = new ScanResult(res.BSSID, res.SSID, res.level);

                listScan.add(scan);
            }
            Gson gson = new Gson();

            MessageFingerprint message = new MessageFingerprint(listScan, System.currentTimeMillis());
            String json = gson.toJson(message);
            if (clientSocket!=null) clientSocket.sendMessageFingerprint(json);
            /* DEMO
            String json;
            if(demoCounter%30 < 10) {
                MessageFingerprint message = new MessageFingerprint(demoScanCamera, System.currentTimeMillis());
                json = gson.toJson(message);

            }
            else if(demoCounter%30 < 20){
                MessageFingerprint message = new MessageFingerprint(demoScanSoggiorno, System.currentTimeMillis());
                json = gson.toJson(message);

            }
            else{
                MessageFingerprint message = new MessageFingerprint(demoScanBagno, System.currentTimeMillis());
                json = gson.toJson(message);

            }
            if (clientSocket != null) {
                clientSocket.sendMessageFingerprint(json);
                demoCounter++;
            }*/
        }

        private void scanFailure() {
            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<ScanResult> listScan = new ArrayList<>();
            for ( android.net.wifi.ScanResult res : results ){
                ScanResult scan = new ScanResult(res.BSSID,res.SSID,res.level);
                listScan.add(scan);
            }
            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan, System.currentTimeMillis());
            String json = gson.toJson(message);
            if(clientSocket!=null) clientSocket.sendMessageFingerprint(json);
        }


    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();

        System.out.println("onDestroy activity live");
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
                    scanService.registerReceiver(broadcastReceiverScan);
                    mHandler.postDelayed(scanRunnable, 3000);
                    break;
            }
        }
    }

}
