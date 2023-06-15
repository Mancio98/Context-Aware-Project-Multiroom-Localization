package com.cas.multiroom.server.messages.connection;

import com.cas.multiroom.server.messages.Message;

public class MessageMapRequest extends Message {

    public static String type = "MAP_REQUEST";
    public String idMap;

    public MessageMapRequest(String idMap) {
        super(type);
        this.idMap = idMap;
    }

    public void setIdMap(String idMap) { this.idMap = idMap; }

    public String getIdMap() { return idMap; }
}
