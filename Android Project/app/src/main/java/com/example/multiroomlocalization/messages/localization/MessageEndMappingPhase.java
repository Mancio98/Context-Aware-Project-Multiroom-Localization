package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageEndMappingPhase extends Message {

    public String key;
    public MessageEndMappingPhase(String key) {
        super("END_MAPPING_PHASE");
        this.key=key;
    }

    public String getKey(){ return key; }

    public void setKey(String key){ this.key = key; }
}
