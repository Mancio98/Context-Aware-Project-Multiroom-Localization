package com.example.multiroomlocalization.localization;

import com.example.multiroomlocalization.speaker.Speaker;

public class ReferencePoint{

    private String id;
    private Speaker speaker;
    private Boolean dnd;
    private int x;
    private int y;
    private String path;


    public ReferencePoint(int x,int y, String label){
        this.x = x;
        this.y = y;
        this.id = label;
        this.speaker = null;
        this.path = null;
        this.dnd=false;

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getY() { return y; }

    public String getId() { return id; }

    public int getX() { return x; }

    public void setY(int y) { this.y = y; }

    public void setId(String label) { this.id = label; }

    public void setX(int x) { this.x = x; }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return this.speaker;
    }

    public Boolean getDnd() { return dnd; }

    public void setDnd(Boolean dnd) { this.dnd = dnd; }

}
