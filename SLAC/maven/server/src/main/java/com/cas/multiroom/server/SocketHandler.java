package com.cas.multiroom.server;


import com.cas.multiroom.server.database.DatabaseManager;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.localization.ReferencePointMap;
import com.cas.multiroom.server.localization.ScanResult;
import com.cas.multiroom.server.messages.localization.MessageFingerprint;
import com.cas.multiroom.server.messages.localization.MessageNewReferencePoint;
import com.cas.multiroom.server.messages.localization.MessageReferencePointResult;
import com.cas.multiroom.server.messages.connection.MessageConnection;
import com.cas.multiroom.server.messages.connection.MessageConnectionBack;
import com.cas.multiroom.server.messages.Message;
import com.cas.multiroom.server.messages.localization.MessageStartScanReferencePoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Objects;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import java.lang.reflect.Field;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;


public class SocketHandler extends Thread {
    private final Socket clientSocket;
    private final DatabaseManager dbm;
    //private final SpeakerManager speakerManager;
    private boolean isRunning;
    
    private final String CSV_EXTENSION = ".csv";
    
    public SocketHandler(Socket clientSocket, DatabaseManager dbm) {//, SpeakerManager speakerManager) {
        this.clientSocket = clientSocket;
        this.dbm = dbm;
        //this.speakerManager = speakerManager;
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
	        
	        /*
	        String json = dataIn.readUTF();
	    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	        if (messageType.equals("START_MAPPING_PHASE")) {
	        	mappingPhase();
	        }
	        */
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("address: " + clientSocket.getInetAddress());
        System.out.println("local address: " + clientSocket.getLocalAddress());
        System.out.println("local socket address: " + clientSocket.getLocalSocketAddress());
        System.out.println("remote socket address: " + clientSocket.getRemoteSocketAddress());
        
        /*
        String clientId;
        try {
        	dataIn = new DataInputStream(clientSocket.getInputStream());
            dataOut = new DataOutputStream(clientSocket.getOutputStream());
            
            
            String json = dataIn.readUTF();
            MessageConnection connection = gson.fromJson(json, MessageConnection.class);
            clientId = connection.getId();
            if (clientId == null) {
                System.out.println("Client is null");
                dataOut.writeUTF(gson.toJson(new MessageConnectionBack(clientId, false)));
                dataOut.close();
                return;
            }

            dataOut.writeUTF(gson.toJson(new MessageConnectionBack(clientId, true)));
        } 
        catch(IOException e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("START SERVING: " + clientId);
        */
        
        String json;
		try {
			json = dataIn.readUTF();
			String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	        if (messageType.equals("START_MAPPING_PHASE")) {
	        	mappingPhase(dataIn);
	        }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    	
        /*
        while (isRunning) {
            try {
                // Find a change in the start/stop state
                //send start to the client
            	
                //dataOut.writeUTF(gson.toJson(new MessageStartScan(true)));
                //dataOut.flush();
                
            	MessageFingerprint resultMessage = gson.fromJson(dataIn.readUTF(), MessageFingerprint.class);

        //while (isRunning) {
        	//mappingPhase();
            /*try {
                // Find a change in the start/stop state
                //send start to the client
            	/*
                dataOut.writeUTF(gson.toJson(new MessageStartScan(true)));
                dataOut.flush();
                */
            	/*MessageFingerprint resultMessage = gson.fromJson(dataIn.readUTF(), MessageFingerprint.class);
                System.out.println(resultMessage);//.toJson());
                dataOut.writeUTF(gson.toJson(new MessageReferencePointResult(new ReferencePoint("edin dzeko")), MessageReferencePointResult.class));
                dataOut.flush();
                
            	String json = dataIn.readUTF();
            	System.out.println(json);
            	//MessageFingerprint resultMessage = gson.fromJson(json, MessageFingerprint.class);
            	//System.out.println(resultMessage.toJson());
            	//dataOut.writeUTF("Edin DZEKO MANCINI MERDA");
            	dataOut.writeUTF(gson.toJson(new MessageReferencePointResult(new ReferencePoint("edin dzeko"))));
                dataOut.flush();
            }
            catch (SocketException e) {
                e.printStackTrace();
                System.out.println("Connection closed");
                isRunning = false;
            }
            catch (EOFException e) {
            	e.printStackTrace();
                System.out.println("EOF");
                isRunning = false;
            }
            catch (IOException e) {
            	e.printStackTrace();
                System.err.println("Error while reading from the socket");
                isRunning = false;
            }
        }
        */
        
        //System.out.println("STOP SERVING: " + clientId);
    }

    
    public void mappingPhase(DataInputStream dataIn) {
    	DataInputStream dataInTemp = null;
        DataOutputStream dataOut = null;
        Gson gson = new Gson();
        
        System.out.println("ENTRATO MAPPING PHASE");
        
        if (clientSocket == null) {
        	System.out.println("USCITO QUI");
            return;
        }
        try {
	        dataInTemp = new DataInputStream(clientSocket.getInputStream());
	        dataOut = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }

        if(dataIn==dataInTemp) {
        	System.out.println("UGUALE");
        }
        if(Objects.equals(dataInTemp, dataIn)) {
        	System.out.println("UGUALIOOOOOI");
        }
        
        MessageNewReferencePoint resultMessage;
        ReferencePoint referencePoint;
        
        try {
	    	String json = dataIn.readUTF();
	    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	    	
	    	while (messageType.equals("NEW_REFERENCE_POINT")) {
	    		
	    		System.out.println("NEW REFERENCE POINT");
	    		resultMessage = gson.fromJson(json, MessageNewReferencePoint.class);
	        	referencePoint = resultMessage.getReferencePoint();
	        	
	    		// FARE IL SALVATAGGIO NEL DB DEL NUOVO REFERENCE POINT PASSATO
	        	
	    		createReferencePointCSV(referencePoint);
	    		
	    		json = dataIn.readUTF();
		    	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	    	}
	    	
	    	
	    	if (messageType.equals("END_MAPPING_PHASE")) {
	    		System.out.println("USCITO MAPPING PHASE");
	    	}
        }
        catch (Exception e) {
	        e.printStackTrace();
	    }
        	
        System.out.println("QUI USCITO MAPPING PHASE");
    }
    
    
    public void createReferencePointCSV(ReferencePoint referencePoint) {
    	LinkedHashMap<String, Integer> columns = new LinkedHashMap<String, Integer>();
    	int c = 0;
    	List<List<ScanResult>> df_scans = new ArrayList<List<ScanResult>>();
    	List<ReferencePointMap> list = new ArrayList<ReferencePointMap>();
    	
    	DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        Gson gson = new Gson();
        
        if (clientSocket == null)
            return;
        
        try {
	        dataIn = new DataInputStream(clientSocket.getInputStream());
	        dataOut = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    	
        
        MessageFingerprint messageFingerprint;
        List<ScanResult> scanResultList = new ArrayList<ScanResult>();
        
        try {
	    	String json = dataIn.readUTF();
	    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();

	    	if (messageType.equals("START_SCAN_REFERENCE_POINT")) {
	    		System.out.println("START SCAN");
	        	//MessageStartScanReferencePoint resultMessage = gson.fromJson(json, MessageStartScanReferencePoint.class);
	        	
	        	json = dataIn.readUTF();
	        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	        	
	        	while (!messageType.equals("END_SCAN_REFERENCE_POINT")) {
		    		System.out.println("WHILE");
		    		
		    		if (messageType.equals("FINGERPRINT")) {
		    			messageFingerprint = gson.fromJson(json, MessageFingerprint.class);
		    		}
		    		else {
		    			return;
		    		}
		        	
		    		List<ScanResult> scan = messageFingerprint.getFingerprint(); //.getScanResultList());
		    		for (ScanResult sr : scanResultList)
		    		{
		    			if (!columns.containsKey(sr.getBSSID()))
		    			{
		    				columns.put(sr.getBSSID(), c);
		    				c++;
		    			}
		    		}
		    		df_scans.add(scan);
		            scanResultList.addAll(scan);
		            
		            json = dataIn.readUTF();
		            messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	        	}
	    	
	    		System.out.println("FUORI WHILE");
	    		
	    		Integer level = null;
	    		for (List<ScanResult> lsr : df_scans) {
    				//list.add(new ReferencePointMap());
    				list.get(list.size() - 1).setName(referencePoint.getId());
    				for (ScanResult sr : lsr) {
    					for (String ap : columns.keySet()) {
    						level = null;
	    					if (ap.equals(sr.getBSSID())) {
	    						level = sr.getLevel();
	    					}
	    					list.get(list.size() - 1).setScans(columns.get(ap), level);
	    				}
	    			}
	    		}
	    		

	        	final String CSV_DIRECTORY_PATH = ".";
	        	final String CSV_FILENAME = referencePoint.getId() + CSV_EXTENSION;
	            final String CSV_LOCATION = CSV_DIRECTORY_PATH + "/" + CSV_FILENAME;
	            // first create file object for file placed at location
	            // specified by filepath
	            File file = new File(CSV_LOCATION);
	            
	            // Creating writer class to generate
	            // csv file
	            FileWriter writer = new FileWriter(file);
	            
	            
	            // Create Mapping Strategy to arrange the 
	            // column name in order
	            ColumnPositionMappingStrategy<ReferencePointMap> mappingStrategy = new ColumnPositionMappingStrategy<ReferencePointMap>();
	            mappingStrategy.setType(ReferencePointMap.class);
	  
	            
	            // Arrange column name as provided in below array.
	            //Field fields[] = ScanResult.class.getDeclaredFields();
	            //String[] columnsCsv = new String[fields.length];
	            String[] columnsCsv = new String[columns.keySet().size() + 1];
	            columnsCsv = (String[]) columns.keySet().toArray();
	            columnsCsv[columns.keySet().size()] = "REFERENCE POINT";
	            /*
	            for (int i = 0; i < fields.length; i++)
	            {
	            	columnsCsv[i] = fields[i].getName();
	                System.out.println("Variable Name is : " + fields[i].getName());
	            }
	            */
	            
	            mappingStrategy.setColumnMapping(columnsCsv);
	  
	            // Creating StatefulBeanToCsv object
	            StatefulBeanToCsvBuilder<ReferencePointMap> builder = new StatefulBeanToCsvBuilder<ReferencePointMap>(writer);
	            StatefulBeanToCsv<ReferencePointMap> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
	  
	            CSVWriter csvwriter = new CSVWriter(writer);
	            csvwriter.writeNext(columnsCsv);
	            csvwriter.close();
	            // Write list to StatefulBeanToCsv object
	            beanWriter.write(list);
	  			
	  
	            // closing the writer object
	            writer.close();
	    	}
	    	else {
	    		return;
	    	}
	    }
	    catch (SocketException e) {
	        e.printStackTrace();
	        System.out.println("Connection closed");
	        isRunning = false;
	    }
	    catch (EOFException e) {
	    	e.printStackTrace();
	        System.out.println("EOF");
	        isRunning = false;
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	        System.err.println("Error while reading from the socket");
	        isRunning = false;
	    }
        catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
    public static void writeDataLineByLine(String filePath)
    {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputFile = new FileWriter(file);
      
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);
            
            /*
            // create CSVWriter with ';' as separator
            CSVWriter writer = new CSVWriter(outputfile, ';',
                                             CSVWriter.NO_QUOTE_CHARACTER,
                                             CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                             CSVWriter.DEFAULT_LINE_END);
            */
      
            // adding header to csv
            String[] header = { "Name", "Class", "Marks" };
            writer.writeNext(header);
            
            // add data to csv
            String[] data1 = { "Aman", "10", "620" };
            writer.writeNext(data1);
            String[] data2 = { "Suraj", "10", "630" };
            writer.writeNext(data2);
            
            // create a List which contains String array
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] { "Name", "Class", "Marks" });
            data.add(new String[] { "Aman", "10", "620" });
            data.add(new String[] { "Suraj", "10", "630" });
            writer.writeAll(data);
      
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	// Java code to illustrate reading a
	// CSV file line by line
	public static void readDataLineByLine(String file)
	{
		try {
			// Create an object of filereader
			// class with CSV file as a parameter.
	        FileReader inputFile = new FileReader(file);
	   
	        // create csvReader object passing
	        // file reader as a parameter
	        CSVReader csvReader = new CSVReader(inputFile);
	        String[] nextRecord;
	        
	        /*
	        // create csvReader object and skip first Line
	        CSVReader csvReader = new CSVReaderBuilder(filereader)
	                                  .withSkipLines(1)
	                                  .build();
	        List<String[]> allData = csvReader.readAll();
	        */
	   
	        // we are going to read data line by line
	        while ((nextRecord = csvReader.readNext()) != null) {
	        	for (String cell : nextRecord) {
	        		System.out.print(cell + "\t");
	            }
	            System.out.println();
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    
	public static void BeanToCSV(String filePath)
    {
		// first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        // name of generated csv
        final String CSV_LOCATION = "ref.csv ";
  
        try {
  
            // Creating writer class to generate
            // csv file
            FileWriter writer = new 
                       FileWriter(CSV_LOCATION);
  
            /*
            // create a list of employee
            List<Employee> EmployeeList = new 
                                 ArrayList<Employee>();
            Employee emp1 = new Employee
                     ("Mahafuj", "24", "HTc", "75000");
            Employee emp2 = new Employee
                  ("Aman", "24", "microsoft", "79000");
            Employee emp3 = new Employee
                    ("Suvradip", "26", "tcs", "39000");
            Employee emp4 = new Employee
                     ("Riya", "22", "NgGear", "15000");
            Employee emp5 = new Employee
                    ("Prakash", "29", "Sath", "51000");
            EmployeeList.add(emp1);
            EmployeeList.add(emp2);
            EmployeeList.add(emp3);
            EmployeeList.add(emp4);
            EmployeeList.add(emp5);
            
  
            // Create Mapping Strategy to arrange the 
            // column name in order
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setType(Employee.class);
  
            // Arrange column name as provided in below array.
            String[] columns = new String[] { "Name", "Age", "Company", "Salary" };
            mappingStrategy.setColumnMapping(columns);
  
            // Creating StatefulBeanToCsv object
            StatefulBeanToCsvBuilder<Employee> builder = new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();
  
            // Write list to StatefulBeanToCsv object
            beanWriter.write(EmployeeList);
  			*/
  
            // closing the writer object
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/*
	public static void CSVToBean()
    {
 
        // Hashmap to map CSV data to
        // Bean attributes.
        Map<String, String> mapping = new
                      HashMap<String, String>();
        mapping.put("name", "Name");
        mapping.put("rollno", "RollNo");
        mapping.put("department", "Department");
        mapping.put("result", "Result");
        mapping.put("cgpa", "Pointer");
 
        // HeaderColumnNameTranslateMappingStrategy
        // for Student class
        HeaderColumnNameTranslateMappingStrategy<Student> strategy = new HeaderColumnNameTranslateMappingStrategy<Student>();
        strategy.setType(Student.class);
        strategy.setColumnMapping(mapping);
 
        // Create csvtobean and csvreader object
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader
            ("D:\\EclipseWorkSpace\\CSVOperations\\StudentData.csv"));
        }
        catch (FileNotFoundException e) {
 
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CsvToBean csvToBean = new CsvToBean();
 
        // call the parse method of CsvToBean
        // pass strategy, csvReader to parse method
        List<Student> list = csvToBean.parse(strategy, csvReader);
 
        // print details of Bean object
        for (Student e : list) {
            System.out.println(e);
        }
    }
	
    public void stopSocketHandler() {
		isRunning = false;
		this.interrupt();
	}
	*/
	
	public class Product {
        @CsvBindByName(column = "ap")
        public String ap[];
		@CsvBindByPosition(position = 0)
        @CsvBindByName(column = "productCode")
        public String id;
		@CsvBindByPosition(position = 1)
        @CsvBindByName(column = "MFD")
		public String member2;
        @CsvBindByName(column = "REFERENCE POINT")
		public String referencePoint;

        public Product(String id, String member2, String referencePoint) {
            this.id = id;
            this.member2 = member2;
            this.referencePoint = referencePoint;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMember2() {
            return member2;
        }

        public void setMember2(String member2) {
            this.member2 = member2;
        }

        public String getReferencePoint() {
            return referencePoint;
        }

        public void setReferencePoint(String referencePoint) {
            this.referencePoint = referencePoint;
        }
    }
}
