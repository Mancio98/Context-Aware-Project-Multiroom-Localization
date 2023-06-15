package com.cas.multiroom.server.messages.localization;

import java.util.ArrayList;

import com.cas.multiroom.server.database.Settings;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageEndMappingPhase extends Message {

	public String key;
	private ArrayList<Settings> arrSettings;
	private String mapName;
	
	public MessageEndMappingPhase(String key, ArrayList<Settings> arrSettings, String mapName) {
		super("END_MAPPING_PHASE");
		this.key = key;
		this.arrSettings = arrSettings;
		this.mapName = mapName;
	}
	
	public String getMapName() {
		return this.mapName;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public ArrayList<Settings> getArrSettings() { return arrSettings; }
}
