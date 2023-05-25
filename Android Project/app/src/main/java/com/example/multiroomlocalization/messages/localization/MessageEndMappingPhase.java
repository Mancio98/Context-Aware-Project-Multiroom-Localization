package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.messages.Message;

public class MessageEndMappingPhase extends Message {

    String password;
    public MessageEndMappingPhase(String pass) {
        super("END_MAPPING_PHASE");
        password= pass;
    }

}
