package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.LoginActivity.client;
import static com.example.multiroomlocalization.Music.AudioPlaybackService.isMyServiceRunning;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothManager;
import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.Music.AudioPlaybackService;
import com.example.multiroomlocalization.Music.ListSongAdapter;
import com.example.multiroomlocalization.messages.music.MessagePlaylist;
import com.example.multiroomlocalization.speaker.Speaker;
import com.google.gson.Gson;

import java.util.concurrent.atomic.AtomicReference;

public class ControlAudioService {

    private final View view;
    private final Activity activity;
    private ListSongAdapter playlistAdapter;
    private final MediaBrowserCompat mediaBrowser;
    private final SeekBar audioSeekBar;
    private final ImageButton playPause;
    private final Gson gson = new Gson();
    private final AtomicReference<String> myPlaylist = new AtomicReference<>();
    private boolean onTop = false;
    private ConnectBluetoothManager bluetoothManager;
    public static boolean blockAutoPlay = true;

    ControlAudioService(Activity activity, @NonNull View view) {
        this.activity = activity;
        this.view = view;
        mediaBrowser = new MediaBrowserCompat(activity,
                new ComponentName(activity, AudioPlaybackService.class),
                connectionCallbacks,
                null);
        this.audioSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        this.playPause = (ImageButton) view.findViewById(R.id.playpause);

        setupMusicPlayer();

    }

