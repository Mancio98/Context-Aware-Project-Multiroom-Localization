package com.cas.multiroom.server.database;

public class Map {
    private String id;
    private boolean isReady;
    private String name;

    public Map(String id, boolean isReady) {
        this.id = id;
        this.isReady = isReady;
        this.name = "";
    }

    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsReady() {
        return this.isReady;
    }
    
    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }
}

