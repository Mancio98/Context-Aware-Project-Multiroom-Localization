package com.cas.multiroom.server;


import com.google.common.base.Function;
import com.google.gson.Gson;

import com.cas.multiroom.server.database.DatabaseManager;
import com.cas.multiroom.server.database.MyAudioTrack;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.localization.ScanResult;
import com.cas.multiroom.server.messages.music.MessageMusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpServer;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.process.NgrokLog;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Server {
	private final static int SERVER_PORT = 50000;
	
	private static SocketHandler socketHandler;
	private static boolean stopped = false;
	private int count;
	
	//private final 
	
	
	public Server() {
		DatabaseManager dbm = new DatabaseManager();
		MessageMusic response_req_playlist = null;
		stopped = false;
		count = 0;
		System.out.println("entrato");
		
		
		final Function<NgrokLog, Void> logEventCallback = ngrokLog -> {
		     System.out.println(ngrokLog.getLine());
		     return null;
		};
		
		Path ngrokPath = Paths.get("C:\\Users\\bocca\\Desktop\\ngrok.exe");
		
		final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
		        .withAuthToken("2PNVOD6bRHs0by6yFp3RcRWSvdk_4Yx2Suv6YCbXgsFAX2Ay3") // TOKEN di Matteo
		        .withNgrokPath(ngrokPath)
		        .withRegion(Region.EU)
		        .withLogEventCallback(logEventCallback)
		        .withMaxLogs(10)
		        .build();

		final NgrokClient ngrokClient = new NgrokClient.Builder()
		        .withJavaNgrokConfig(javaNgrokConfig)
		        .build();
	
		
		// Open a tunnel to a local file server
		// <NgrokTunnel: "http://<public_sub>.ngrok.io" -> "file:///">
		/*final CreateTunnel fileserverCreateTunnel = new CreateTunnel.Builder()
		        .withAddr("file:///")
		        .build();*/
		
		final CreateTunnel createTunnel = new CreateTunnel.Builder()
		        .withAddr(8000)
		        .build();

		//final HttpClient httpClient = new DefaultHttpClient.Builder().build()
		//final Response<SomePOJOResponse> postResponse = httpClient.post("http://localhost:4040/api/tunnels",
		//                                                                 createTunnel,
		//                                                                 Tunnel.class);
		
		final Tunnel fileserverTunnel = ngrokClient.connect(createTunnel);
		
		
		//final NgrokClient ngrokClient = new NgrokClient.Builder().build();

		// Open a HTTP tunnel on the default port 80
		// <Tunnel: "http://<public_sub>.ngrok.io" -> "http://localhost:80">
		//final Tunnel httpTunnel = ngrokClient.connect();
		
		String publicUrl = fileserverTunnel.getPublicUrl();
		//String prefix = "http://";
		//publicUrl = publicUrl + "/Will_Clarke_Rock_with_me.mp3";
		//System.out.println(publicUrl);
		
		List<MyAudioTrack> music_list = new ArrayList<MyAudioTrack>();
		//music_list.add(new myAudioTrack(publicUrl, "Will Clarke", "Rock with me", "3:25"));
		
		System.out.println("server started at 8000");
		final String dir = System.getProperty("user.dir");
		Process process = null;
		System.out.println(dir);
		
		publicUrl = publicUrl + '/' + "music" + '/';
		String musicPath = dir + File.separator + "music";
		File musicfolder = new File(musicPath);
		File[] listOfSongs = musicfolder.listFiles();
		System.out.println(publicUrl);
		String MP3_EXTENTION = ".mp3";
		for (int i = 0; i < listOfSongs.length; i++) {
			if (listOfSongs[i].isFile()) {
				System.out.println("File " + listOfSongs[i].getName());
				
				String[] author_title = listOfSongs[i].getName().substring(0, listOfSongs[i].getName().length() - MP3_EXTENTION.length()).split("-");
				music_list.add(new MyAudioTrack(publicUrl + listOfSongs[i].getName(), author_title[0].trim(), author_title[1].trim(), ""));
				System.out.println(author_title[0].trim() + " " + author_title[1].trim());
			}
			else if (listOfSongs[i].isDirectory()) {
				System.out.println("Directory " + listOfSongs[i].getName());
			}		
		}
		
		response_req_playlist = new MessageMusic(music_list);
		
		if (dir.equals("C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server")) {
			process = startServerPython();
		}
		
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while(!stopped) {
                Socket clientSocket = serverSocket.accept();
                count++;
                System.out.println(count);
                socketHandler = new SocketHandler(clientSocket, dbm, response_req_playlist, this); //, speakerManger);
                socketHandler.start();
            }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (process != null) {
			stopServerPython(process);
		}
	}
	
	public int getActiveSocket() {
		return this.count;
	}
	
	public void socketConnectionClose() {
		this.count--;
	}
	
	private Process startServerPython() {
    	System.out.println("SERVER START");
    	ProcessBuilder processBuilder = new ProcessBuilder("python", "-m", "http.server", "8000").inheritIO();

        Process process = null;
		try {
			process = processBuilder.start();
			//process.waitFor();
		}
		catch (IOException e) { //| InterruptedException
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return process;
    }
	
	private void stopServerPython(Process process) {
		System.out.println("SERVER STOP");
		process.destroy();
	}
}
