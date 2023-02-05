package server.messages.connection;


import server.messages.Message;


public class MessageConnection extends Message {
    private String id;
    private String name;

    public MessageConnection(String id, String name) {
        super("CONNECTION");
        this.id = id;
        this.name = name;
    }

    public MessageConnection(String id) {
        super("CONNECTION");
        this.id = id;
        this.name = null;
    }
    
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}