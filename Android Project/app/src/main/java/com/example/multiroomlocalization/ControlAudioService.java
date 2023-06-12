package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.LoginActivity.client;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.multiroomlocalization.Bluetooth.ConnectBluetoothManager;
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

    ControlAudioService(Activity activity, @NonNull View view){
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

    public void initBluetoothManagerIfNot(){
        if(bluetoothManager == null)
            bluetoothManager = new ConnectBluetoothManager(activity);
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

                    System.out.println("connection connected");

                    downloadAudioTracks();
                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    Log.i("connection","suspended");
                    if(bluetoothManager!=null)
                        bluetoothManager.disconnect();
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    mediaBrowser.unsubscribe("root");
                    Log.i("connection","failed");
                }
            };


    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    int totalDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                    System.out.println(metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    System.out.println(metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE));
                    if(totalDuration != 0L)
                        audioSeekBar.setMax(totalDuration);
                }


                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (PlaybackStateCompat.STATE_PLAYING == state.getState()) {

                        Drawable drawablePause = activity.getDrawable(android.R.drawable.ic_media_pause);

                        System.out.println(playPause.getDrawable().equals(drawablePause));
                        if(!playPause.getDrawable().equals(drawablePause))
                            playPause.setImageResource(android.R.drawable.ic_media_pause);

                        if(playlistAdapter != null){
                            int current = AudioPlaybackService.currentTrack;
                            if(current >= 0) {
                                ImageButton btn = playlistAdapter.getPlayButtons().get(current);
                                if(!btn.getDrawable().equals(drawablePause))
                                    btn.setImageResource(android.R.drawable.ic_media_pause);
                            }
                        }

                        long pos = state.getPosition();
                        audioSeekBar.setProgress(Math.toIntExact(pos));

                        int seconds = (int) (pos / 1000) % 60 ;
                        int minutes = (int) ((pos / (1000*60)) % 60);

                        CharSequence time = minutes+":"+seconds;
                        ((TextView) view.findViewById(R.id.audio_time)).setText(time);

                    } else if (PlaybackStateCompat.STATE_PAUSED == state.getState()) {

                        Drawable drawablePlay = activity.getDrawable(android.R.drawable.ic_media_play);
                        if(!playPause.getDrawable().equals(drawablePlay))
                            playPause.setImageResource(android.R.drawable.ic_media_play);

                        if(playlistAdapter != null){
                            int current = AudioPlaybackService.currentTrack;
                            if(current >= 0) {
                                ImageButton btn = playlistAdapter.getPlayButtons().get(current);
                                if(!btn.getDrawable().equals(drawablePlay))
                                    btn.setImageResource(android.R.drawable.ic_media_play);
                            }
                        }
                    }
                }
                @Override
                public void onSessionDestroyed() {
                    mediaBrowser.disconnect();
                    if(bluetoothManager!= null)
                        bluetoothManager.disconnect();
                    // maybe schedule a reconnection using a new MediaBrowser instance
                }
            };

    public void connectToSpeaker(Speaker speaker){

        if(bluetoothManager!= null)
            bluetoothManager.connectDevice(speaker);

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

        RelativeLayout audioControllerView = (RelativeLayout) activity.findViewById(R.id.audiocontroller);

        ViewGroup.LayoutParams backupLayoutParams = audioControllerView.getLayoutParams();

        ListView audioPlaylistView = (ListView) activity.findViewById(R.id.playlist_view);
        ImageButton closePlaylistView = (ImageButton) activity.findViewById(R.id.closePlaylistButton);

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

    private void downloadAudioTracks() {


        client.sendMessageReqPlaylist((playlist) -> {


            System.out.println(playlist);
            myPlaylist.set(playlist);

            playlistAdapter = new ListSongAdapter(R.id.playlist_view, activity, gson.fromJson(playlist, MessagePlaylist.class).getSong(), activity);


            ListView audioPlaylistView = (ListView) view.findViewById(R.id.playlist_view);
            if(audioPlaylistView != null)
                audioPlaylistView.setAdapter(playlistAdapter);
        });

    }
    private void buildTransportControls() {
        // Grab the view for the play/pause button
        ImageButton playPause = (ImageButton) view.findViewById(R.id.playpause);
        TextView timeTextView = (TextView) view.findViewById(R.id.audio_time);
        // Attach a listener to the button
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    Log.i("button","pausa");
                    MediaControllerCompat.getMediaController(activity).getTransportControls().pause();
                } else {
                    Log.i("button","play");

                    if(!AudioPlaybackService.isMyServiceRunning){

                        if(myPlaylist.get() != null) {
                            Intent intent = new Intent(activity, AudioPlaybackService.class);
                            intent.putExtra("playlist", myPlaylist.get());
                            activity.startService(intent);
                        }else
                            Toast.makeText(activity,"Waiting for playlist...", Toast.LENGTH_LONG).show();
                    }else
                        MediaControllerCompat.getMediaController(activity).getTransportControls().play();
                }
            }});

        ImageButton nextTrack = (ImageButton) view.findViewById(R.id.nexttrack);
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED ) {
                    MediaControllerCompat.getMediaController(activity).getTransportControls().skipToNext();
                }
            }
        });

        ImageButton previousTrack = (ImageButton) view.findViewById(R.id.previoustrack);
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(activity).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING || pbState == PlaybackStateCompat.STATE_PAUSED ) {

                    MediaControllerCompat.getMediaController(activity).getTransportControls().skipToPrevious();
                }
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);

    }

    public void disconnectMediaBrowser() {
        if (MediaControllerCompat.getMediaController(activity) != null) {
            MediaControllerCompat.getMediaController(activity).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
        if(bluetoothManager!=null)
            bluetoothManager.disconnect();
    }

    public void connectMediaBrowser() {
        mediaBrowser.connect();
    }


}
