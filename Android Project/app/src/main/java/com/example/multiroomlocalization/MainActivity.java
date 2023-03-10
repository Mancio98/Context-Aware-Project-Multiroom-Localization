package com.example.multiroomlocalization;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;


import android.os.Handler;

import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

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

import com.example.multiroomlocalization.databinding.ActivityMainBinding;
import com.example.multiroomlocalization.socket.ClientSocket;
//import com.yalantis.ucrop.UCrop;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    protected static final int BT_CONNECT_AND_SCAN = 101;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.multiroomlocalization.PlayNewAudio";
    private ServerSLAC server;
    private Activity activity;
    private ConnectBluetoothThread connectBluetoothThread;
    protected static BluetoothUtility btUtility;

    boolean serviceBound = false;

    private MediaBrowserCompat mediaBrowser;

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
    private int intervalScan = 10000;

    private ArrayList<ReferencePoint> referencePoints = new ArrayList<ReferencePoint>();

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {

                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController =
                            new MediaControllerCompat(MainActivity.this, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                    Log.i("connection","connected");
                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    Log.i("connection","suspended");
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection

                    Log.i("connection","failed");
                }
            };

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    int totalDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                    audioSeekBar.setMax(totalDuration);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (PlaybackStateCompat.STATE_PLAYING == state.getState()) {
                        playPause.setImageResource(android.R.drawable.ic_media_pause);
                        audioSeekBar.setProgress(Math.toIntExact(state.getPosition()));

                    } else if (PlaybackStateCompat.STATE_PAUSED == state.getState()) {
                        playPause.setImageResource(android.R.drawable.ic_media_play);

                    }
                }
                @Override
                public void onSessionDestroyed() {
                    mediaBrowser.disconnect();
                    // maybe schedule a reconnection using a new MediaBrowser instance
                }
            };
    private int seekPosition;
    private ImageButton nextTrack;
    private ImageButton previousTrack;
    private Serializable deviceForRoom;
    private boolean onTop = false;

    void buildTransportControls() {
        // Grab the view for the play/pause button
        playPause = (ImageButton) findViewById(R.id.playpause);

        // Attach a listener to the button
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    Log.i("button","pausa");
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
                } else {
                    Log.i("button","play");
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().playFromMediaId(String.valueOf(0),null);
                }
            }});

        nextTrack = (ImageButton) findViewById(R.id.nexttrack);
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED ) {
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext();
                }
            }
        });

        previousTrack = (ImageButton) findViewById(R.id.previoustrack);
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED ) {

                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToPrevious();
                }
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);

    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_temp_mansio);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1 );
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 1);
            }
        });

        Button scanBT = (Button) findViewById(R.id.scanBT);

        scanBT.setOnClickListener(askBtPermission);
        activity = this;






        audioSeekBar = (SeekBar) findViewById(R.id.seekBar);
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {

                    int progress = seekBar.getProgress();
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().seekTo(progress);
                }
            }
        });
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, AudioPlaybackService.class),
                connectionCallbacks,
                null);




        btUtility = new BluetoothUtility(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.setFragmentResultListener("requestDevice", this, (requestKey, result) -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deviceForRoom = result.getSerializable("requestDevice", ArrayList.class);

            }
            else
                deviceForRoom =  result.getSerializable("requestDevice");

            //TODO send to SLAC the array
        });


        RelativeLayout audioControllerView = (RelativeLayout) findViewById(R.id.audiocontroller);

        ListView audioPlaylistView = (ListView) findViewById(R.id.playlist_view);
        audioControllerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onTop){
                    Animation slideUp = AnimationUtils.loadAnimation(activity,R.anim.slide_up);
                    audioControllerView.setLayoutAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            audioPlaylistView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    audioControllerView.startAnimation(slideUp);


                    onTop = true;
                }
                else{

                    Animation slideDown = AnimationUtils.loadAnimation(activity,R.anim.slide_down);
                    audioControllerView.setLayoutAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            audioPlaylistView.setVisibility(View.INVISIBLE);
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

            ClientSocket client = new ClientSocket();
            client.setContext(getApplicationContext());
            client.start();

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

        next.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dialog.cancel();
                return false;
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
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            scanService = new ScanService(getApplicationContext());

            mHandler.postDelayed(scanRunnable, intervalScan);
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
                dialog.cancel();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //DA SCOMMENTARE
        //mediaBrowser.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(controllerCallback);
        }
        //mediaBrowser.disconnect();
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

    /*
    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlaybackService.LocalBinder binder = (AudioPlaybackService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();

            mediaController = new myAudioController(getApplicationContext());
            mediaController.setAnchorView(findViewById(R.id.audiocontroller));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            mediaController = null;
        }
    };



    private void playPause(String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, AudioPlayerService.class);
            playerIntent.putExtra("song", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }*/
    BroadcastReceiver connectA2dpReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            Log.d("a2dp", "receive intent for action : " + action);
            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    //setIsA2dpReady(true);
                    Log.i("connesso2", "diocane");
                    //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                    //setIsA2dpReady(false);
                }
            } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                if (state == BluetoothA2dp.STATE_PLAYING) {
                    Log.d("a2dp", "A2DP start playing");
                    Toast.makeText(activity, "A2dp is playing", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("a2dp", "A2DP not playing");
                    Toast.makeText(activity, "A2dp not playing", Toast.LENGTH_SHORT).show();
                }
            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){

                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
                if (state == BluetoothDevice.BOND_BONDED) {
                    Log.d("bonded", "bonded");

                } else {
                    Log.d("bonded", "not bonded");

                }
            }
            else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                // This is when we can be assured that fetchUuidsWithSdp has completed.
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        System.out.println("uuidExtra - " + p);
                    }
                    if (connectBluetoothThread != null){
                        connectBluetoothThread.start();
                    }

                } else {
                    System.out.println("uuidExtra is still null");
                }

            }

        }
    };

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
                    scanService = new ScanService(getApplicationContext());

                    mHandler.postDelayed(scanRunnable, intervalScan);
                }
                else {
                    Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;

        }
    }


    private void startBluetoothConnection() {
        BluetoothManager manager = getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = manager.getAdapter();

        BluetoothUtility.checkPermission(this);
        Set<BluetoothDevice> devices = adapter.getBondedDevices();

        BluetoothDevice device = null;
        if (devices.size() <= 0)
            Log.i("name", "vaffanculo");
        else {
            for (BluetoothDevice elem : devices) {

                Log.i("name", elem.getName());
                if (elem.getName().equals("UE BOOM 2"))
                    device = elem;
            }

            IntentFilter filter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_UUID);
            registerReceiver(connectA2dpReceiver, filter);


            connectBluetoothThread = new ConnectBluetoothThread(this, adapter);
            connectBluetoothThread.connectDevice(device);
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
    View.OnClickListener askBtPermission = new View.OnClickListener() {


        @Override
        public void onClick(View view) {



            if(BluetoothUtility.checkPermission(activity))
                launchAssignRAFragment();
            //startBluetoothConnection();


        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.interrupt();
        server = null;
        activity = null;
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