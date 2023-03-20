package com.example.multiroomlocalization.messages.localization;


import com.example.multiroomlocalization.messages.Message;


public class MessageScanRoom extends Message {
<<<<<<< HEAD
	private String roomId;
	
=======
    private String roomId;

>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
    public MessageScanRoom(String roomId) {
        super("SCAN_ROOM");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return this.roomId;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
