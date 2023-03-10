package com.cas.multiroom.server;


import com.cas.multiroom.server.database.DatabaseManager;
import com.cas.multiroom.server.localization.ReferencePoint;
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
import java.util.List;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import java.lang.reflect.Field;


public class SocketHandler extends Thread {
    private final Socket clientSocket;
    private final DatabaseManager dbm;
    //private final SpeakerManager speakerManager;
    private boolean isRunning;
    
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
	        	mappingPhase();
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
    
    
    public void mappingPhase() {
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
    	
        MessageNewReferencePoint resultMessage;
        ReferencePoint referencePoint;
        
        try {
	    	String json = dataIn.readUTF();
	    	String messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	    	
	    	while (messageType.equals("NEW_REFERENCE_POINT")) {
	    		resultMessage = gson.fromJson(json, MessageNewReferencePoint.class);
	        	referencePoint = resultMessage.getReferencePoint();
	        	
	    		// FARE IL SALVATAGGIO NEL DB DEL NUOVO REFERENCE POINT PASSATO
	        	
	    		createReferencePointCSV(referencePoint);
	    	}
	    	
	    	if (messageType.equals("END_MAPPING_PHASE")) {
	    		
	    	}
        }
        catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
    
    public void createReferencePointCSV(ReferencePoint referencePoint) {
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
	        	//MessageStartScanReferencePoint resultMessage = gson.fromJson(json, MessageStartScanReferencePoint.class);
	        	
	        	json = dataIn.readUTF();
	        	messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
	        	
	        	while (!messageType.equals("END_SCAN_REFERENCE_POINT")) {
		    		if (messageType.equals("FINGERPRINT")) {
		    			messageFingerprint = gson.fromJson(json, MessageFingerprint.class);
		    		}
		    		else {
		    			return;
		    		}
		        	
		            scanResultList.addAll(messageFingerprint.getFingerprint().getScanResultList());
		            
		            json = dataIn.readUTF();
		            messageType = gson.fromJson(json, JsonObject.class).get("type").getAsString();
		    	}
	        	
	        	
	        	String filePath = "";
	        	// first create file object for file placed at location
	            // specified by filepath
	            File file = new File(filePath);
	            // name of generated csv
	            final String CSV_LOCATION = referencePoint.getId() + ".csv ";
	            
	            // Creating writer class to generate
	            // csv file
	            FileWriter writer = new FileWriter(CSV_LOCATION);
	            
	            
	            // Create Mapping Strategy to arrange the 
	            // column name in order
	            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
	            mappingStrategy.setType(ScanResult.class);
	  
	            
	            Field fields[] = ScanResult.class.getDeclaredFields();
	            for (int i = 0; i < fields.length; i++)
	            {
	                System.out.println("Variable Name is : " + fields[i].getName());
	            }
	            
	            
	            // Arrange column name as provided in below array.
	            String[] columns = new String[] { "BSSID", "SSID", "level" };
	            mappingStrategy.setColumnMapping(columns);
	  
	            // Creating StatefulBeanToCsv object
	            StatefulBeanToCsvBuilder<ScanResult> builder = new StatefulBeanToCsvBuilder(writer);
	            StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();
	  
	            // Write list to StatefulBeanToCsv object
	            beanWriter.write(scanResultList);
	  			
	  
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
}
