package server.messages.localization;


import server.messages.Message;


public class MessageScanRoom extends Message {
	private String roomId;
	
    public MessageScanRoom(String roomId) {
        super("SCAN_ROOM");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return this.roomId;
    }
}
