package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.messages.Message;

public class MessageMappingPhaseFinish extends Message {
    public static String type = "MAPPING_PHASE_FINISH";

    public MessageMappingPhaseFinish() {
        super(type);
    }
}
