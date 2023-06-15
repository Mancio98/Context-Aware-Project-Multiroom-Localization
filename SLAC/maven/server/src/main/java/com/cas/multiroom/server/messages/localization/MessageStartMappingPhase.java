package com.cas.multiroom.server.messages.localization;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageStartMappingPhase extends Message {

	public int len;
	
	public MessageStartMappingPhase(int len) {
		super("START_MAPPING_PHASE");
		this.len = len;
	}
	
	public int getLen() {
		return this.len;
	}
	
	public void setLen(int len) {
		this.len = len;
	}
}
