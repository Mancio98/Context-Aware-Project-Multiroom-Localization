package com.example.multiroomlocalization.messages.speaker;

import android.util.ArraySet;

import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

public class MessageListSpeaker extends Message {

    private ArraySet<Speaker> speaker;

    public MessageListSpeaker(ArraySet<Speaker> listSpeaker) {
        super("LIST_SPEAKER");
        this.speaker = listSpeaker;
    }

    public void setSpeaker(ArraySet<Speaker> listSpeaker) {
        this.speaker = listSpeaker;
    }

    public ArraySet<Speaker> getSpeaker() {
        return speaker;
    }
}
