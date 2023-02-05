package com.example.multiroomlocalization.messages;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public abstract class Message implements Serializable {
<<<<<<< HEAD
	private static final long serialVersionUID = 1L;

	protected String type;
	
	
	public Message(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

    public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		return baos.toByteArray();
	}

	public static Message fromByteArray(byte[] array) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return (Message) ois.readObject();
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public String toJson(Gson serializer) {
        return serializer.toJson(this);
    }
}
=======
    private static final long serialVersionUID = 1L;

    protected String type;

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        return baos.toByteArray();
    }

    public static Message fromByteArray(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Message) ois.readObject();
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toJson(Gson serializer) {
        return serializer.toJson(this);
    }
}
>>>>>>> ac03508371086e2bae36ec6c0e1a3ba394c9c5cd
