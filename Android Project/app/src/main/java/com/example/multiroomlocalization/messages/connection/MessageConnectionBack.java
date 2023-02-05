package com.example.multiroomlocalization.messages.connection;

<<<<<<< HEAD

import com.example.multiroomlocalization.messages.Message;


public class MessageConnectionBack extends Message {
    private String id;
    private boolean accepted;
    
=======
import com.example.multiroomlocalization.messages.Message;

public class MessageConnectionBack  extends Message {
    private String id;
    private boolean accepted;

>>>>>>> origin/luca-branch
    public MessageConnectionBack(String id, boolean accepted) {
        super("CONNECTION_BACK");
        this.id = id;
        this.accepted = accepted;
    }
<<<<<<< HEAD
    
=======

>>>>>>> origin/luca-branch
    public String getId() {
        return this.id;
    }

    public boolean getName() {
        return this.accepted;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/luca-branch
