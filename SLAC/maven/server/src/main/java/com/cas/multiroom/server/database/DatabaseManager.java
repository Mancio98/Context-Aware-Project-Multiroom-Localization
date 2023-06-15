package com.cas.multiroom.server.database;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;
import com.cas.multiroom.server.messages.MessageSettings;
import com.cas.multiroom.server.messages.connection.MessageRegistrationSuccessful;
import com.cas.multiroom.server.messages.connection.MessageRegistrationUnsuccessful;
import com.cas.multiroom.server.messages.connection.MessageSuccessfulLogin;
import com.cas.multiroom.server.messages.connection.MessageUnsuccessfulLogin;
import com.cas.multiroom.server.speaker.Speaker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.ResultSet;

public class DatabaseManager {
	private final String url = "jdbc:sqlite:C:/Users/bocca/Desktop/sqlite/CASDB.db";
	
	/**
	 * Connect to the CASDB.db database
	 *
	 * @return the Connection object
	 */
    private synchronized Connection connect() {
        // SQLite connection string
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return conn;
    }
    
    
    
    public synchronized int selectUser(String username) {
    	int found = 0;
    	String sql_user = "SELECT * FROM USER WHERE USERNAME = ?";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql_user)) {
        	
        	pstmt.setString(1, username);
        	
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
            	System.out.println(rs.getString("username"));
            	found = 1;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            found = -1;
        }
        
        return found;
    }
    
    /**
     * Insert a new row into the USER table
     *
     * @param name
     * @param capacity
     */
    public synchronized Message insertUser(User user) {
    	Message message = null;
        String sql = "INSERT INTO USER(USERNAME, PASSWORD) VALUES(?, ?)";

        int found = this.selectUser(user.getUsername());
        
        if (found == 0) {
        	try (Connection conn = this.connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            	
            
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                
                pstmt.executeUpdate();
                
                message = new MessageRegistrationSuccessful();
                
                System.out.println("USER inserted: " + user.getUsername());
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
                message = new MessageRegistrationUnsuccessful(e.getMessage());
            }
        }
        else if (found == 1) {
        	message = new MessageRegistrationUnsuccessful("Username already choosen");
        }
        else {
        	message = new MessageRegistrationUnsuccessful("Error in database");
        }
        
        return message;
    }
    
    
    public synchronized void updateSettingsForMapUser(String idMap, String username, String path) {
    	String sql = "UPDATE MAP_USER "
    			+ "SET SETTINGS = ? "
    			+ "WHERE ID = ? AND USERNAME = ?";
    	
        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	pstmt.setString(1, path);
            pstmt.setInt(2, Integer.parseInt(idMap));
            pstmt.setString(3, username);
        	
            System.out.println(pstmt);
            
        	pstmt.executeUpdate();
        	
        	System.out.println("UPDATE DONE");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public synchronized void updateMap(String id, String datasetPath) {
    	String sql = "UPDATE MAP "
    			+ "SET DATASET = ?, PYTHON = ? "
    			+ "WHERE ID = ?";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	pstmt.setString(1, datasetPath);
        	pstmt.setString(2, datasetPath);
            pstmt.setInt(3, Integer.parseInt(id));
        	
        	pstmt.executeUpdate();
        	
        	System.out.println("UPDATE DONE");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Insert a new row into the MAP table
     *
     * @param name
     * @param capacity
     */
    public synchronized int insertMap(String key, File dir) {
    	int id_map = -1;
        String sql = "INSERT INTO MAP(KEY, IMAGE, DATASET, PYTHON) VALUES(?, ?, ?, ?)";

        /*PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);*/
        
        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        	
            pstmt.setString(1, key);
            pstmt.setString(2, dir.getAbsolutePath());
            pstmt.setString(3, null);
            pstmt.setString(4, null);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            else {
            	System.out.println("MAP INSERTED");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                	id_map = (int) generatedKeys.getLong(1);
                	System.out.println("MAP " + id_map);
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return id_map;
    }
    
    /**
     * Insert a new row into the MAP_USER table
     *
     * @param name
     * @param capacity
     */
    public synchronized void insertMapUser(int idMap, String username, String mapName, String settingsPath) {
        String sql = "INSERT INTO MAP_USER(ID, USERNAME, MAP_NAME, SETTINGS) VALUES(?, ?, ?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
            pstmt.setInt(1, idMap);
            pstmt.setString(2, username);
            pstmt.setString(3, mapName);
            pstmt.setString(4, settingsPath);
            
            pstmt.executeUpdate();
            
            System.out.println("MAP_USER INSERTED");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Insert a new row into the ROOM table
     *
     * @param name
     * @param capacity
     */
    public synchronized void insertReferencePoint(int id, ReferencePoint referencePoint) {
        String sql = "INSERT INTO REFERENCE_POINT(ID_MAP, X, Y, CSV, SPEAKER) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	pstmt.setInt(1, id);
        	pstmt.setInt(2, referencePoint.getX());
        	pstmt.setInt(3, referencePoint.getY());
            pstmt.setString(4, referencePoint.getPathCSV());
            pstmt.setString(5, ""); // referencePoint.getSpeaker().getMAC()
            
            pstmt.executeUpdate();
            
            System.out.println("FATTO! " + referencePoint.getId());
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public synchronized void updateImagePath(String idMap, String path) {
    	String sql = "UPDATE MAP "
    			+ "SET IMAGE = ? "
    			+ "WHERE ID = ?";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	pstmt.setString(1, path);
            pstmt.setInt(2, Integer.parseInt(idMap));
        	
        	pstmt.executeUpdate();
        	
        	System.out.println("UPDATE IMAGE DONE");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public synchronized boolean isMapReady(String idMap) {
    	boolean isReady = false;
    	
    	String sql = "SELECT DATASET, PYTHON "
            	+ "FROM MAP WHERE ID = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	System.out.println(rs.getString("python"));
            	if (rs.getString("python") != null && !rs.getString("python").equals("")) { // rs.getString("dataset").equals("") &&
            		System.out.println(rs.getString("python"));
            		isReady = true;
            	}
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	return isReady;
    }
    
    
    public synchronized void updateMapName(String mapName, String idMap, String username) {
    	String sql = "UPDATE MAP_USER "
    			+ "SET MAP_NAME = ?"
    			+ "WHERE ID = ? AND USERNAME = ?";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	pstmt.setString(1, mapName);
        	pstmt.setInt(2, Integer.parseInt(idMap));
            pstmt.setString(3, username);
        	
        	pstmt.executeUpdate();
        	
        	System.out.println("UPDATE MAP_NAME");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public synchronized String getMapName(String idMap, String username) {
    	String name = "";
    	
    	String sql = "SELECT MAP_NAME "
            	+ "FROM MAP_USER WHERE ID = ? AND USERNAME = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		pstmt.setString(2, username);
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	System.out.println(rs.getString("map_name"));
            	name = rs.getString("map_name");
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	return name;
    }
    
    
    public synchronized String getKeyMapFromId(String idMap) {
    	String key = "";
    	
    	String sql = "SELECT KEY "
            	+ "FROM MAP WHERE ID = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	System.out.println(rs.getString("key"));
            	key = rs.getString("key");
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	return key;
    }
    
    
    public synchronized String getImagePath(String idMap) {
    	String path = null;
    	
    	String sql = "SELECT IMAGE "
            	+ "FROM MAP WHERE ID = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	path = rs.getString("image");
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	return path;
    }
    
    
    public synchronized ArrayList<ReferencePoint> selectReferencePointWithIdMap(String idMap, String username) {
    	ArrayList<ReferencePoint> rps = new ArrayList<ReferencePoint>();
    	
    	String sql_rp = "SELECT * "
            	+ "FROM REFERENCE_POINT WHERE ID_MAP = ?";
    	
    	String sql_mu = "SELECT * "
            	+ "FROM MAP_USER WHERE ID = ? AND USERNAME = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql_rp)) {
    		
    		pstmt.setString(1, idMap);
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	String path = rs.getString("csv");
            	String id = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf(".csv"));
            	ReferencePoint rp = new ReferencePoint(id, rs.getInt("x"), rs.getInt("y"), path); // Integer.toString(rs.getInt("id"))
            	rps.add(rp);
            }
            
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql_mu)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		pstmt.setString(2, username);
    		
    		ResultSet rs = pstmt.executeQuery();
    		System.out.println("pre while");
    		// loop through the result set
            while (rs.next()) {
            	if (!rs.getString("settings").equals("")) {
            		//String mapName = rs.getString("map_name");
                	String settingsPath = rs.getString("settings");
                	
                	// create a reader
            	    Reader reader = new FileReader(settingsPath);
            	    
            	    //MessageSettings settings = new Gson().fromJson(reader, MessageSettings.class);
            	    
            	    //List<Object> list = Arrays.asList(new Gson().fromJson(reader, Object[].class));
            	    //System.out.println(list);
            	    ArrayList<Settings> settings = new Gson().fromJson(reader, new TypeToken<ArrayList<Settings>>(){}.getType()); // Settings[].class
            	    reader.close();
            	    //System.out.println(settingsPath.length());
            	    for (Settings s : settings) {
            	    	//System.out.println(s.getIdReferencePoint());
            	    	for (ReferencePoint r : rps) {
            	    		//System.out.println(r.getId());
            	    		if (s.getIdReferencePoint().equals(r.getId())) {
            	    			//System.out.println(s.getSpeaker());
            	    			r.setSpeaker(s.getSpeaker());
            	    			r.setDND(s.getDnd());
            	    		}
            	    	}
            	    }
            	}
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return rps;
    }
    
    
    public synchronized ReferencePoint selectReferencePoint(String idMap, String username, String id) {
    	ReferencePoint rp = null;
    	
    	String sql = "SELECT * "
            	+ "FROM REFERENCE_POINT WHERE ID_MAP = ? AND CSV = ?";
    	
    	String sql_mu = "SELECT * "
            	+ "FROM MAP_USER WHERE ID = ? AND USERNAME = ?";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
    	
    		String pathcsv = "C:/Users/bocca/Desktop/laurea_magistrale_informatica/SCA/progetto/Context-Aware-Project-Multiroom-Localization/SLAC/maven/server/" + idMap + "/" + id + ".csv";
    		pstmt.setString(1, idMap);
    		pstmt.setString(2, pathcsv);
    		
    		ResultSet rs = pstmt.executeQuery();
    		
    		// loop through the result set
            while (rs.next()) {
            	rp = new ReferencePoint(id, rs.getInt("x"), rs.getInt("y"), rs.getString("csv")); // Integer.toString(rs.getInt("id"))
            	rp.setSpeaker(new Speaker("", ""));
            }
            
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql_mu)) {
    		
    		pstmt.setInt(1, Integer.parseInt(idMap));
    		pstmt.setString(2, username);
    		
    		ResultSet rs = pstmt.executeQuery();
    		System.out.println("pre while");
    		// loop through the result set
            while (rs.next()) {
            	if (!rs.getString("settings").equals("")) {
            		//String mapName = rs.getString("map_name");
                	String settingsPath = rs.getString("settings");
                	
                	// create a reader
            	    Reader reader = new FileReader(settingsPath);
            	    
            	    //MessageSettings settings = new Gson().fromJson(reader, MessageSettings.class);
            	    
            	    //List<Object> list = Arrays.asList(new Gson().fromJson(reader, Object[].class));
            	    //System.out.println(list);
            	    ArrayList<Settings> settings = new Gson().fromJson(reader, new TypeToken<ArrayList<Settings>>(){}.getType()); // Settings[].class
            	    reader.close();
            	    //System.out.println(settingsPath.length());
            	    for (Settings s : settings) {
            	    	//System.out.println(s.getIdReferencePoint());
        	    		//System.out.println(r.getId());
        	    		if (s.getIdReferencePoint().equals(rp.getId())) {
        	    			//System.out.println(s.getSpeaker());
        	    			rp.setSpeaker(s.getSpeaker());
        	    			rp.setDND(s.getDnd());
        	    		}
            	    }
            	}
            }
    	}
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return rp;
    }
    
    
    /**
     * Get the USER PASSWORD whose USERNAME is equal to username
     * @param capacity 
     */
    public synchronized Message selectUser(User user){
    	//System.out.println("THREAD DENTRO, QUINDI BLOCCARE");
    	Message response = new MessageUnsuccessfulLogin();
    	
        String sql = "SELECT PASSWORD "
                	+ "FROM USER WHERE USERNAME = ?";
        
        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
               
        	System.out.println("CONNECTION ESTABLISHED");
            // set the value
            pstmt.setString(1, user.getUsername());
            
            //
            ResultSet rs = pstmt.executeQuery();
            // Only one USER with a unique USERNAME
            if (rs.next()) {
            	System.out.println(rs.getString("PASSWORD"));
            	if (rs.getString("PASSWORD").equals(user.getPassword())) {
            		System.out.println("Correct PASSWORD");
            		response = new MessageSuccessfulLogin(this.selectMapUser(user));
            	}
            }
            /*
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("PASSWORD"));
            }
            */
        }
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
        
        //System.out.println("THREAD FUORI, QUINDI SBLOCCARE");
        return response;
    }
    
    
    /**
     * select all ID, MAP_NAME where USERNAME is equal to username
     */
    public synchronized ArrayList<Map> selectMapUser(User user) {
    	String sql = "SELECT ID, MAP_NAME "
                + "FROM MAP_USER WHERE USERNAME = ?";
        
    	ArrayList<Map> mapList = new ArrayList<Map>();
    	
        try (Connection conn = this.connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
            // set the value
        	pstmt.setString(1, user.getUsername());
            //
            ResultSet rs  = pstmt.executeQuery();
           
            // loop through the result set
            while (rs.next()) {
            	String idMap = Integer.toString(rs.getInt("id"));
            	Map map = new Map(idMap, isMapReady(idMap));
            	map.setName(rs.getString("map_name"));
            	mapList.add(map);
                System.out.println(rs.getInt("id") + " " + rs.getString("map_name"));
            }
            
            System.out.println("LISTA MAPPE RITORNATA");
            
        }
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
        
        return mapList;
    }
    
    /**
     * select all ID, MAP_NAME where USERNAME is equal to username
     */
    public synchronized void selectMap() {
    	String sql = "SELECT ID "
                + "FROM MAP";
        
    	try (Connection conn = this.connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
    		
    		ResultSet rs  = pstmt.executeQuery();
            
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id"));
            }
        }
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
        
    }
}
