import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Tester {
public static void main(String[] args) throws IOException, ParseException {
		
		DataStore dataStore=new DataStore();
		dataStore.create("9757f2d3-80d4-420a-85f7", "Foo");
		dataStore.create("9757f2d3-80d4-420a-85f9", "Bar");
		
		JSONObject obj=dataStore.read("9757f2d3-80d4-420a-85f9");
		if(obj!=null) {
			System.out.println(obj);
		}
		
		dataStore.create("9757f2d3-80d4-420a-85f8", "Foo",3);
		
		
		JSONObject obj1=dataStore.read("9757f2d3-80d4-420a-85f8");
		if(obj1!=null) {
			System.out.println(obj1);
		}
		
		
		dataStore.create("9757f2d3-80d4-420a-85f9", "NewBar"); 
		
	} 
}
