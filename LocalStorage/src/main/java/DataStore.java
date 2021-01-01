import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DataStore {
	String fileLocation;
	public DataStore() {    //initializing DataStore with default fileLocation
		this.fileLocation=System.getProperty("user.home")+"\\datastore.json";
	}
	public DataStore(String fileLocation) {   //if user explicitly provides a fileLocation 
		this.fileLocation=fileLocation;
	}
	
	static HashMap<String, List<Long>> map=new HashMap<>();    //Hashmap to keep track of time to live property of key
	
	//create method with timeToLive property
	public synchronized void create(String key,String value,int timeToLive) throws IOException, ParseException {
		if(key.length()>32) {  //if key length is greater than 32 chars
			System.err.println("Invalid key");
		}	
		else if(getFileSizeGigaBytes(new File(fileLocation))>1) {  //if file size exceeds 1GB
			System.err.println("Memory Limit reached");
		}
		else if(value.getBytes().length>16*1024) {   //if value of the key greater than 16KB
			System.err.println("Invalid Value");
		}
		else if(getFileSizeGigaBytes(new File(fileLocation))==0) {  //if file is empty or not exists then
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put(key, value);
		    JSONArray objects=new JSONArray();
		    objects.add(sampleObject);
		    List<Long> timeList=new ArrayList<>();
		    timeList.add(System.currentTimeMillis());
		    timeList.add((long) timeToLive);
		    map.put(key, timeList);
		    
		    Files.write(Paths.get(fileLocation), objects.toJSONString().getBytes());
		}
		else
		{
			FileReader reader = new FileReader(fileLocation);
		    JSONParser jsonParser = new JSONParser();
		    Object jsonObject = jsonParser.parse(reader);
		    JSONArray objects = (JSONArray) jsonObject;
		    
		    boolean flag=false;
		    Iterator<JSONObject> iterator = objects.iterator();
		    while(iterator.hasNext()) {
		      JSONObject obj=iterator.next();
		      if(obj.containsKey(key)) {
		    	  if(map.containsKey(key)) {  //if map contains key
		    		  long seconds=(System.currentTimeMillis()-map.get(key).get(0))/1000;
			    	  System.out.println(seconds);
			    	  if(map.get(key).get(1)<seconds)  //if TTL of the key exipres then
			    	  {
			    		 map.remove(key);
			    		 flag=true;
			    	  }
		    	  } else {
		    		  flag=true;
			    	  System.err.println("A key with given key already exists in the DataStore");
			    	  break;
		    	  }
		    	  
		      }
		    }
		    
		    if(flag==false) {
		    	JSONObject sampleObject = new JSONObject();
			    sampleObject.put(key, value);
			    List<Long> timeList=new ArrayList<>();
			    timeList.add(System.currentTimeMillis());
			    timeList.add((long) timeToLive);
			    map.put(key, timeList);
			    System.out.println(map);
			    objects.add(sampleObject);
			    Files.write(Paths.get(fileLocation), objects.toJSONString().getBytes());
		    }
		    
			
		}
		
	}
	
	public synchronized void create(String key,String value) throws IOException, ParseException {
		if(key.length()>32)
			System.err.println("Invalid key");
		else if(getFileSizeGigaBytes(new File(fileLocation))>1) {
			System.err.println("Memory Limit reached");
		} else if(value.getBytes().length>16*1024) {
			System.err.println("Invalid Value");
		}
		else if(getFileSizeGigaBytes(new File(fileLocation))==0) {
			JSONObject sampleObject = new JSONObject();
		    sampleObject.put(key, value);
		    JSONArray objects=new JSONArray();
		    objects.add(sampleObject);
		    Files.write(Paths.get(fileLocation), objects.toJSONString().getBytes());
		}
		else
		{
			FileReader reader = new FileReader(fileLocation);
		    JSONParser jsonParser = new JSONParser();
		    Object jsonObject = jsonParser.parse(reader);
		    JSONArray objects = (JSONArray) jsonObject;
		    
		    boolean flag=false;
		    Iterator<JSONObject> iterator = objects.iterator();
		    while(iterator.hasNext()) {
		      JSONObject obj=iterator.next();
		      if(obj.containsKey(key)) {
		    	  flag=true;
		    	  System.err.println("A key with given key already exists in the DataStore");
		    	  break;
		      }
		    }
		    
		    if(flag==false) {
		    	JSONObject sampleObject = new JSONObject();
			    sampleObject.put(key, value);
			    objects.add(sampleObject);
			    Files.write(Paths.get(fileLocation), objects.toJSONString().getBytes());
		    }
		    
			
		}
	}
	
	public synchronized JSONObject read(String key) throws IOException, ParseException {
		if(getFileSizeGigaBytes(new File(fileLocation))==0) {
			System.err.println("DataStore doesn't contain any data");
		}
		else {
			FileReader reader = new FileReader(fileLocation);
		    JSONParser jsonParser = new JSONParser();
		    Object jsonObject = jsonParser.parse(reader);
		    JSONArray objects = (JSONArray) jsonObject;
		    
		    boolean flag=false;
		    Iterator<JSONObject> iterator = objects.iterator();
		    while(iterator.hasNext()) {
		      JSONObject obj=iterator.next();
		      if(obj.containsKey(key)) {
		    	  flag=true;
		    	  if(map.containsKey(key)) {
		    		  long seconds=(System.currentTimeMillis()-map.get(key).get(0))/1000;
			    	  System.out.println(seconds);
			    	  if(map.get(key).get(1)<seconds)
			    	  {
			    		  System.err.println("This item is no longer available");
			    	  } else {
				    	  return obj;
			    	  }
		    	  }
		    	  return obj;
		      }
		    }
		    if(flag==false) {
		    	System.err.println("Data with given key doesn't exist in the DataStore");
		    }
		}
		return null;
		
		
	    
	}
	
	public synchronized void delete(String key) throws IOException, ParseException {
		if(getFileSizeGigaBytes(new File(fileLocation))==0) {
			System.err.println("DataStore doesn't contain any data");
		} else {
			FileReader reader = new FileReader(fileLocation);
		    JSONParser jsonParser = new JSONParser();
		    Object jsonObject = jsonParser.parse(reader);
		    JSONArray objects = (JSONArray) jsonObject;
		    
		    JSONArray updatedObjects = new JSONArray();
		    Iterator<JSONObject> iterator = objects.iterator();
		    while(iterator.hasNext()) {
		      JSONObject obj=iterator.next();
		      if(obj.containsKey(key)) {
		    	  if(map.containsKey(key)) {
		    		  long seconds=(System.currentTimeMillis()-map.get(key).get(0))/1000;
			    	  System.out.println(seconds);
			    	  if(map.get(key).get(1)<seconds) {
		    			  System.err.println("This item is no longer available to delete");
		    			  updatedObjects.add(obj);
		    		  }
		    	  }
		      }
		      else {
		    	  updatedObjects.add(obj);
		      }
		    }
		    Files.write(Paths.get(fileLocation), updatedObjects.toJSONString().getBytes());
		}
		
	    
	}
	
	private static double getFileSizeGigaBytes(File file) {
		return (double) file.length() / (1024 * 1024 * 1024);
	}

	
}

