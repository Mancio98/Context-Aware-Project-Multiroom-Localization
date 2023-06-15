package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.database.Map;
import com.cas.multiroom.server.messages.Message;

public class MessageUpdateMapList extends Message {

    public static String type = "UPDATE_LIST_MAP";
    private Map map;

    public MessageUpdateMapList(Map map) {
        super(type);
        this.map = map;
    }

    public Map getMap() { return map; }

    public void setMap(Map map) { this.map = map; }
}
