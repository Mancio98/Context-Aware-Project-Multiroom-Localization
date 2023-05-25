package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.LoginActivity.clientSocket;
import static com.example.multiroomlocalization.MainActivity.activity;

import android.content.ComponentName;
import android.content.Context;
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

import com.example.multiroomlocalization.Music.AudioPlaybackService;
import com.example.multiroomlocalization.Music.ListSongAdapter;
import com.example.multiroomlocalization.messages.music.MessagePlaylist;
import com.example.multiroomlocalization.messages.music.MessageRequestPlaylist;
import com.google.gson.Gson;

import java.util.concurrent.atomic.AtomicReference;

public class ControlAudioService {

    private final View view;
    private final Context context;
    private ListSongAdapter playlistAdapter;
    private MediaBrowserCompat mediaBrowser;
    private SeekBar audioSeekBar;
    private ImageButton playPause;
    private Gson gson = new Gson();
    private AtomicReference<String> myPlaylist = new AtomicReference<>();
    ControlAudioService(Context context, View view, ListSongAdapter playlistAdapter){
        this.context = context;
        this.view = view;
        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, AudioPlaybackService.class),
                connectionCallbacks,
                null);
        this.audioSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        this.playPause = (ImageButton) view.findViewById(R.id.playpause);
        this.playlistAdapter = playlistAdapter;
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

                    Log.i("connection","connected");
                    downloadAudioTracks();
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

                        Drawable drawablePause = context.getDrawable(android.R.drawable.ic_media_pause);

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

                        Drawable drawablePlay = context.getDrawable(android.R.drawable.ic_media_play);
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
                    // maybe schedule a reconnection using a new MediaBrowser instance
                }
            };

    private void downloadAudioTracks() {


        String messageReqPlaylist = gson.toJson(new MessageRequestPlaylist());
        clientSocket.sendMessageReqPlaylist((playlist) -> {

            System.out.println(playlist);
            myPlaylist.set(playlist);

            playlistAdapter = new ListSongAdapter(R.id.playlist_view, activity, gson.fromJson(playlist, MessagePlaylist.class).getSong(), activity);


            ListView audioPlaylistView = (ListView) view.findViewById(R.id.playlist_view);
            if(audioPlaylistView != null)
                audioPlaylistView.setAdapter(playlistAdapter);
        },
            messageReqPlaylist
                );

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
                            Intent intent = new Intent(context, AudioPlaybackService.class);
                            intent.putExtra("playlist", myPlaylist.get());
                            context.startService(intent);
                        }else
                            Toast.makeText(context,"Waiting for playlist...", Toast.LENGTH_LONG).show();
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

    public void unregisterCallback() {
        if (MediaControllerCompat.getMediaController(activity) != null) {
            MediaControllerCompat.getMediaController(activity).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    public void connectMediaBrowser() {
        mediaBrowser.connect();
    }


}
