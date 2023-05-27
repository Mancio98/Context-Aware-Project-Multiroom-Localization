package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageStartMappingPhase  extends Message {
    int len;

    public MessageStartMappingPhase(int len) {
        super("START_MAPPING_PHASE");
        this.len=len;
    }
}
