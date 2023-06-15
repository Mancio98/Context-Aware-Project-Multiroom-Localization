package com.example.multiroomlocalization.Music;

import static com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MUSIC;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.example.multiroomlocalization.messages.music.MessagePlaylist;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AudioPlaybackService extends MediaBrowserServiceCompat implements Player.Listener{

    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mediaSession;
    private Context context;
    public static boolean isMyServiceRunning = false;

    private ExoPlayer exoPlayer;

    private final com.google.android.exoplayer2.audio.AudioAttributes attrs2 = new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            .build();

    public static int currentTrack = 0;

    private List<MyAudioTrack> trackList;

    private int playerState = PlaybackState.STATE_NONE;
    private final Handler handler = new Handler();
    private final Gson gson = new Gson();
    private final MediaSessionCompat.Callback myMediaSessionCallback = new MediaSessionCompat.Callback() {


        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId,extras);
                int mediaIndex = Integer.parseInt(mediaId);

                if(mediaIndex == currentTrack)
                    playPauseAudio();
                else {
                    currentTrack = mediaIndex;
                    seekToAudio();
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


    private void pauseAudio() {
        if(exoPlayer.isPlaying())
            exoPlayer.pause();
    }

    private void seekToAudio(){
        if(exoPlayer != null){
            pauseAudio();
            exoPlayer.seekTo(currentTrack,0);
            resumeAudio();
        }
    }

    private void resumeAudio() {
        if (!exoPlayer.isPlaying()) {
            System.out.println("now play!");
            exoPlayer.play();
        }

    }

    private void playPauseAudio(){
        if(exoPlayer.isPlaying())
            pauseAudio();
        else
            resumeAudio();
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

        currentTrack = exoPlayer.getCurrentMediaItemIndex();
        MediaMetadataCompat mediaMetadata = null;
        if (mediaItem != null) {
            mediaMetadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, trackList.get(currentTrack).getTitle())
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, trackList.get(currentTrack).getAuthor())
                    .build();
        }
        mediaSession.setMetadata(mediaMetadata);
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        error.printStackTrace();
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        error.printStackTrace();
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



        mediaSession = new MediaSessionCompat(context, "LOG_TAG");

        // MySessionCallback has methods that handle callbacks from a media controller
        mediaSession.setCallback(myMediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

        mediaSession.setActive(true);

        updatePlaybackState();


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


        stopForeground(true);
        stopSelf();
        isMyServiceRunning = false;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String myPlaylist = intent.getStringExtra("playlist");

        if(myPlaylist != null)
            trackList = gson.fromJson(myPlaylist, MessagePlaylist.class).getSong();
        initExoPlayer();
        isMyServiceRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }



    private void initExoPlayer() {


        exoPlayer = new ExoPlayer.Builder(context).build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
            }
        });
        for (MyAudioTrack track : trackList) {

            MediaItem firstItem = MediaItem.fromUri(track.getPath());
            // Add the media items to be played.
            exoPlayer.addMediaItem(firstItem);

        }

        exoPlayer.setAudioAttributes(attrs2, true);

        exoPlayer.addListener(this);
        exoPlayer.prepare();
        exoPlayer.seekTo(0,0);

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
        result.detach();
        result.sendResult(children);

    }
}