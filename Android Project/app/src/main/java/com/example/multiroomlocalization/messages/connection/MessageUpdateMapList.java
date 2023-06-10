package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.Map;
import com.example.multiroomlocalization.messages.Message;

public class MessageUpdateMapList extends Message {

    public static String type = "UPDATE_LIST_MAP";
    Map map;

    public MessageUpdateMapList(Map map) {
        super(type);
        this.map = map;
    }

    public Map getMap() { return map; }

    public void setMap(Map map) { this.map = map; }
}
