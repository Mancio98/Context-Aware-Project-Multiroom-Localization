package com.cas.multiroom.server;


import com.cas.multiroom.server.database.DatabaseManager;
import com.cas.multiroom.server.database.Settings;
import com.cas.multiroom.server.database.User;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.localization.ScanResult;
import com.cas.multiroom.server.localization.Training;
import com.cas.multiroom.server.messages.localization.MessageChangeReferencePoint;
import com.cas.multiroom.server.messages.localization.MessageEndMappingPhase;
import com.cas.multiroom.server.messages.localization.MessageFingerprint;
import com.cas.multiroom.server.messages.localization.MessageNewReferencePoint;
import com.cas.multiroom.server.messages.localization.MessageStartMappingPhase;
import com.cas.multiroom.server.messages.connection.MessageAcknowledgment;
import com.cas.multiroom.server.messages.connection.MessageImage;
import com.cas.multiroom.server.messages.connection.MessageImageFinish;
import com.cas.multiroom.server.messages.connection.MessageLogin;
import com.cas.multiroom.server.messages.connection.MessageMapDetails;
import com.cas.multiroom.server.messages.connection.MessageMapRequest;
import com.cas.multiroom.server.messages.connection.MessageMapSubscription;
import com.cas.multiroom.server.messages.connection.MessageMappingPhaseFinish;
import com.cas.multiroom.server.messages.connection.MessageReferencePointFinish;
import com.cas.multiroom.server.messages.connection.MessageRegistration;
import com.cas.multiroom.server.messages.connection.MessageSubscriptionSuccessful;
import com.cas.multiroom.server.messages.connection.MessageSubscriptionUnsuccessful;
import com.cas.multiroom.server.messages.connection.MessageUpdateMapList;
import com.cas.multiroom.server.messages.Message;
import com.cas.multiroom.server.messages.MessageSettings;
import com.cas.multiroom.server.messages.music.MessageMusic;
import com.cas.multiroom.server.speaker.Speaker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;



public class SocketHandler extends Thread {
    private final Socket clientSocket;
    private final DatabaseManager dbm;
    private boolean isRunning;
    private MessageMusic message_music;
    private User userLogged = null;
    private String currentIdMap = null;
    private ReferencePoint currentRP = null;
    
    private Training t = null;
    private Server server;
    private final String CSV_EXTENSION = ".csv";
    
    /*
    private Table tableTime = Table.create("time");
    private IntColumn columnTimeSpent = IntColumn.create("time_spent");
    private IntColumn columnChannel = IntColumn.create("channel");
    */
    
    public SocketHandler(Socket clientSocket, DatabaseManager dbm, MessageMusic message_music, Server server) {
        this.clientSocket = clientSocket;
        this.dbm = dbm;
        this.message_music = message_music;
        this.server = server;
    }
    
