package com.cas.multiroom.server.database;

import com.cas.multiroom.server.speaker.Speaker;

public class Settings {
    private String idReferencePoint;
    private Speaker speaker;
    private Boolean dnd;
    

    public Settings(String idReferencePoint,Speaker speaker,Boolean dnd) {
        this.idReferencePoint = idReferencePoint;
        this.speaker = speaker;
        this.dnd = dnd;
    }
    
    public Boolean getDnd() { return dnd; }

    public String getIdReferencePoint() { return idReferencePoint; }

    public Speaker getSpeaker() {return speaker; }

    public void setDnd(Boolean dnd) { this.dnd = dnd; }

    public void setIdReferencePoint(String idReferencePoint) { this.idReferencePoint = idReferencePoint; }

    public void setMacSpeaker(Speaker speaker) { this.speaker = speaker; }
}
