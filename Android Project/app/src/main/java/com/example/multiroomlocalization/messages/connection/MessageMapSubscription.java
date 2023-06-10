package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageMapSubscription extends Message {

    public static String type = "MAP_SUBSCRIPTION";
    private String idMap;
    private String keyMap;

    public MessageMapSubscription(String idMap,String keyMap) {
        super(type);
        this.idMap = idMap;
        this.keyMap = keyMap;
    }

    public String getIdMap() { return idMap; }

    public String getKeyMap() { return keyMap; }

    public void setIdMap(String idMap) { this.idMap = idMap; }

    public void setKeyMap(String keyMap) { this.keyMap = keyMap; }
}
