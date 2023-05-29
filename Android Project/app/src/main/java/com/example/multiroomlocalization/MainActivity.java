package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.BT_CONNECT_AND_SCAN;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.util.ArraySet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothThread;
import com.example.multiroomlocalization.Music.ListSongAdapter;
import com.example.multiroomlocalization.databinding.ActivityMainBinding;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.speaker.MessageChangeReferencePoint;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.example.multiroomlocalization.speaker.Speaker;

import com.google.gson.Gson;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {


    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.multiroomlocalization.PlayNewAudio";
    public static Activity activity;
    private ConnectBluetoothThread connectBluetoothThread;
    public static BluetoothUtility btUtility;
    public static WeakReference<MainActivity> weakActivity;
    boolean serviceBound = false;


    private ImageView playPause;
    private SeekBar audioSeekBar;

    private ActivityMainBinding binding;
    private Handler mHandler = new Handler();
    private ScanService scanService;
    private Canvas canvas;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText labelRoom;
    private Button cancel, save, upload;
    private ImageView imageView;
    private Bitmap mutableBitmap;
    private int imageViewHeight;
    private int imageViewWidth;
    private boolean first = true;
    private boolean newImage = true;
    private int intervalScan = 30000;
    private int timerScanTraining = 60000; //* 5 //60000 = 1 min

    protected ClientSocket clientSocket;

    private ArrayList<com.example.multiroomlocalization.ReferencePoint> referencePoints = new ArrayList<com.example.multiroomlocalization.ReferencePoint>();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientSocket = LoginActivity.clientSocket;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setContentView(R.layout.activity_main_temp_mansio);
        setSupportActionBar(binding.toolbar);

        binding.fab.setEnabled(false);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 2);
            }
        });


        activity = this;
        weakActivity = new WeakReference<>(MainActivity.this);

        setupMusicPlayer();

        setupAudioService = new ControlAudioService(activity,((View)findViewById(R.id.mainLayout)), playlistAdapter);

        btUtility = new BluetoothUtility(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.setFragmentResultListener("requestDevice", this, (requestKey, result) -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deviceForRoom = result.getSerializable("deviceForRoom", ArrayList.class);

            }
            else
                deviceForRoom = (ArrayList<ListRoomsElement>) result.getSerializable("deviceForRoom");

            if(deviceForRoom != null) {
                listSpeaker = new ArraySet<>();
                deviceForRoom.forEach(room -> {
                    BluetoothDevice device = room.getDevice();
                    listSpeaker.add(new Speaker(device.getName(), device.getAddress(), room.getName(), device.getBluetoothClass().getDeviceClass()));
                });

                clientSocket.sendMessageListSpeaker(listSpeaker);

                /* fare metodo per inizializzre il thread
                connectBluetoothThread = new ConnectBluetoothThread(activity);

                IntentFilter filter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_UUID);
                registerReceiver(btUtility.getConnectA2dpReceiver(), filter); */
            }

        });
        //DA RIVEDERE




        //imageView = (ImageView) findViewById(R.id.map);
