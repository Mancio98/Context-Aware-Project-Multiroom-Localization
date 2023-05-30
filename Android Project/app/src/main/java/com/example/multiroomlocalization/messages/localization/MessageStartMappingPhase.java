package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageStartMappingPhase  extends Message {
    int len;
    public static String type = "START_MAPPING_PHASE";
    public MessageStartMappingPhase(int len) {
        super(type);
        this.len=len;
    }
}
