package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.Settings;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

public class MessageEndMappingPhase extends Message {

    private String key;
    private String mapName;
    private ArrayList<Settings> arrSettings;

    public MessageEndMappingPhase(String key,ArrayList<Settings> arrSettings,String mapName) {
        super("END_MAPPING_PHASE");
        this.key=key;
        this.arrSettings = arrSettings;
        this.mapName = mapName;
    }

    public String getKey(){ return key; }

    public void setKey(String key){ this.key = key; }

    public void setArrSettings(ArrayList<Settings> arrSettings) { this.arrSettings = arrSettings; }

    public ArrayList<Settings> getArrSettings() { return arrSettings; }

    public String getMapName() { return mapName; }

    public void setMapName(String mapName) { this.mapName = mapName; }
}
