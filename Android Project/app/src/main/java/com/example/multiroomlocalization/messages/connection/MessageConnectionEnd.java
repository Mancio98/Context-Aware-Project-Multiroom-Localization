package com.example.multiroomlocalization.messages.connection;

<<<<<<< HEAD

import com.example.multiroomlocalization.messages.Message;


public class MessageConnectionEnd extends Message {
=======
import com.example.multiroomlocalization.messages.Message;

public class MessageConnectionEnd  extends Message {
>>>>>>> origin/luca-branch
    private String id;
    private String name;

    public MessageConnectionEnd(String id, String name) {
        super("CONNECTION_END");
        this.id = id;
        this.name = name;
    }
<<<<<<< HEAD
    
=======

>>>>>>> origin/luca-branch
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
