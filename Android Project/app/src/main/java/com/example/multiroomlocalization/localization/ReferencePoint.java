package com.example.multiroomlocalization.localization;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.example.multiroomlocalization.speaker.Speaker;


public class ReferencePoint {
    private String id;
    private Speaker speaker;

    public ReferencePoint(String id) {
        this.id = id;
        this.speaker = null;
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