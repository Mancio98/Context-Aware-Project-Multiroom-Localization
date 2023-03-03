package com.cas.multiroom.server.localization;

import java.io.Serializable;

public class ReferencePointMap implements Serializable {
	private Integer[] scans;
    private String name;
    
    public ReferencePointMap() {  }
    
    public ReferencePointMap(int c) {
    	this.scans = new Integer[c];
    }
  
    public String getName() {
      return name;
    }
  
    public void setName(String name) {
      this.name = name;
    }
  
    public Integer[] getScans() {
      return scans;
    }
  
    public Integer getScans(int index) {
      return scans[index];
    }
  
    public void setScans(Integer[] scans) {
      this.scans = scans;
    }
    
    public void setScans(int index, Integer level) {
      this.scans[index] = level;
    }
    
    public String[] toCSV() {
    	String[] row = new String[this.scans.length + 1];
    	for (int i = 0; i < this.scans.length; i++) {
    		row[i] = this.scans[i].toString();
    	}
    	row[this.scans.length] = this.name;
    	return row;
    }
  }