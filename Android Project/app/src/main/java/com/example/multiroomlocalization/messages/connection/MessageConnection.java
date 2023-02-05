package com.example.multiroomlocalization.messages.connection;

<<<<<<< HEAD

import com.example.multiroomlocalization.messages.Message;


=======
import com.example.multiroomlocalization.messages.Message;

>>>>>>> origin/luca-branch
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
<<<<<<< HEAD
    
=======

>>>>>>> origin/luca-branch
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/luca-branch
