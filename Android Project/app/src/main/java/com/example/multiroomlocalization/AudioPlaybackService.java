package com.example.multiroomlocalization;

import static com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MUSIC;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.media.AudioFocusRequest;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

public class AudioPlaybackService extends MediaBrowserServiceCompat implements Player.Listener{

    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private Context context;

    private MediaPlayer mediaPlayer;
    private ExoPlayer exoPlayer;

    private final AudioAttributes attrs = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
    private final com.google.android.exoplayer2.audio.AudioAttributes attrs2 = new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            .build();
    private AudioFocusRequest focusRequest;

    private int currentTrack = 0;

    private int resumePosition;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean ongoingCall = false;

    private final List<myAudioTrack> trackList = Arrays.asList(new myAudioTrack("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg",
            "ah non lo so io", "lucacotu", null),
            new myAudioTrack("https://upload.wikimedia.org/wikipedia/commons/e/e3/Columbia-d14531-bx538.ogg", "urbania", "bonajunior", null),
            new myAudioTrack("https://58e7-79-55-37-219.ngrok-free.app/Will_Clarke_Rock_with_me.mp3 ","asdkadn","boh",null));

    private int playerState = PlaybackState.STATE_NONE;
    private final Handler handler = new Handler();

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
    private MediaSessionConnector mediaSessionConnector;

    private void pauseAudio() {
        if(exoPlayer.isPlaying()) {

            exoPlayer.pause();

        }
    }

    private void seekToAudio(){
        if(exoPlayer != null){
            exoPlayer.seekTo(currentTrack,0);
            resumeAudio();
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
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        Player.Listener.super.onMediaItemTransition(mediaItem, reason);


        MediaMetadataCompat mediaMetadata = null;
        if (mediaItem != null) {
            mediaMetadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, trackList.get(currentTrack).getTitle())
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, trackList.get(currentTrack).getAuthor())
                    .build();
        }
        mediaSession.setMetadata(mediaMetadata);
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
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        callStateListener();
        mediaSession = new MediaSessionCompat(context, "LOG_TAG");



        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(myMediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

        mediaSession.setActive(true);

        updatePlaybackState();
        //downloadAudioTracks();
    }

    private void downloadAudioTracks() {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.setActive(false);
        if(exoPlayer != null){
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        stopForeground(true);
        stopSelf();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //MediaButtonReceiver.handleIntent(mediaSession, intent);
        //Notification notification = buildNotification();
        ///startForeground(1,notification);
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
        exoPlayer = new ExoPlayer.Builder(context).build();
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
        exoPlayer.seekTo(currentTrack,0);
        exoPlayer.play();

    }


/*
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
*/

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

    private List<MediaBrowserCompat.MediaItem> buildListMediaItem(){

        List<MediaBrowserCompat.MediaItem> list = new ArrayList<>();
        // Add media items to the children list
        for(int i=0; i< trackList.size(); i++) {
            list.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setTitle(trackList.get(i).getTitle())
                            .setMediaUri(Uri.parse(trackList.get(i).getPath()))
                            .setMediaId("media_item_"+i)
                            .build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return list;
    }
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        List<MediaBrowserCompat.MediaItem> children = buildListMediaItem();

        result.sendResult(children);

    }
}