    public void initBluetoothManagerIfNot(ScanBluetoothService service) {
        if (bluetoothManager == null)
            bluetoothManager = new ConnectBluetoothManager(activity, service);
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {

                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController =
                            new MediaControllerCompat(activity, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(activity, mediaController);


                    registerReceiverBluetooth();
                    downloadAudioTracks();
                    // Finish building the UI
                    buildTransportControls();
                    Log.i("MediaBrowserCompat.ConnectionCallback", "connected");
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects

                    if (bluetoothManager != null)
                        bluetoothManager.disconnectEverything(true);
                    Log.i("MediaBrowserCompat.ConnectionCallback", "suspended");
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    mediaBrowser.unsubscribe("root");
                    Log.i("MediaBrowserCompat.ConnectionCallback", "failed");
                }
            };

    private void registerReceiverBluetooth() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.EXTRA_CLASS);
        activity.registerReceiver(connectBluetoothReceiver, intentFilter);
    }

    private void unregisterReceiverBluetooth() {
        activity.unregisterReceiver(connectBluetoothReceiver);
    }


    private boolean isPlaying = false;
    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    int totalDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

                    if (totalDuration != 0L)
                        audioSeekBar.setMax(totalDuration);
                }


                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    if (PlaybackStateCompat.STATE_PLAYING == state.getState()) {

                        isPlaying = true;

                        playPause.setImageResource(android.R.drawable.ic_media_pause);

                        ListView audioPlaylistView = (ListView) activity.findViewById(R.id.playlist_view);
                        if(audioPlaylistView.getVisibility() == View.VISIBLE) {
                            int current = AudioPlaybackService.currentTrack;

                            ImageButton btn;
                            for(int i=0; i < audioPlaylistView.getChildCount(); i++){

                                btn = (ImageButton) audioPlaylistView.getChildAt(i).findViewById(R.id.play_list);
                                if( i != current)
                                    btn.setImageResource(android.R.drawable.ic_media_play);
                                else
                                    btn.setImageResource(android.R.drawable.ic_media_pause);
                            }

                        }

                        long pos = state.getPosition();
                        audioSeekBar.setProgress(Math.toIntExact(pos));

                        int seconds = (int) (pos / 1000) % 60;
                        int minutes = (int) ((pos / (1000 * 60)) % 60);

                        CharSequence time = minutes + ":" + seconds;
                        ((TextView) view.findViewById(R.id.audio_time)).setText(time);

                    } else if (PlaybackStateCompat.STATE_PAUSED == state.getState()) {
                        isPlaying = false;

                        playPause.setImageResource(android.R.drawable.ic_media_play);

                        ListView audioPlaylistView = (ListView) activity.findViewById(R.id.playlist_view);
                        if(audioPlaylistView.getVisibility() == View.VISIBLE) {

                            int current = AudioPlaybackService.currentTrack;

                            ImageButton btn = (ImageButton) audioPlaylistView.getChildAt(current).findViewById(R.id.play_list);

                            btn.setImageResource(android.R.drawable.ic_media_play);

                        }

                    }
                }

                @Override
                public void onSessionDestroyed() {
                    mediaBrowser.disconnect();
                    if (bluetoothManager != null)
                        bluetoothManager.disconnectEverything(true);
                    unregisterReceiverBluetooth();

                }
            };

    public void connectToSpeaker(Speaker speaker) {


        if (bluetoothManager != null) {
            if (speaker.getMac() != null && speaker.getName() != null)
                bluetoothManager.connectDevice(speaker);
            else {
                if (isMyServiceRunning) {
                    pausePlayback();
                }
                bluetoothManager.disconnectEverything(false);

            }
        }

    }

    private void stopPlayback() {
        if (isMyServiceRunning) {
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);
            if (mediaController != null)
                mediaController.getTransportControls().stop();
        }

    }

    private void setupMusicPlayer() {

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {

                    int progress = seekBar.getProgress();
                    MediaControllerCompat.getMediaController(activity).getTransportControls().seekTo(progress);
                }
            }
        });


        ListView audioPlaylistView = (ListView) activity.findViewById(R.id.playlist_view);
        ImageButton closePlaylistView = (ImageButton) activity.findViewById(R.id.closePlaylistButton);
        ImageButton openPlaylistView = (ImageButton) activity.findViewById(R.id.openplaylist);

        openPlaylistView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!onTop) {
                    /*Animation slideUp = AnimationUtils.loadAnimation(activity, R.anim.slide_up);

                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {*/
                            audioPlaylistView.setVisibility(View.VISIBLE);
                            closePlaylistView.setVisibility(View.VISIBLE);
                       /* }

                        @Override
                        public void onAnimationEnd(Animation animation) {


                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });

                    audioControllerView.startAnimation(slideUp);*/

                    onTop = true;
                }

            }
        });

        closePlaylistView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTop) {
                    /*Animation slideDown = AnimationUtils.loadAnimation(activity, R.anim.slide_down);

                    slideDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {*/
                            audioPlaylistView.setVisibility(View.GONE);
                            closePlaylistView.setVisibility(View.INVISIBLE);
                        /*}

                        @Override
                        public void onAnimationEnd(Animation animation) {}

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    audioControllerView.startAnimation(slideDown);
                    */
                    onTop = false;
                }
            }
        });
    }

    private void downloadAudioTracks() {


        client.sendMessageReqPlaylist((playlist) -> {



            myPlaylist.set(playlist);

            playlistAdapter = new ListSongAdapter(R.id.playlist_view, activity, gson.fromJson(playlist, MessagePlaylist.class).getSong(), activity);

            ListView audioPlaylistView = (ListView) view.findViewById(R.id.playlist_view);
            if (audioPlaylistView != null) {
                initAudioPlaybackService();
                audioPlaylistView.setAdapter(playlistAdapter);
            }
        });

    }
    private void initAudioPlaybackService(){
        if (!isMyServiceRunning) {

            if (myPlaylist.get() != null) {
                Intent intent = new Intent(activity, AudioPlaybackService.class);
                intent.putExtra("playlist", myPlaylist.get());
                activity.startService(intent);
            }
        }
    }
    private void buildTransportControls() {

        ImageButton playPause = (ImageButton) view.findViewById(R.id.playpause);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playPausePlayback();

            }
        });

        ImageButton nextTrack = (ImageButton) view.findViewById(R.id.nexttrack);
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning) {
                    int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {
                        MediaControllerCompat.getMediaController(activity).getTransportControls().skipToNext();
                    }
                }
            }
        });

        ImageButton previousTrack = (ImageButton) view.findViewById(R.id.previoustrack);
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning) {
                    int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED) {

                        MediaControllerCompat.getMediaController(activity).getTransportControls().skipToPrevious();
                    }
                }
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);


        mediaController.registerCallback(controllerCallback);

    }

    private void playPausePlayback() {


        if (isMyServiceRunning) {
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);
            if (mediaController != null) {

                if (isPlaying) {
                    blockAutoPlay = true;
                    mediaController.getTransportControls().pause();
                }
                else {
                    blockAutoPlay = false;
                    mediaController.getTransportControls().play();
                }
            }

        }

    }

    private void pausePlayback(){
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);
        if (mediaController != null)
            if (isPlaying)
                mediaController.getTransportControls().pause();
    }

    private void playPlayback() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);
        if (mediaController != null)
            if (!isPlaying)
                mediaController.getTransportControls().play();

    }

    private final BroadcastReceiver connectBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();

            int state;

            switch (action){
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                    if (state == BluetoothA2dp.STATE_CONNECTED) {

                        Log.d("a2dp", "A2DP connected");
                        if(isMyServiceRunning && !blockAutoPlay)
                            playPlayback();


                    } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {

                        Log.d("a2dp", "A2DP disconnected");
                        if(isMyServiceRunning && !blockAutoPlay)
                            pausePlayback();

                    }
                    break;
                case (BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED):
                    state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                    if (state == BluetoothA2dp.STATE_PLAYING) {
                        Log.d("a2dp", "A2DP start playing");
                    } else {
                        Log.d("a2dp", "A2DP not playing");

                    }
                    break;
                case(BluetoothDevice.ACTION_BOND_STATE_CHANGED):

                    state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
                    if (state == BluetoothDevice.BOND_BONDED) {
                        Log.d("bonded", "bonded");

                    } else {
                        Log.d("bonded", "not bonded");

                    }
                    break;

            }
           
        }


    };


    public void disconnectMediaBrowser() {
        if (isMyServiceRunning) {

            stopPlayback();
            MediaControllerCompat.getMediaController(activity).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
        if(bluetoothManager!=null)
            bluetoothManager.disconnectEverything(true);
        unregisterReceiverBluetooth();
    }

    public void connectMediaBrowser() {
        mediaBrowser.connect();
    }


}
