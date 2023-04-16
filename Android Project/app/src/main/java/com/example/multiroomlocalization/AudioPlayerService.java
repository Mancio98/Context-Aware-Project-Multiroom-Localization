package com.example.multiroomlocalization;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import static com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MUSIC;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AudioPlayerService  extends Service implements Player.Listener{

    private int currentTrack;
    private final MediaSessionCompat.Callback myMediaSessionCallback = new MediaSessionCompat.Callback() {

        //private final MediaSessionConnector.PlaybackPreparer myMediaSessionCallback = new MediaSessionConnector.PlaybackPreparer() {


        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId,extras);
            int mediaIndex = Integer.parseInt(mediaId);
            if(exoPlayer == null) {

                currentTrack = mediaIndex;
                initExoPlayer();
                Intent intent = new Intent(context, AudioPlaybackService.class);
                startService(intent);


            }
            else{

                if(mediaIndex == currentTrack){
                    resumeAudio();
                }
                else {
                    currentTrack = mediaIndex;
                    seekToAudio();
                }
            }

        }

        @Override
        public void onPlay() {
            super.onPlay();
            resumeAudio();
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseAudio();

        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if(exoPlayer != null){
                exoPlayer.seekToNext();

                if(!exoPlayer.isPlaying())
                    exoPlayer.play();
            }
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if(exoPlayer != null) {

                exoPlayer.seekToPrevious();
                if(!exoPlayer.isPlaying())
                    exoPlayer.play();
            }
        }

        @Override
        public void onStop() {
            super.onStop();

            exoPlayer.stop();


        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);

            exoPlayer.seekTo(pos);
        }

    };
    private MediaPlayer mediaPlayer;
    //path to the audio file
    private String URL;
    //Used to pause/resume MediaPlayer
    private int resumePosition;
    private final com.google.android.exoplayer2.audio.AudioAttributes attrs2 = new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            .build();

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();
    private final List<myAudioTrack> trackList = Arrays.asList(new myAudioTrack("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg",
                    "ah non lo so io", "lucacotu", null),
            new myAudioTrack("https://upload.wikimedia.org/wikipedia/commons/e/e3/Columbia-d14531-bx538.ogg", "urbania", "bonajunior", null));
    private Context context;
    private MediaSessionCompat mediaSession;
    private ExoPlayer exoPlayer;
    private int playerState = PlaybackState.STATE_NONE;
    private Handler handler = new Handler();



    private void pauseAudio() {
        if(exoPlayer.isPlaying()) {

            exoPlayer.pause();
            
        }
    }

    private void seekToAudio(){
        if(exoPlayer != null){
            exoPlayer.seekTo(currentTrack,0);
            if(!exoPlayer.isPlaying())
                exoPlayer.play();
        }
    }

    private void resumeAudio() {
        if (!exoPlayer.isPlaying())
            exoPlayer.play();

    }


    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Player.Listener.super.onIsPlayingChanged(isPlaying);

        if(isPlaying) {
            playerState = PlaybackState.STATE_PLAYING;
            MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, exoPlayer.getDuration())
                    .build();
            mediaSession.setMetadata(mediaMetadata);
            updateCurrentPosition();
        }
        else
            playerState = PlaybackState.STATE_PAUSED;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        callStateListener();
        mediaSession = new MediaSessionCompat(context, "LOG_TAG");

        updatePlaybackState();

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(myMediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        //setSessionToken(mediaSession.getSessionToken());

        mediaSession.setActive(true);

        exoPlayer = new ExoPlayer.Builder(context).build();
        // Resume on hangup.
        callStateListener();
        //Listen for new Audio to play -- BroadcastReceiver

    }


    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    private void updatePlaybackState() {
        long position = PlaybackState.PLAYBACK_POSITION_UNKNOWN;
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            position = exoPlayer.getCurrentPosition();
        }
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        stateBuilder.setState(playerState, position, 1.0f);
        mediaSession.setPlaybackState(PlaybackStateCompat.fromPlaybackState(stateBuilder.build()));
    }
    private void updateCurrentPosition() {

        if(exoPlayer.isPlaying()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updatePlaybackState();
                    updateCurrentPosition();
                }
            }, 1000);
        }
        else{
            updatePlaybackState();
        }
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID;

        if (playerState == PlaybackState.STATE_PLAYING)
            actions |= PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SEEK_TO;
        else
            actions |= PlaybackState.ACTION_PLAY | PlaybackState.ACTION_SEEK_TO;


        actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SKIP_TO_NEXT;


        return actions;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Notification notification = buildNotification();
        ///startForeground(1,notification);
        System.out.println("cominciato!");
        initExoPlayer();
        startExoPlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification(){

        MediaControllerCompat controller = mediaSession.getController();

        //MediaMetadataCompat mediaMetadata = controller.getMetadata();
        //MediaDescriptionCompat description = mediaMetadata.getDescription();

        String NOTIFICATION_CHANNEL_ID = "multiroomlocalization";
        String channelName = "Music Playback";

       /* NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) (getSystemService(Context.NOTIFICATION_SERVICE));
        manager.createNotificationChannel(chan);*/
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder
                // Add the metadata for the currently playing track
                .setContentTitle("Title")
                .setContentText("Subtitle")
                .setSubText("Description")
                //.setLargeIcon("description.getIconBitmap()")
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setOngoing(true)
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                // Add a pause button
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_media_pause, "pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)));

        return builder.build();

    }

    private void initExoPlayer() {

        // Build the media items.

        for (myAudioTrack track : trackList) {

            MediaItem firstItem = MediaItem.fromUri(track.getPath());
            // Add the media items to be played.
            exoPlayer.addMediaItem(firstItem);

        }

        exoPlayer.setAudioAttributes(attrs2, true);

        exoPlayer.addListener(this);
        //mediaSessionConnector = new MediaSessionConnector(mediaSession);
        //mediaSessionConnector.setPlayer(exoPlayer);
    }

    private void startExoPlayer(){
        exoPlayer.prepare();
        //exoPlayer.seekTo(currentTrack,0);
        //exoPlayer.play();

    }





    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(exoPlayer != null){
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }


}