package com.example.multiroomlocalization.localization;


import com.example.multiroomlocalization.speaker.Speaker;


public class ReferencePoint {
    private String id;
    private Speaker speaker;

    public ReferencePoint(String id, Speaker speaker) {
        this.id = id;
        this.speaker = speaker;
    }

    public String getId() {
        return this.id;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }
    
    public Speaker getSpeaker() {
        return this.speaker;
    }
}