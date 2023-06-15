package com.example.multiroomlocalization.Music;

import android.content.Context;
import android.content.Intent;
import android.widget.MediaController;

public class MyAudioController extends MediaController implements MediaController.MediaPlayerControl {

    private final Context myContext;

    public MyAudioController(Context context) {
        super(context);
        myContext = context;
        setMediaPlayer(this);
    }

    public static final String Broadcast_PLAY_AUDIO ="com.example.multiroomlocalization.myAudioController.PlayAudio";
    @Override
    public void start() {

        Intent broadcastIntent = new Intent(Broadcast_PLAY_AUDIO);
        myContext.sendBroadcast(broadcastIntent);
    }

    public static final String Broadcast_PAUSE_AUDIO = "com.example.multiroomlocalization.myAudioController.PauseAudio";
    @Override
    public void pause() {
        Intent broadcastIntent = new Intent(Broadcast_PAUSE_AUDIO);
        myContext.sendBroadcast(broadcastIntent);
    }

    @Override
    public int getDuration() {
        
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    public static final String Broadcast_SEEKTO_AUDIO = "com.example.multiroomlocalization.myAudioController.SeekToAudio";
    @Override
    public void seekTo(int i) {
        Intent broadcastIntent = new Intent(Broadcast_SEEKTO_AUDIO);
        broadcastIntent.putExtra("pos",i);
        myContext.sendBroadcast(broadcastIntent);
    }


    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
