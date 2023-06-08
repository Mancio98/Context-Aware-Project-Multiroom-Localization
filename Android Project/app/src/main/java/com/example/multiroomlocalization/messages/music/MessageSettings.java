package com.example.multiroomlocalization.messages.music;

import com.example.multiroomlocalization.Settings;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

public class MessageSettings extends Message {

    public static String type = "SETTINGS";
    private String idMap;
    private String mapName;
    private ArrayList<Settings> arrSettings;

    public MessageSettings(ArrayList<Settings> arrSettings,String idMap,String mapName) {
        super(type);
        this.arrSettings = arrSettings;
        this.idMap = idMap;
        this.mapName = mapName;
    }

    public ArrayList<Settings> getArrSettings() { return arrSettings; }

    public String getIdMap() { return idMap; }

    public void setIdMap(String idMap) { this.idMap = idMap; }

    public void setArrSettings(ArrayList<Settings> arrSettings) { this.arrSettings = arrSettings; }

    public String getMapName() { return mapName; }

    public void setMapName(String mapName) { this.mapName = mapName; }
}
