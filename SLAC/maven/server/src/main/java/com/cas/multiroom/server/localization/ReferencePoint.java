package com.cas.multiroom.server.localization;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.cas.multiroom.server.speaker.Speaker;


public class ReferencePoint {
    private String id;
    private Speaker speaker;
    private int x;
    private int y;
    private String pathCSV;
    private boolean dnd;
    

    public ReferencePoint(String id, int x, int y, String pathCSV) {
        this.id = id;
        this.speaker = null;
        this.x = x;
        this.y = y;
        this.pathCSV = pathCSV;
        this.dnd = false;
    }

    public boolean getDND() {
    	return this.dnd;
    }
    
    public void setDND(boolean dnd) {
    	this.dnd = dnd;
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
    
    public int getX() {
        return this.x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public String getPathCSV() {
    	return this.pathCSV;
    }
    
    public void setPathCSV(String pathCSV) {
    	this.pathCSV = pathCSV;
    }
}