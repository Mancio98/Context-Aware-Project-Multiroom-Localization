package server;


import com.google.gson.Gson;

import server.database.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
<<<<<<< HEAD
	private final static int SERVER_PORT = 8779;
=======
	private final static int SERVER_PORT = 8777;
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
	
	private static SocketHandler socketHandler;
	private static boolean stopped;
	
	
	public static void main(String[] args) {
		DatabaseManager dbm = new DatabaseManager();
		
<<<<<<< HEAD
		System.out.println("entrato");
		
=======
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
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
