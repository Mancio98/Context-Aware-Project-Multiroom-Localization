package com.example.multiroomlocalization;

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

import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;


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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

    @Override
    protected void onStart() {
        super.onStart();

        mediaBrowser.connect();

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
        mediaBrowser.disconnect();
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

        if (requestCode == BT_CONNECT_AND_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                launchAssignRAFragment();
            } else {
                Toast.makeText(this, "BT Permission Denied", Toast.LENGTH_SHORT).show();
            }

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