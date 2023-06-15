package com.cas.multiroom.server.localization;

import java.io.IOException;

import com.cas.multiroom.server.database.DatabaseManager;

public class Training extends Thread {
	private DatabaseManager dbm;
	private String idMap;
	private String dataPath;
	private final String pythonPath;
	
	public Training(DatabaseManager dbm, String idMap, String dataPath) {
		this.dbm = dbm;
		this.idMap = idMap;
		this.dataPath = dataPath;
		this.pythonPath = "C:\\Users\\bocca\\Desktop\\laurea_magistrale_informatica\\SCA\\progetto\\Context-Aware-Project-Multiroom-Localization\\SLAC\\ml\\modeling.py";
	}
	
	@Override
	public void run() {
		this.startPythonModeling();
		
		// FARE CONTROLLO ULTERIORE SE I FILE SONO STATI CREATI O MENO
		// NEL CASO NEGATIVO DARE ERRORE
		this.dbm.updateMap(this.idMap, this.dataPath);
	}
	
	private void startPythonModeling() {
    	System.out.println("MODELING START");
    	ProcessBuilder procesBuilder = new ProcessBuilder("python", this.pythonPath, "-p", this.dataPath).inheritIO();

        Process process;
		try {
			process = procesBuilder.start();
		
			process.waitFor();
		}
		catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("MODELING STOP");
    }
}
