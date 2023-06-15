package com.cas.multiroom.server.messages;


import java.util.ArrayList;
import com.cas.multiroom.server.messages.Message;

import com.cas.multiroom.server.database.Settings;

public class MessageSettings extends Message {

    public static String type = "SETTINGS";
    private ArrayList<Settings> arrSettings;
    private String idMap;
    private String mapName;

    public MessageSettings(ArrayList<Settings> arrSettings, String idMap) {
        super(type);
        this.arrSettings = arrSettings;
        this.idMap = idMap;
        this.mapName = "";
    }
    
    public String getMapName() {
    	return this.mapName;
    }
    
    public void setMapName(String mapName) {
    	this.mapName = mapName;
    }

    public ArrayList<Settings> getArrSettings() { return arrSettings; }
    
    public String getIdMap() {
    	return this.idMap;
    }
}
