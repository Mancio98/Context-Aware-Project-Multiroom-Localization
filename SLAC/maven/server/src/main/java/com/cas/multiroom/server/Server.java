package com.cas.multiroom.server;


import com.google.gson.Gson;

import com.cas.multiroom.server.database.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	private final static int SERVER_PORT = 8777;
	
	private static SocketHandler socketHandler;
	private static boolean stopped;
	
	
	public Server() {
		DatabaseManager dbm = new DatabaseManager();
		System.out.println("entrato");
		
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while(!stopped) {
                Socket clientSocket = serverSocket.accept();
                socketHandler = new SocketHandler(clientSocket, dbm);//, speakerManger);
                socketHandler.start();
            }
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
