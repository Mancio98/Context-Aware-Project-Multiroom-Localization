package com.cas.multiroom.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.cas.multiroom.server.Server;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.localization.ReferencePointMap;
import com.cas.multiroom.server.localization.ScanResult;
import com.cas.multiroom.server.messages.localization.MessageFingerprint;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.lang.reflect.Field;


public class App 
{
    public static void main( String[] args )
    {
        //Server server = new Server();
    	createReferencePointCSV(new ReferencePoint("Cucina"));
    }
    
    public static void createReferencePointCSV(ReferencePoint referencePoint) {
    	LinkedHashMap<String, Integer> columns = new LinkedHashMap<String, Integer>();
    	int c = 0;
    	List<List<ScanResult>> df_scans = new ArrayList<List<ScanResult>>();
    	List<ReferencePointMap> list = new ArrayList<ReferencePointMap>();
    	
    	DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        Gson gson = new Gson();
    	
        
        MessageFingerprint messageFingerprint;
        List<ScanResult> scanResultList = new ArrayList<ScanResult>();
        
	        	
	        	/*while (!messageType.equals("END_SCAN_REFERENCE_POINT")) {
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
	        	}*/
	    	
	    		System.out.println("FUORI WHILE");
	    		List<ScanResult> scan = new ArrayList<ScanResult>();
	    		scan.add(new ScanResult("matteo", "matteo", 10));
	    		scan.add(new ScanResult("daje", "daje", 20));
	    		scan.add(new ScanResult("roma", "roma", 30));
	    		df_scans.add(scan);
	            scanResultList.addAll(scan);
	            
	            for (ScanResult sr : scan)
	    		{
	    			if (!columns.containsKey(sr.getBSSID()))
	    			{
	    				columns.put(sr.getBSSID(), c);
	    				c++;
	    			}
	    		}
	            
	            for (String col : columns.keySet())
	            {
	            	System.out.println(col);
	            }
	            
	            
	            
	    		
	    		Integer level = null;
	    		for (List<ScanResult> lsr : df_scans) {
    				list.add(new ReferencePointMap(columns.size()));
    				list.get(list.size() - 1).setName(referencePoint.getId());
    				for (ScanResult sr : lsr) {
    					level = null;
    					if (columns.containsKey(sr.getBSSID())) {
	    					level = sr.getLevel();
    					}
    					list.get(list.size() - 1).setScans(columns.get(sr.getBSSID()), level);
    					/*for (String ap : columns.keySet()) {
    						level = null;
	    					if (ap.equals(sr.getBSSID())) {
	    						level = sr.getLevel();
	    					}
	    					list.get(list.size() - 1).setScans(columns.get(ap), level);
	    				}*/
	    			}
	    		}
	    		
	    		for (ReferencePointMap rpm : list) {
	    			for (Integer lev : rpm.getScans()) {
	    				System.out.println(lev);
	    			}
	    			System.out.println(rpm.getName());
	    		}

	        	final String CSV_DIRECTORY_PATH = ".";
	        	final String CSV_FILENAME = referencePoint.getId() + ".csv";
	            final String CSV_LOCATION = CSV_DIRECTORY_PATH + "/" + CSV_FILENAME;
	            // first create file object for file placed at location
	            // specified by filepath
	            File file = new File(CSV_LOCATION);
	            
	            // Creating writer class to generate
	            // csv file
	            FileWriter writer;
				try {
					writer = new FileWriter(file);
					
					// Create Mapping Strategy to arrange the 
		            // column name in order
		            ColumnPositionMappingStrategy<ReferencePointMap> mappingStrategy = new ColumnPositionMappingStrategy<ReferencePointMap>();
		            mappingStrategy.setType(ReferencePointMap.class);
		  
		            
		            // Arrange column name as provided in below array.
		            Field fields[] = ReferencePointMap.class.getDeclaredFields();
		            //String[] columnsCsv = new String[fields.length];
		            String[] columnsCsv = new String[fields.length];  // columns.keySet().size() + 1
		            /*
		            int i = 0;
		            for (String col : columns.keySet())
		            {
		            	columnsCsv[i] = col;
		            	i++;
		            }
		            */
		            //columnsCsv = (String[]) columns.keySet().toArray();
		            //columnsCsv[columns.keySet().size()] = "name";
		            
		            System.out.println("len: " + fields.length);
		            for (int f = 0; f < fields.length; f++)
		            {
		            	columnsCsv[f] = fields[f].getName();
		                System.out.println("Variable Name is : " + fields[f].getName());
		            }
		            /*
		            for (int j = 0; j < columnsCsv.length; j++)
		            {
		            	System.out.println(columnsCsv[j]);
		            }
		            */
		            mappingStrategy.setColumnMapping(columnsCsv);
		  
		            // Creating StatefulBeanToCsv object
		            StatefulBeanToCsvBuilder<ReferencePointMap> builder = new StatefulBeanToCsvBuilder<ReferencePointMap>(writer);
		            StatefulBeanToCsv<ReferencePointMap> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
		  
		            CSVWriter csvwriter = new CSVWriter(writer);
		            List<String[]> data = new ArrayList<String[]>();
		            csvwriter.writeNext(columnsCsv);
		            for (ReferencePointMap rpm : list) {
		            	data.add(rpm.toCSV());
		            	//csvwriter.writeNext(rpm.toCSV());
		            }
		            csvwriter.writeAll(data);
		            csvwriter.close();
		            // Write list to StatefulBeanToCsv object
		            
		            beanWriter.write(list);
		            // closing the writer object
		            writer.close();
				}
				catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            
	           
    }
}
