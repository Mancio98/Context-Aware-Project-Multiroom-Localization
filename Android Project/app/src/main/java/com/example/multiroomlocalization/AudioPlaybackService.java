package com.example.multiroomlocalization;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;

import android.os.Bundle;
import android.os.Handler;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.ExoPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioPlaybackService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private Context context;

    private MediaPlayer mediaPlayer;
    private ExoPlayer exoPlayer;

    private AudioAttributes attrs = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
    private AudioFocusRequest focusRequest;

    private String currentTrack = "";

    private int resumePosition;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean ongoingCall = false;
    private ArrayList<myAudioTrack> trackList;
    private final MediaSessionCompat.Callback myMediaSessionCallback = new MediaSessionCompat.Callback() {


        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId,extras);
            int result = requestAudioFocus();

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service


                if(mediaPlayer == null) {
                    Log.i("service","null");
                    currentTrack = mediaId;
                    Intent intent = new Intent(context, AudioPlaybackService.class);
                    startService(intent);
                }
                else{
                    Log.i("service","notnull");
                    if(mediaId.equals(currentTrack)){
                        resumeAudio();

                    }
                    else {
                        currentTrack = mediaId;

                        initMediaPlayer();
                    }
                }
                // Put the service in the foreground, post notification
                //service.startForeground(id, myPlayerNotification);
            }else {
                stopSelf();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
            pauseAudio();

            // Update metadata and state

            // Take the service out of the foreground, retain the notification
            //service.stopForeground(false);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }



        @Override
            public void onStop() {
                super.onStop();
                removeAudioFocus();

                // Set the session inactive  (and update metadata and state)
                mediaSession.setActive(false);
                // stop the player (custom call)
                stopAudio();
                // Stop the service
                stopSelf();
                // Take the service out of the foreground
                //stopForeground(false);
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                Log.i("porco","dio");
                seekToAudio(Math.toIntExact(pos));
            }

    };

    private int playerState = PlaybackState.STATE_NONE;
    private Handler handler = new Handler();

    private void updatePlaybackState() {
        long position = PlaybackState.PLAYBACK_POSITION_UNKNOWN;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
        }
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        stateBuilder.setState(playerState, position, 1.0f);
        mediaSession.setPlaybackState(PlaybackStateCompat.fromPlaybackState(stateBuilder.build()));
    }

    private void updateCurrentPosition() {
        if (mediaPlayer == null) {
            return;
        }
        else if(mediaPlayer.isPlaying()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                            .setActions(getAvailableActions())
                            .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition, 1)
                            .build();
                    mediaSession.setPlaybackState(playbackState);
                    updateCurrentPosition();
                }
            }, 1000);
        }
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID;

        if (playerState == PlaybackState.STATE_PLAYING)
            actions |= PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SEEK_TO;
        else
            actions |= PlaybackState.ACTION_PLAY | PlaybackState.ACTION_SEEK_TO;
        /*
        if (currentIndexOnQueue > 0) {
            actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        }
        if (currentIndexOnQueue < playingQueue.size() - 1) {
            actions |= PlaybackState.ACTION_SKIP_TO_NEXT;
        }*/
        return actions;
    }

    private void playAudio() {
        if (!mediaPlayer.isPlaying()) {
            requestAudioFocus();
            mediaPlayer.start();
            playerState = PlaybackState.STATE_PLAYING;
            updatePlaybackState();
            updateCurrentPosition();
        }
    }

    private void stopAudio() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();

        }
    }

    private void pauseAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

            resumePosition = mediaPlayer.getCurrentPosition();

            playerState = PlaybackState.STATE_PAUSED;
            updatePlaybackState();
        }
    }

    private void seekToAudio(int position){
        if(mediaPlayer != null){
            mediaPlayer.seekTo(position);
        }
    }

    private void resumeAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            playerState = PlaybackState.STATE_PLAYING;
            updatePlaybackState();
            updateCurrentPosition();
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // Create a MediaSessionCompat
        Log.i("service","già creato");
        callStateListener();
        mediaSession = new MediaSessionCompat(getApplicationContext(), LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        //mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        /*stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());*/
        updatePlaybackState();

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(myMediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopAudio();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!currentTrack.equals(""))
            initMediaPlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();


        mediaPlayer.setAudioAttributes(attrs);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(currentTrack);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        mediaPlayer.prepareAsync();

        mediaSession.setActive(true);
    }


    private int requestAudioFocus(){

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build();

        return am.requestAudioFocus(focusRequest);
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    private boolean removeAudioFocus() {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                am.abandonAudioFocusRequest(focusRequest);

    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        //Invoked when playback of a media source has completed.
        stopAudio();
        removeAudioFocus();
        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;

    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "Song Title")
            .putLong(MediaMetadata.METADATA_KEY_DURATION, mediaPlayer.getDuration())
                .build();
        mediaSession.setMetadata(mediaMetadata);
        playAudio();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseAudio();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeAudio();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {


        return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        result.sendResult(null);
    }
}