    @Override
    public void run() {
    	isRunning = true;
        DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        Gson gson = new Gson();
        
        
        
        if (clientSocket == null)
            return;
        
        try {
	        dataIn = new DataInputStream(clientSocket.getInputStream());
	        dataOut = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("address: " + clientSocket.getInetAddress());
        System.out.println("local address: " + clientSocket.getLocalAddress());
        System.out.println("local socket address: " + clientSocket.getLocalSocketAddress());
        System.out.println("remote socket address: " + clientSocket.getRemoteSocketAddress());
        
        int i = 0 ;
        String json;
        
        while (isRunning) {
			try {
				
				System.out.println(server.getActiveSocket());
				json = dataIn.readUTF();
				System.out.println(json);
				
				
				String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
				
				Message message;
				if (messageType.equals("REQ_PLAYLIST")) {
					dataOut.writeUTF(gson.toJson(message_music));
	                dataOut.flush();
	                System.out.println("playlist inviata");
		        }
				else if (messageType.equals("REGISTRATION")) {
		        	message = gson.fromJson(json, MessageRegistration.class);
		        	message = dbm.insertUser(((MessageRegistration) message).getUser());
		        	
		        	dataOut.writeUTF(gson.toJson(message));
		        	dataOut.flush();
		        }
				else if (messageType.equals("LOGIN")) {
		        	message = gson.fromJson(json, MessageLogin.class);
	        		this.userLogged = ((MessageLogin) message).getUser();
	        		message = dbm.selectUser(this.userLogged);	
	        		System.out.println(this.userLogged.getUsername());
		        	
		        	dataOut.writeUTF(gson.toJson(message));
		        	dataOut.flush();
		        }
				else if (messageType.equals("START_MAPPING_PHASE")) {
					final String dir = System.getProperty("user.dir");
					System.out.println(dir);
					// specify an abstract pathname in the File object 
					File tmpDir = new File(dir + "\\tmp" + this.userLogged.getUsername()); 
					System.out.println(tmpDir.getAbsolutePath());

					// check if the directory can be created 
					// using the specified path name 
					if (tmpDir.mkdir() == true) { 
						System.out.println("Directory has been created successfully"); 
					} 
					else { 
						System.out.println("Directory cannot be created"); 
					}
					
					//DataInputStream bytesIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
					message = gson.fromJson(json, MessageStartMappingPhase.class);
					int len = ((MessageStartMappingPhase)(message)).getLen();
					System.out.println("Len: " + len);
					
				    dataOut.writeUTF(gson.toJson(new MessageAcknowledgment()));
	                dataOut.flush();
				    
	                byte[] data = null;
					if (len > 0) {
					    data = new byte[len];
					    dataIn.readFully(data, 0, data.length); // read the message
					}
				    System.out.println(data);
	                
				    String img_path = tmpDir.getAbsolutePath() + "\\image.jpg";
				    System.out.println(img_path);
					try( OutputStream stream = new FileOutputStream(img_path) ) 
					{
						stream.write(data);
					}
					catch (Exception e) 
					{
					   System.err.println("Couldn't write to file...");
					}
					
					dataOut.writeUTF(gson.toJson(new MessageImageFinish()));
	                dataOut.flush();
					
		        	boolean finished = this.mappingPhase(tmpDir);
		        	
		        	if (finished) {
		        		String dataPath = "C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\" + this.currentIdMap + "\\";
			        	t = new Training(this.dbm, this.currentIdMap, dataPath);
			        	t.start();
		        	}
		        	else {
		        		System.out.println("ERRORE NELLA MAPPING PHASE");
		        	}
		        }
				else if (messageType.equals("MAP_REQUEST")) {
		        	message = gson.fromJson(json, MessageMapRequest.class);
		        	this.currentIdMap = ((MessageMapRequest)(message)).getIdMap();
		        	
		        	// Prende l'immagine e convertirla in byte[]
		        	File fi = new File(dbm.getImagePath(this.currentIdMap));
		        	byte[] fileContent = Files.readAllBytes(fi.toPath());
		        	
		        	dataOut.writeUTF(gson.toJson(new MessageImage(fileContent.length)));
		        	dataOut.flush();
		        	System.out.println("PRE ACK");
		        	
		        	//json = dataIn.readUTF();
		        	json = this.messageOrKeepAlive();
		        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
		        	if (messageType.equals("ACKNOWLEDGMENT")) {
		        		// INVIO IMAGE
		        		dataOut.write(fileContent);
		        		dataOut.flush();
		        		System.out.println("PRE IMAGE_FINISH");
		        		
		        		//json = dataIn.readUTF();
		        		json = this.messageOrKeepAlive();
			        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
			        	System.out.println(messageType);
			        	if (!messageType.equals("IMAGE_FINISH")) {
			        		System.out.println("PRE BREAK " + messageType);
			        		break;
			        	}
		        	}
		        	
		        	ArrayList<ReferencePoint> rps = dbm.selectReferencePointWithIdMap(this.currentIdMap, this.userLogged.getUsername());
		        	
		        	dataOut.writeUTF(gson.toJson(new MessageMapDetails(rps)));
		        	dataOut.flush();
		        	System.out.println("MAP DETAILS FINISH");
		        }
				else if (messageType.equals("MAP_SUBSCRIPTION")) {
		        	MessageMapSubscription messageMS = gson.fromJson(json, MessageMapSubscription.class);
		        	String idMap = ((MessageMapSubscription)(messageMS)).getIdMap();
		        	String keyMap = ((MessageMapSubscription)(messageMS)).getKeyMap();
		        	
		        	String key = dbm.getKeyMapFromId(idMap);
		        	Message back = null;
		        	
		        	if (key.equals(keyMap)) {
		        		this.currentIdMap = idMap;
		        		// inserisci map_user
		        		dbm.insertMapUser(Integer.parseInt(idMap), this.userLogged.getUsername(), "", "");
		        		//crea settings tutto false
		        		ArrayList<ReferencePoint> rps = dbm.selectReferencePointWithIdMap(this.currentIdMap, this.userLogged.getUsername());
		        		
		        		back = new MessageSubscriptionSuccessful(rps);
		        	}
		        	else {
		        		back = new MessageSubscriptionUnsuccessful();
		        	}
		        	
		        	dataOut.writeUTF(gson.toJson(back));
		        	dataOut.flush();
		        }
		        else if (messageType.equals("SETTINGS")) {
		        	message = gson.fromJson(json, MessageSettings.class);
		        	MessageSettings messageSettings = ((MessageSettings)(message));
		        	String path = "C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\" + this.currentIdMap + "\\" + this.userLogged.getUsername() + ".json";
		        	this.setSettings(messageSettings.getArrSettings(), path);
		        	dbm.updateSettingsForMapUser(this.currentIdMap, this.userLogged.getUsername(), path);
		        	
		        	if (messageSettings.getIdMap() != null) {
		        		dbm.updateMapName(messageSettings.getMapName(), this.currentIdMap, this.userLogged.getUsername());
		        		this.currentIdMap = messageSettings.getIdMap();
		        		com.cas.multiroom.server.database.Map mapSub = new com.cas.multiroom.server.database.Map(this.currentIdMap, this.isMapReady());
		        		mapSub.setName(dbm.getMapName(this.currentIdMap, this.userLogged.getUsername()));
			        	dataOut.writeUTF(gson.toJson(new MessageUpdateMapList(mapSub)));
			        	dataOut.flush();
		        	}
		        	else {
		        		/*dataOut.writeUTF(gson.toJson());
			        	dataOut.flush();*/
		        	}
		        }
		        else if (messageType.equals("FINGERPRINT")) {
		        	ReferencePoint rp = null;
		        	message = gson.fromJson(json, MessageFingerprint.class);
		        	List<ScanResult> f = ((MessageFingerprint)(message)).getFingerprint();
		        	rp = this.startIndoorLocalization("C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\ml\\predict.py", f, "C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\" + this.currentIdMap + "\\");
		        	System.out.println("C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\" + this.currentIdMap + "\\");
		        	if (rp != null) {
		        		if (currentRP != null) {
	        				System.out.println("CHANGE : " + !currentRP.getId().equals(rp.getId()));
	        				System.out.println(currentRP.getId());
	        				System.out.println(rp.getId());
		        		}
		        		if (currentRP == null || !currentRP.getId().equals(rp.getId())) {
			        		currentRP = rp;
			        		System.out.println(gson.toJson(new MessageChangeReferencePoint(currentRP)));
			        		dataOut.writeUTF(gson.toJson(new MessageChangeReferencePoint(currentRP)));
				        	dataOut.flush();
			        	}
		        	}
		        	else {
		        		System.out.println("MISURAZIONI SBAGLIATE");
		        		dataOut.writeUTF(gson.toJson(new MessageChangeReferencePoint(null)));
			        	dataOut.flush();
		        	}
		        }
		        else if (messageType.equals("KEEP_ALIVE")) {
		        	System.out.println("KEEP_ALIVE");
		        }
		        else {
		        	System.out.println("ELSE CASE");
		        }
		        /*
		        else if (messageType.equals("CONNECTION_CLOSE")) {
		        	connectionClose();
		        }
		        */
			}
			catch (IOException e) {
				e.printStackTrace();
				System.out.println("catch exception");
				/*
				tableTime.addColumns(columnTimeSpent);
				tableTime.addColumns(columnChannel);
				tableTime.write().csv("C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\time_spent.csv");
				*/
				connectionClose();
			}
        }
    }
    
    
    private String messageOrKeepAlive() throws IOException {
    	String json = null;
    	String messageType = null;
    	DataInputStream dataIn = null;
    	Gson gson = new Gson();
    	Message message = null;
    	
    	dataIn = new DataInputStream(clientSocket.getInputStream());
    	
    	json = dataIn.readUTF();
    	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
    	System.out.println(messageType);
    	while (messageType.equals("KEEP_ALIVE")) {
    		json = dataIn.readUTF();
        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
        	System.out.println(messageType);
    	}
    	
    	return json;
    }
    
    
    private void connectionClose() {
    	System.out.println("CLOSE " + server.getActiveSocket());
    	
    	System.out.println(t);
    	if (t != null) {
    		try {
				t.join();
				System.out.println("aspettato");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	this.isRunning = false;
    	try {
			clientSocket.close();
			this.server.socketConnectionClose();
			System.out.println(server.getActiveSocket());
		}
    	catch (IOException e) {
			e.printStackTrace();
		}
    	this.interrupt();
    	
    	System.out.println("dopo aspettato");
    }
    
    
    private boolean isMapReady() {
    	// CHIAMATA AL DB PER SSAPERE SE IL PERCORSO DATASET E PYTHON SONO BUONI, ALTRIMENTI NON READY
    	return dbm.isMapReady(this.currentIdMap);
    }
    

    private ReferencePoint startIndoorLocalization(String pythonScript, List<ScanResult> f, String dataPath) {
    	System.out.println("LOCALIZATION START");
    	ReferencePoint rp = null;
    	JSONObject fingerprint = new JSONObject();

    	for (ScanResult sr : f) {
    		fingerprint.put('"' + sr.getBSSID() + '"', sr.getLevel());
    	}
    	
    	System.out.println(fingerprint.toString());
    	
    	ProcessBuilder process_builder = new ProcessBuilder("python", pythonScript, "-f", fingerprint.toString(), "-p", dataPath);

        Process process;
		try {
			/*
			long start = System.currentTimeMillis();
			long channel = start - time;
			*/
			process = process_builder.start();
		
			int exit_code = process.waitFor();
			/*
			long stop = System.currentTimeMillis();
			long predict_time = stop - start;
			
			System.out.println(predict_time);
			
			columnTimeSpent.append((int) predict_time);
			columnChannel.append((int) (((int) channel) * 2 + predict_time));
			*/
			System.out.println("EXIT CODE : " + exit_code);
			
	        BufferedReader Buffered_Reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String output = "";
	        
	        while ((output = Buffered_Reader.readLine()) != null) {
	            System.out.println("OUTPUT : " + output);
	            // FARE IL CONTROLLO SUI SETTINGS
	            rp = dbm.selectReferencePoint(this.currentIdMap, this.userLogged.getUsername(), output);
	            System.out.println(rp);
	            if (rp != null) {
	            	System.out.println(rp.getId());
	            	if (rp.getSpeaker() != null) {
	            		System.out.println(rp.getSpeaker().getName());
	            	}
		            System.out.println(rp.getDND());
	            }
	        }
		}
		catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("PYTHON STOP");
		return rp;
    }
    
    
    public void setSettings(ArrayList<Settings> settings, String path) { // MessageSettings
    	
    	try {
    	    // create a map
    	    Map<String, Object> map = new HashMap<>();
    	    map.put("Settings", settings);
    	    
    	    for (Settings s : settings) {
    	    	if (this.currentRP != null && !(this.currentRP.getSpeaker().getMAC() == null && s.getSpeaker().getMAC() == null)) {
    	    		if (this.currentRP.getId().equals(s.getIdReferencePoint()) && 
        	    			((this.currentRP.getSpeaker().getMAC() == null && s.getSpeaker().getMAC() != null) 
        					|| (this.currentRP.getSpeaker().getMAC() != null && s.getSpeaker().getMAC() == null)
        					|| !this.currentRP.getSpeaker().getMAC().equals(s.getSpeaker().getMAC()))) {
        	    		this.currentRP = null;
        	    	}
    	    	}
    	    }
    	    
    	    // create a writer
    	    Writer writer = new FileWriter(path);
    	    
    	    // convert map to JSON File
    	    new Gson().toJson(settings, writer);

    	    // close the writer
    	    writer.close();  
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    }
    
    
    public boolean mappingPhase(File dir) throws IOException {
    	boolean finished = false;
    	DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        Gson gson = new Gson();
        
        int id_map = -1;
        
        System.out.println("ENTRATO MAPPING PHASE");
        

        dataIn = new DataInputStream(clientSocket.getInputStream());
        dataOut = new DataOutputStream(clientSocket.getOutputStream());
        
        
        HashMap<ReferencePoint, Table> referencePoints = new HashMap<ReferencePoint, Table>();
        MessageNewReferencePoint resultMessage;
        ReferencePoint referencePoint;

    	//String json = dataIn.readUTF();
    	String json = this.messageOrKeepAlive();
    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
    	
    	while (messageType.equals("NEW_REFERENCE_POINT")) {
    		System.out.println("NEW REFERENCE POINT");
    		resultMessage = gson.fromJson(json, MessageNewReferencePoint.class);
        	referencePoint = resultMessage.getReferencePoint();
        	
    		// FARE IL SALVATAGGIO NEL DB DEL NUOVO REFERENCE POINT PASSATO
        	
        	Table t = createReferencePointCSV(referencePoint);
        	if (t == null) {
        		System.out.println("ERRORE DA QUALCHE PARTE");
        		return finished;
        	}
        	referencePoints.put(referencePoint, t);
        	
        	dataOut.writeUTF(gson.toJson(new MessageReferencePointFinish()));
        	dataOut.flush();
        	
    		//json = dataIn.readUTF();
    		json = this.messageOrKeepAlive();
	    	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	    	// FORSE SUFFICIENTE UN IF
	    	while (messageType.equals("FINGERPRINT")) {
	    		//json = dataIn.readUTF();
	    		json = this.messageOrKeepAlive();
		    	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	    	}
    	}
    	
    	System.out.println(messageType);
    	if (messageType.equals("END_MAPPING_PHASE")) {
    		MessageEndMappingPhase endMessage = gson.fromJson(json, MessageEndMappingPhase.class);
    		id_map = dbm.insertMap(endMessage.getKey(), dir);
    		System.out.println("MAPPA RITORNATA " + id_map);
    		
    		this.currentIdMap = Integer.toString(id_map);
    		String mapName = endMessage.getMapName();
    		
    		String tmp = "tmp" + this.userLogged.getUsername();
    		String path = dir.getAbsolutePath().substring(0, dir.getAbsolutePath().length() - tmp.length()) + Integer.toString(id_map);
    		File mapDir = new File(path);
    		boolean flag = dir.renameTo(mapDir);
    		System.out.println("Directory renamed? " + flag);
    		
    		dbm.updateImagePath(this.currentIdMap, path + "\\image.jpg");
    		String settingsPath = "C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\maven\\server\\" + this.currentIdMap + "\\" + this.userLogged.getUsername() + ".json";
    		System.out.println("SETTINGS PATH : " + settingsPath);
    		this.setSettings(endMessage.getArrSettings(), settingsPath);
    		dbm.updateSettingsForMapUser(this.currentIdMap, this.userLogged.getUsername(), settingsPath);
    		
    		for (ReferencePoint rp : referencePoints.keySet()) {
	    		rp.setPathCSV("C:/Users/bocca/Desktop/laurea_magistrale_informatica/SCA/progetto/Context-Aware-Project-Multiroom-Localization/SLAC/maven/server/" + this.currentIdMap + "/" + rp.getId() + CSV_EXTENSION);
    			//referencePoints.get(rp).write().csv("C:/Users/bocca/Desktop/laurea_magistrale_informatica/SCA/progetto/Context-Aware-Project-Multiroom-Localization/SLAC/maven/server/" + rp.getId() + ".csv");
    			referencePoints.get(rp).write().csv(rp.getPathCSV());  // mapDir.getAbsolutePath() + '\\' + rp.getId()
    			dbm.insertReferencePoint(id_map, rp);
    		}
    		
    		dbm.insertMapUser(id_map, userLogged.getUsername(), mapName, settingsPath);
    	}
    	
    	System.out.println("QUI USCITO MAPPING PHASE");
        
        String csvPath = "C:/Users/bocca/Desktop/laurea_magistrale_informatica/SCA/progetto/Context-Aware-Project-Multiroom-Localization/SLAC/maven/server/" + Integer.toString(id_map) + "/";
        System.out.println(csvPath);
        
        dataOut.writeUTF(gson.toJson(new MessageMappingPhaseFinish()));
    	dataOut.flush();
    	
    	finished = true;
        
        return finished;
    }
    
    
    public Table createReferencePointCSV(ReferencePoint referencePoint) throws IOException {
    	DataInputStream dataIn = null;
        Gson gson = new Gson();
        
        if (clientSocket == null)
            return null;

        dataIn = new DataInputStream(clientSocket.getInputStream());
    	
        // Create table with new reference point id (name)
        Table table = Table.create(referencePoint.getId());
        String srBSSID = "";
        //int size = -1 ;
        IntColumn columnFound = null;
        
        MessageFingerprint messageFingerprint;
        
    	//String json = dataIn.readUTF();
    	String json = this.messageOrKeepAlive();
    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();

    	if (messageType.equals("START_SCAN_REFERENCE_POINT")) {
    		System.out.println("START SCAN");
        	//MessageStartScanReferencePoint resultMessage = gson.fromJson(json, MessageStartScanReferencePoint.class);
        	
        	//json = dataIn.readUTF();
        	json = this.messageOrKeepAlive();
        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
        	
        	while (!messageType.equals("END_SCAN_REFERENCE_POINT")) {
	    		System.out.println("WHILE");
	    		System.out.println(table);
	    		System.out.println(table.shape());
	    		
	    		if (messageType.equals("FINGERPRINT")) {
	    			messageFingerprint = gson.fromJson(json, MessageFingerprint.class);
	    		}
	    		else {
	    			return null;
	    		}
	        	
	    		
	    		List<ScanResult> scanResultList = messageFingerprint.getFingerprint(); //.getScanResultList());
	    		//size = -1;
	    		
	    		for (Column column : table.columns()) {
    				column.appendMissing();
	    		}
	    		
	    		for (ScanResult sr : scanResultList)
	    		{
	    			System.out.println("sr");
	    			columnFound = null;
	    			srBSSID = sr.getBSSID();
	    			
	    			for (Column column : table.columns()) {
	    				if (srBSSID.equals(column.name())) {
	    					columnFound = (IntColumn)column;
	    				}
	    			}
	    			
	    			if (columnFound != null) {
	    				System.out.println("SIZE:" + columnFound.size());
	    				columnFound.set(columnFound.size() - 1, sr.getLevel());
	    				//columnFound.append(sr.getLevel());
	    			}
	    			else {
	    				IntColumn intColumn = IntColumn.create(srBSSID);
    					for (int i = 0; i < table.rowCount() - 1; i++) {
    						intColumn.appendMissing();
    					}
    					
    					intColumn.append(sr.getLevel());
    					System.out.println(intColumn.print());
    					table.addColumns(intColumn);
	    			}
	    		}
	    		
	            //json = dataIn.readUTF();
	            json = this.messageOrKeepAlive();
	            messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
        	}
    	
    		System.out.println("FUORI WHILE");
            
            System.out.println(table);
    	}
    	else {
    		return null;
    	}
        
        return table;
    }
}
