package com.cas.multiroom.server.messages.connection;

import java.util.ArrayList;

import com.cas.multiroom.server.database.Map;
import com.cas.multiroom.server.messages.Message;

public class MessageSuccessfulLogin extends Message {
	
	public ArrayList<Map> mapList;
	
    public MessageSuccessfulLogin(ArrayList<Map> mapList) {
        super("SUCCESSFUL_LOGIN");
        this.mapList = mapList;
    }
    
    public ArrayList<Map> getMapList() {
        return this.mapList;
    }

    public void setMapList(ArrayList<Map> mapList) {
        this.mapList = mapList;
    }
}