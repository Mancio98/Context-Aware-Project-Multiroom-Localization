package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.BT_CONNECT_AND_SCAN;
import static com.example.multiroomlocalization.MainActivity.btUtility;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.lang.ref.WeakReference;
import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothThread;
import com.example.multiroomlocalization.Music.ListSongAdapter;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.example.multiroomlocalization.speaker.Speaker;

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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.localization.MessageMapDetails;
import com.example.multiroomlocalization.messages.music.MessageSettings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
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

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.multiroomlocalization.PlayNewAudio";
    public static Activity activity;
    private ConnectBluetoothThread connectBluetoothThread;
    public static WeakReference<ActivityLive> weakActivity;
    boolean serviceBound = false;


    private ImageView playPause;
    private SeekBar audioSeekBar;

    private HashMap<String,ArrayList<com.example.multiroomlocalization.ScanResult>> resultScan = new HashMap<>();
    private ArrayList<com.example.multiroomlocalization.ScanResult> scanResultArrayList = new ArrayList<com.example.multiroomlocalization.ScanResult>();

    private int seekPosition;
    private ImageButton nextTrack;
    private ImageButton previousTrack;
    private ArrayList<ListRoomsElement> deviceForRoom;
    private boolean onTop = false;
    private float startPlaylistX;
    private float startPlaylistY;
    private ArraySet<Speaker> listSpeaker;
    private TextView timeTextView;
    private ListView audioPlaylistView;
    private ListSongAdapter playlistAdapter;


    private final Gson gson = new Gson();
    private ControlAudioService setupAudioService;


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

                ReferencePointListAdapter adapter = new ReferencePointListAdapter(referencePoints,getApplicationContext(),activity);
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

                connectBluetoothDevice(currentRef.getSpeaker());
            }
        };

        clientSocket.setCallbackChangeReferencePoint(callback);
        scanService.registerReceiver(broadcastReceiverScan);
        mHandler.postDelayed(scanRunnable, 3000);

        weakActivity = new WeakReference<>(ActivityLive.this);

        setupAudioService = new ControlAudioService(activity, (View)findViewById(R.id.activity_live_layout), playlistAdapter);
        setupMusicPlayer();

        btUtility = new BluetoothUtility(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupAudioService.connectMediaBrowser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setupAudioService.unregisterCallback();
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                    //launchAssignRAFragment();
                } else {
                    Toast.makeText(this, "BT Permission Denied", Toast.LENGTH_SHORT).show();
                }
            case 1: //DEFINIRE CODICE RICHIESTA
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ActivityLive.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(ActivityLive.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                }

        }
    }




    public void askBtPermission(View view) {


        /*if(BluetoothUtility.checkPermission(activity))
            launchAssignRAFragment();
        //startBluetoothConnection();

        /*
        clientSocket.createMessageNewReferencePoint(new com.example.multiroomlocalization.localization.ReferencePoint(
                "a",
                new Speaker("F8:FF:C2:54:C1:28","Cucina","MacBook Pro di Mancio",0)
        )).execute();
        clientSocket.createMessageNewReferencePoint(new com.example.multiroomlocalization.localization.ReferencePoint(
                "a",
                new Speaker("45689","salotto","speaker2",0)
        )).execute();*/

        List<com.example.multiroomlocalization.ScanResult> list = new ArrayList<>();


        if(connectBluetoothThread != null)
            connectBluetoothThread = new ConnectBluetoothThread(activity);

        /*IntentFilter filter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(btUtility.getConnectA2dpReceiver(), filter);*/



        //clientSocket.sendMessageFingerPrint(list);

    }


    private void setupMusicPlayer() {
        audioSeekBar = (SeekBar) findViewById(R.id.seekBar);
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pbState = MediaControllerCompat.getMediaController(ActivityLive.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {

                    int progress = seekBar.getProgress();
                    MediaControllerCompat.getMediaController(ActivityLive.this).getTransportControls().seekTo(progress);
                }
            }
        });

        RelativeLayout audioControllerView = (RelativeLayout) findViewById(R.id.audiocontroller);

        ViewGroup.LayoutParams backupLayoutParams = audioControllerView.getLayoutParams();

        ListView audioPlaylistView = (ListView) findViewById(R.id.playlist_view);
        ImageButton closePlaylistView = (ImageButton) findViewById(R.id.closePlaylistButton);

        audioControllerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onTop) {
                    Animation slideUp = AnimationUtils.loadAnimation(activity, R.anim.slide_up);


                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            System.out.println(v.getY());
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            audioPlaylistView.setVisibility(View.VISIBLE);
                            closePlaylistView.setVisibility(View.VISIBLE);

                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);

                            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            v.setLayoutParams(params);

                            //v.setY(0.0f);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    audioControllerView.startAnimation(slideUp);


                    onTop = true;
                }

            }
        });

        closePlaylistView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTop) {
                    Animation slideDown = AnimationUtils.loadAnimation(activity, R.anim.slide_down);

                    slideDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            audioPlaylistView.setVisibility(View.INVISIBLE);
                            closePlaylistView.setVisibility(View.INVISIBLE);
                            ((RelativeLayout) v.getParent()).setLayoutParams(backupLayoutParams);

                            /*DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            float height = displayMetrics.heightPixels - ((View)v.getParent()).getHeight();

                            System.out.println(height);
                            ((View)v.getParent()).setY(height);*/
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    audioControllerView.startAnimation(slideDown);

                    onTop = false;
                }
            }
        });
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
            for ( android.net.wifi.ScanResult res : results ){
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

    public static ActivityLive getInstance(){
        return weakActivity.get();
    }

    private void connectBluetoothDevice(Speaker speaker){
        if(connectBluetoothThread!= null)
            connectBluetoothThread.connectDevice(speaker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();

        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(btUtility.getConnectA2dpReceiver());

            activity.unregisterReceiver(btUtility.getConnectA2dpReceiver());
        }catch (Exception e){
            e.printStackTrace();
        }

        if(connectBluetoothThread != null)
            connectBluetoothThread.disconnect();

        activity = null;
        weakActivity = null;
        clientSocket = null;
        /*
        if (serviceBound) {
            unbindService(serviceConnection);
            mediaController.setMediaPlayer(null);
            mediaController.hide();
            //service is active
            playerService.stopSelf();
        }*/
    }
}
