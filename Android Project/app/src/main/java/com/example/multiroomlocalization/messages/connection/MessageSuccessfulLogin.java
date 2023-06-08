package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.Map;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

public class MessageSuccessfulLogin extends Message {
    public static String type = "SUCCESSFUL_LOGIN";
    public ArrayList<Map> mapList;
    public MessageSuccessfulLogin(ArrayList<Map> mapList) {
        super(type);
        this.mapList = mapList;
    }

    public ArrayList<Map> getMapList() {
        return mapList;
    }

    public void setMapList(ArrayList<Map> mapList) {
        this.mapList = mapList;
    }

}