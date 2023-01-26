package server;


import com.google.gson.Gson;

import server.database.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	private final static int SERVER_PORT = 49152;
	
	private static SocketHandler socketHandler;
	private static boolean stopped;
	
	
	public static void main(String[] args) {
		DatabaseManager dbm = new DatabaseManager();
		
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
