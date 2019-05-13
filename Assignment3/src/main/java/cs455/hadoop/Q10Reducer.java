package cs455.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

public class Q10Reducer  extends Reducer<Text, Text, Text, Text>{
	Map<Integer, ArrayList<Text>> yearsTopSongs = new HashMap<Integer, ArrayList<Text>>();	
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    	Text combine=new Text();
    	Text first=new Text();
    	Text artist_song=new Text();
    	
    	String[] parsedData;
    	int i=0;
    	for(Text value:values) {
            parsedData=value.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if(i==0) {
            	first.set(value);
            }else {
            	if(parsedData.length>30) {
            		combine.set(first+","+value);
            	}
            	else {
                	combine.set(value+","+first);
            	}
            }
            i+=1;
    	}

    	
    	if(combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).length>=33) {
	    	//q10
	    	try {
	    		parsedData=combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	    		int year=Integer.parseInt(parsedData[14].trim());
	    		if(year!=0 ) {
	        		
	        		if(!yearsTopSongs.containsKey(year)) {
	        			yearsTopSongs.put(year, new ArrayList<Text>());
	
	        		}
	        		if(yearsTopSongs.get(year).size()<10) {
	        			yearsTopSongs.get(year).add(combine);
	        			//yearsTopSongsHotness.get(year).add(Double.parseDouble(parsedData[15+2]));
	        		}else {
	        			double lowest=1000;
	        			int lowestIndex=0;
	        			for(i=0;i<yearsTopSongs.get(year).size();i++) {
	        				if(Double.parseDouble(yearsTopSongs.get(year).get(i).toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)[15+2])<lowest) {
	        					lowestIndex=i;
	        					lowest=Double.parseDouble(yearsTopSongs.get(year).get(i).toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)[15+2]);
	        				}
	        				
	        			}
	        			if(Double.parseDouble(parsedData[15+2])>lowest) {
	        				yearsTopSongs.get(year).remove(lowestIndex);
	        				yearsTopSongs.get(year).add(combine);
	        			}
	        		}
	    		}
	    	}catch(Exception e) {}
    	}
    }
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
    
        //q10
        Map<Integer, String> yearTerms=new HashMap<Integer, String>();
        String s=new String();
        for(	Map.Entry<Integer, ArrayList<Text>> a:yearsTopSongs.entrySet()) {
        	yearTerms.put(a.getKey(), new String());
        	for(Text b: a.getValue()) {
        		String[] topTerms=b.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)[11].trim().toString().split(" ");
        		for(int i=0;i<5;i++) {
        			s=yearTerms.get(a.getKey())+" " +topTerms[i];
        			yearTerms.put(a.getKey(),s);
        			//context.write(new Text(String.valueOf(a.getKey())), new Text(topTerms[i]));
        		}
        		
        	}
        	
        }
        
        for(Map.Entry<Integer, String> a:yearTerms.entrySet()) {      
        	context.write(new Text(String.valueOf(a.getKey())), new Text(a.getValue()));
        }
    	
    	
    }
    
    
}
