package com.cas.multiroom.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {
	
<<<<<<< HEAD
	private final String url = "jdbc:sqlite:C:/Users/bocca/Desktop/sqlite/CASDB.db";
=======
	private final String url = "jdbc:sqlite:/Users/luca/Library/Mobile Documents/com~apple~CloudDocs/Università/Magistrale/Primo Anno/Sistemi Context-Aware/sqlite-tools-osx-x86-3410000/CASDB.db";
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
	
	/**
	 * Connect to the CASDB.db database
	 *
	 * @return the Connection object
	 */
    private Connection connect() {
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
    
    
    /**
     * Insert a new row into the USER table
     *
     * @param name
     * @param capacity
     */
<<<<<<< HEAD
    public void insertUser() {
=======
    public void insertUser(User user) {
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
        String sql = "INSERT INTO USER(USERNAME, PASSWORD) VALUES(?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
<<<<<<< HEAD
        	/*
            pstmt.setString(1, );
            pstmt.setString(2, );
            */
=======
        	
            pstmt.setString(1, user.getUsername() );
            pstmt.setString(2, user.getPassword() );
            
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        System.out.println("USER inserted: ");
    }
    
    /**
     * Insert a new row into the MAP table
     *
     * @param name
     * @param capacity
     */
    public void insertMap() {
        String sql = "INSERT INTO MAP(,) VALUES(?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	/*
            pstmt.setString(1, );
            pstmt.setDouble(2, );
            */
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Insert a new row into the MAP_USER table
     *
     * @param name
     * @param capacity
     */
    public void insertMapUser() {
        String sql = "INSERT INTO MAP_USER(,) VALUES(?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	/*
            pstmt.setString(1, );
            pstmt.setDouble(2, );
            */
            pstmt.executeUpdate();
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
    public void insertRoom() {
        String sql = "INSERT INTO ROOM(,) VALUES(?, ?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	/*
            pstmt.setString(1, );
            pstmt.setDouble(2, );
            */
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Get the USER PASSWORD whose USERNAME is equal to username
     * @param capacity 
     */
<<<<<<< HEAD
    public void selectUser(){
=======
    public void selectUser(User user){
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
        String sql = "SELECT PASSWORD "
                	+ "FROM USER WHERE USERNAME = ?";
        
        try (Connection conn = this.connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
            // set the value
<<<<<<< HEAD
        	/*
            pstmt.setString(1, );
            */
=======
            pstmt.setString(1,user.getUsername());
            
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
            //
            ResultSet rs  = pstmt.executeQuery();
            // Only one USER with a unique USERNAME
            if (rs.next()) {
<<<<<<< HEAD
            	if (rs.getString("PASSWORD").equals()) {
=======
            	if (rs.getString("PASSWORD").equals(user.getPassword())) {
>>>>>>> b9169371c0b82af5d25afc787f9ec486cabdd2da
            		System.out.println("Correct PASSWORD");
            	}
            	else {
            		System.out.println("Incorrect PASSWORD");
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
    }
    
    
    /**
     * select all ID, MAP_NAME where USERNAME is equal to username
     */
    public void selectMapUser(){
    	String sql = "SELECT ID, MAP_NAME "
                + "FROM MAP_USER WHERE USERNAME = ?";
        
        try (Connection conn = this.connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
            // set the value
        	/*
            pstmt.setString(1, );
            */
            //
            ResultSet rs  = pstmt.executeQuery();
           
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" + 
                                	rs.getString("name") + "\t" +
                                	rs.getDouble("capacity"));
            }
        }
        catch (SQLException e) {
           System.out.println(e.getMessage());
        }
    }
}