/*
        imageView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageViewHeight = imageView.getHeight();
                imageViewWidth = imageView.getWidth();
                int xGlobal = imageView.getLeft();
                int yGlobal = imageView.getTop();

                System.out.println("Global");
                System.out.println("X: " + xGlobal);
                System.out.println("Y: " + yGlobal);
                System.out.println("Height: " + imageViewHeight + " Width: " + imageViewWidth);
                // don't forget to remove the listener to prevent being called again
                // by future layout events:
                if(first || newImage) {
                    first=false;
                    newImage = false;
                    Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), imageViewWidth, imageViewHeight, true);
                    mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    canvas = new Canvas(mutableBitmap);

                }
            });

        }else{

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        imageView.setOnTouchListener(touchListener);

                createDialog(tempx,tempy);
                return false;
            }
        });*/

        //downloadAudioTracks();
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x1 = (int) event.getX();
            int y1 = (int) event.getY();

            System.out.println("X: " + x1 + " Y: " + y1);
            int tempx = x1;
            int tempy = y1;

            createDialog(tempx,tempy);
            return false;
        }
    };

    ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            imageViewHeight = imageView.getHeight();
            imageViewWidth = imageView.getWidth();
            int xGlobal = imageView.getLeft();
            int yGlobal = imageView.getTop();

            System.out.println("Global");
            System.out.println("X: " + xGlobal);
            System.out.println("Y: " + yGlobal);
            System.out.println("Height: " + imageViewHeight + " Width: " + imageViewWidth);
            // don't forget to remove the listener to prevent being called again
            // by future layout events:
            if(first || newImage) {
                first=false;
                newImage = false;
                Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), imageViewWidth, imageViewHeight, true);
                mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                canvas = new Canvas(mutableBitmap);
            }
        }
    };

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanService.startScan();
            mHandler.postDelayed(scanRunnable, intervalScan);

        }
    };

    private void stopScan(){
        mHandler.removeCallbacks(scanRunnable);
    }

    public static MainActivity getInstance(){
        return weakActivity.get();
    }
    public void connectBluetoothDevice(Speaker speaker){
        if(connectBluetoothThread!= null)
            connectBluetoothThread.connectDevice(speaker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            dialogBuilder = new AlertDialog.Builder(this);
            final View popup = getLayoutInflater().inflate(R.layout.popup_upload_map, null);
            upload = (Button) popup.findViewById(R.id.upload);

            dialogBuilder.setView(popup);
            dialog = dialogBuilder.create();

            dialog.show();

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // create an instance of the
                    // intent of the type image
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    someActivityResultLauncher.launch(i);
                }
            });


            System.out.println("Presss");
            return true;
        }
        else if(id==R.id.action_client){


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri image = data.getData();
                        //Uri destImage = null;

                        if (null != image) {

                            dialog.cancel();
                            CropView cv = new CropView(MainActivity.this, image);
                            cv.setCanceledOnTouchOutside(false);
                            cv.show();
                            cv.setTouchButton(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), cv.getCropImageView().getCroppedImage(), "Title", null);

                                    imageView.setImageURI(Uri.parse(path));
                                    imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
                                    imageView.setOnTouchListener(touchListener);

                                    newImage=true;

                                    cv.cancel();

                                    startPopup();
                                    return false;
                                }
                            });
                        }


                    }
                }
            });


    @SuppressLint("ClickableViewAccessibility")
    private void startPopup(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
        Button next = (Button) popup.findViewById(R.id.buttonPopup);
        TextView text = (TextView) popup.findViewById(R.id.textPopup);

        final boolean[] firstTime = {true};

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstTime[0]){
                    text.setText(getString(R.string.addReferencePointpopupSecond));
                    next.setText("Start");
                    firstTime[0] = false;
                }
                else{
                    dialog.cancel();
                }
            }
        });
        text.setText(getString(R.string.addReferencePointpopup));

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            switch (requestCode){
                case 2:
                    /*
                    clientSocket = new ClientSocket();
                    clientSocket.setContext(getApplicationContext());
                    clientSocket.start();*/

                    //new ClientSocket.MessageStartMappingPhase().execute();
                    clientSocket.createMessageStartMappingPhase().execute();

                    createPopupStartTraining();
            }



        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
 /*   @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
                scanService = new ScanService(getApplicationContext());

                mHandler.postDelayed(scanRunnable, intervalScan);
            }
            else {
                Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == 0) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/
    private void createDialog(int x,int y){
        dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_room, null);
        labelRoom = (EditText) popup.findViewById(R.id.labelRoom);
        cancel = (Button) popup.findViewById(R.id.buttonCancel);
        save = (Button) popup.findViewById(R.id.buttonSave);

        save.setEnabled(false);

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        labelRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.toString().trim().length()==0){
                    save.setEnabled(false);
                } else {
                    save.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Paint mPaint = new Paint();
                mPaint.setColor(Color.RED);
                canvas.drawCircle(x, y, 20, mPaint);
                imageView.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                ReferencePoint ref = new ReferencePoint(x,y,labelRoom.getText().toString());
                referencePoints.add(ref);
                Toast.makeText(getApplicationContext(), "Stanza aggiunta correttamente", Toast.LENGTH_LONG).show();
                binding.fab.setEnabled(true);
                dialog.cancel();
            }
        });

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
                    launchAssignRAFragment();
                } else {
                    Toast.makeText(this, "BT Permission Denied", Toast.LENGTH_SHORT).show();
                }
            case 1: //DEFINIRE CODICE RICHIESTA
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();

                }
                else {
                    Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                }

        }
    }


    private void launchAssignRAFragment(){

        FrameLayout frame = findViewById(R.id.RaRooms);
        frame.bringToFront();
        frame.setVisibility(View.VISIBLE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RoomRAFragment btFragment = new RoomRAFragment();

        fragmentTransaction.replace(R.id.RaRooms, btFragment);
        fragmentTransaction.addToBackStack("btFragment");

        fragmentTransaction.commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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



        clientSocket.sendMessageFingerPrint(list);

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
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {

                    int progress = seekBar.getProgress();
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().seekTo(progress);
                }
            }
        });

        /* service
        nextTrack = (ImageButton) findViewById(R.id.nexttrack);
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myExoPlayer != null) {
                    int pbState = myExoPlayer.getPlaybackState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {
                        myExoPlayer.seekToNext();
                    }
                }
            }
        });

        previousTrack = (ImageButton) findViewById(R.id.previoustrack);
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myExoPlayer != null){

                    int pbState = myExoPlayer.getPlaybackState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED ) {
                        myExoPlayer.seekToPrevious();
                    }
                }
            }
        });

        playPause = (ImageButton) findViewById(R.id.playpause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myExoPlayer != null){

                    if (myExoPlayer.isPlaying())
                        myExoPlayer.pause();
                    else
                        myExoPlayer.play();
                }
            }
        });*/


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





    private void createPopupRoomTraining(com.example.multiroomlocalization.ReferencePoint point,int index){
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final View popup = getLayoutInflater().inflate(R.layout.layout_scan_training, null);
        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        Button buttonNext = popup.findViewById(R.id.buttonTraining);

        TextView textRoom = (TextView) popup.findViewById(R.id.textRoom);
        textRoom.setText(point.getId());

        TextView timer = (TextView) popup.findViewById(R.id.timer);
        timer.setText("seconds remaining: 05:00");

        //clientSocket.createMessageNewReferencePoint(point).execute();

        CountDownTimer countDownTimer = new CountDownTimer(timerScanTraining, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText("seconds remaining: " +new SimpleDateFormat("mm:ss").format(new Date( millisUntilFinished)));
            }

            public void onFinish() {

                buttonNext.setEnabled(true);
                buttonNext.setText("STOP");
                buttonNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        timer.setText("Stanza completata");

                        mHandler.removeCallbacks(scanRunnable);

                        resultScan.put(referencePoints.get(index).getId(),scanResultArrayList);
                        System.out.println(resultScan);

                        if (index+1<referencePoints.size()){
                            buttonNext.setText("Next");
                            buttonNext.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                    clientSocket.createMessageEndScanReferencePoint().execute();
                                    createPopupRoomTraining(referencePoints.get(index+1), index+1);
                                }
                            });
                        }
                        else {
                            buttonNext.setText("Finish");
                            buttonNext.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    clientSocket.createMessageEndScanReferencePoint().execute();
                                    clientSocket.createMessageEndMappingPhase().execute();
                                    // SALVATAGGIO DATI
                                    dialog.cancel();
                                }
                            });
                        }

                    }
                });




            }
        };

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanService = new ScanService(getApplicationContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    if(!scanService.getWifiManager().isScanThrottleEnabled()){//Settings.Global.getInt(getApplicationContext().getContentResolver(), "wifi_scan_throttle_enabled") == 0){
                        intervalScan = 5000;
                        System.out.println("IntervalScan: " + intervalScan);
                    }
                    else {
                        intervalScan = 30000;
                        System.out.println("IntervalScan: " + intervalScan);
                    }
                }

                mHandler.postDelayed(scanRunnable, 0);
                scanService.registerReceiver(broadcastReceiverScan);

                scanResultArrayList.clear();

                countDownTimer.start();

                //new ClientSocket.MessageStartScanReferencePoint().execute();
                clientSocket.createMessageStartScanReferencePoint().execute();

                buttonNext.setEnabled(false);
            }
        });

        dialog.show();
    }

   private void createPopupStartTraining(){
       dialogBuilder = new AlertDialog.Builder(MainActivity.this);
       final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
       dialogBuilder.setView(popup);
       dialog = dialogBuilder.create();


       Button next = (Button) popup.findViewById(R.id.buttonPopup);
       TextView text = (TextView) popup.findViewById(R.id.textPopup);

       text.setText(getString(R.string.trainingText));
       next.setText("Next");

       next.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               dialog.cancel();
               int i=0;
               createPopupRoomTraining(referencePoints.get(i),i);
           }
       });

       dialog.setCanceledOnTouchOutside(false);
       dialog.show();
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

       private void scanSuccess(){
           List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
           List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
           for ( ScanResult res : results ) {
               com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID,res.SSID,res.level);
               listScan.add(scan);
               System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
           }
           clientSocket.createMessageFingerprint(listScan).execute();

       }

       private void scanFailure(){
           List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
           List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
           for ( ScanResult res : results ) {
               com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID,res.SSID,res.level);
               listScan.add(scan);
               System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
           }
           clientSocket.createMessageFingerprint(listScan).execute();
       }


   };
}
