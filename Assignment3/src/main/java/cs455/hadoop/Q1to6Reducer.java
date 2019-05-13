package cs455.hadoop;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.collect.ImmutableList;

public class Q1to6Reducer  extends Reducer<Text, Text, Text, Text> {
	Text combine=new Text();
	double db=0;
	String[] parsedData = null;

	double fadeTime=0;
	double duration=0;
	double totals=0;
	
	Map<Text, Integer> artists_songs = new HashMap<Text, Integer>();
	Map<Text, ArrayList<Double>> loudness = new HashMap<Text, ArrayList<Double>>();
	Map<Text, Double> song_duration=new HashMap<Text, Double>();
	
	
	ArrayList<Text> hottness = new ArrayList<Text>();
	double bestHotness=0;
	Text bestHotnessAllData=new Text();
	
	Map<Text, Double> fade = new HashMap<Text, Double>();
	ArrayList<Map.Entry<Text, Double>> energetic_danceable = new ArrayList<Map.Entry<Text, Double>>();
	
	
	ArrayList<Double> startTime=new ArrayList<Double>();
	ArrayList<Double> startTimeCount=new ArrayList<Double>();
	String[] segmentData;
	double last=0;
	
	
	ArrayList<Double> pitch=new ArrayList<Double>();
	ArrayList<Double> pitchCount=new ArrayList<Double>();
	
	ArrayList<Double> timbre=new ArrayList<Double>();
	ArrayList<Double> timbreCount=new ArrayList<Double>();
	
	ArrayList<Double> maxLoudness=new ArrayList<Double>();
	ArrayList<Double> maxLoudnessCount=new ArrayList<Double>();
	
	ArrayList<Double> maxLoudTime=new ArrayList<Double>();
	ArrayList<Double> maxLoudTimeCount=new ArrayList<Double>();
	
	ArrayList<Double> startLoudness=new ArrayList<Double>();
	ArrayList<Double> startLoudnessCount=new ArrayList<Double>();
	
	//q9
	ArrayList<Map.Entry<Text, Double>> top25HottnessSongs = new ArrayList<Map.Entry<Text, Double>>();
	Map<Text,Text> artistsList=new HashMap<Text,Text>();
	Text artist_=new Text();
	
	
	
	//q8
	Map<Text,Double> artistsList_tempo=new HashMap<Text,Double>();//14
	Map<Text,Integer> artistsList_tempoCount=new HashMap<Text,Integer>();//14
	double tempoAll=0;
	int tempoAllCount=0;

	
	Map<Text,Integer> termCountAll=new HashMap<Text,Integer>();	
	Map<Text,Map<Text, Integer>> termCountArtist=new HashMap<Text,Map<Text, Integer>>();	

	
	//q10
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
    		
    		
    		//q1
    		parsedData=combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    		Text artist=new Text();
    		artist.set(parsedData[7]);
    		if(!artists_songs.containsKey(artist)){
        		artists_songs.put(artist, 0);
    		}
    		int count = (artists_songs.containsKey(artist) ? artists_songs.get(artist) : 0);
    		artists_songs.put(artist, count+1);
    		
    		
    		//q2
    		try {
        		//artist.set(parsedData[7]);
    			db=Double.parseDouble(parsedData[15+10].toString());
    			if(!loudness.containsKey(artist)) {
    				loudness.put(artist, new ArrayList());
    			}
    	    	if(loudness.containsKey(artist)) {
    	    		loudness.get(artist).add(db);
    	    	}
    		}catch(NumberFormatException e) {
    		}

	    	
    		
    		//q3
	    	artist_song.set(parsedData[9].trim()+" by " +parsedData[7].trim());
	    	try {
				db=Double.parseDouble(parsedData[15+2].toString());
		    	if(db>bestHotness) {
		    		bestHotness=db;
		    		hottness.clear();
		    		hottness.add(artist_song);
		    		bestHotnessAllData.set(combine);
		    	}else if(db==bestHotness) {
		    		hottness.add(artist_song);
		    	}	    		
	    	}catch(Exception e) {
	    		
	    	}

	    	
	    	
	    	
	    	
	    	
	    	//q4
        	fadeTime=0;
        	try {
    			fadeTime=Double.parseDouble(parsedData[15+6].trim());
            	try {
        			fadeTime=fadeTime+(Double.parseDouble(parsedData[15+5].trim())-Double.parseDouble(parsedData[15+13].trim()));
        			processFade(artist);
            	}catch(Exception e) {
            		processFade(artist);
            	}
        	}catch(Exception e) {
        		fadeTime=(Double.parseDouble(parsedData[15+5].trim())-Double.parseDouble(parsedData[15+13].trim()));
        		processFade(artist);
        	}

	    	//q5
    		try {
    	    	artist_song.set(parsedData[9].trim()+" by " +parsedData[7].trim());
    			db=Double.parseDouble(parsedData[15+5].trim().toString());
    			if(!song_duration.containsKey(artist_song)) {
    				song_duration.put(artist_song, db);
    			}
    		}catch(Exception e) {
    			
    		}

    		
    		
    		//q6
	    	artist_song.set(parsedData[9].trim()+" by " +parsedData[7].trim());    		
	    	try {
		    	db=Double.parseDouble(parsedData[15+4].trim().toString());
		    	try {
			    	db=db+Double.parseDouble(parsedData[15+7].trim().toString());
			    	processEnergy_Danceability(artist_song);
		    	}catch(Exception e) {
			    	processEnergy_Danceability(artist_song);
	        	}

	    	}catch(Exception e) {
		    	try {
			    	db=Double.parseDouble(parsedData[15+7].trim().toString());
			    	processEnergy_Danceability(artist_song);
		    	}catch(Exception ex) {
	        		
	        	}
        	}



    		
	    	
    	}
    }
    
    public void processTopHottness(double hottness, Text allData, String[] parsedData) {
    	
    	//keep list of top 26 hot songs, will remove top song at cleanup. 
    	if(top25HottnessSongs.size()>25) {
    		top25HottnessSongs.sort(Map.Entry.comparingByValue());
    		if(hottness>top25HottnessSongs.get(0).getValue()) {
    			top25HottnessSongs.remove(0);
		    	Map.Entry<Text, Double> newEntry=new Entry<Text, Double>(allData, hottness);
		    	top25HottnessSongs.add(newEntry);
		    	
    		}
    	}
    	else {
	    	Map.Entry<Text, Double> newEntry=new Entry<Text, Double>(allData, hottness);
	    	top25HottnessSongs.add(newEntry);
    	}
    }
   
    public void processFade(Text artist) {
		if(!fade.containsKey(artist)) {
			fade.put(artist,(double) 0);
		}
    	if(fade.containsKey(artist)) {
    		double d=fade.get(artist);
    		fade.put(artist,d+fadeTime);
    	}
    }
    
    public void processEnergy_Danceability(Text artist_song) {
    	if(energetic_danceable.size()>10) {
    		energetic_danceable.sort(Map.Entry.comparingByValue());
    		if(db>energetic_danceable.get(0).getValue()) {
    			energetic_danceable.remove(0);
		    	Map.Entry<Text, Double> newEntry=new Entry<Text, Double>(artist_song, db);
		    	energetic_danceable.add(newEntry);
    		}
    	}
    	else {
	    	Map.Entry<Text, Double> newEntry=new Entry<Text, Double>(artist_song, db);
	    	energetic_danceable.add(newEntry);
    	}
    }
    

   @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
    	
    	//question 1
    	context.write(new Text("Q1:Artist with most songs"), new Text());
    	int maxNum=0;
    	ArrayList<Text> mostSongs=new ArrayList<Text>();
    	
    	for( Map.Entry<Text, Integer> a:artists_songs.entrySet()) {
    		if(a.getValue()>maxNum) {
    			maxNum=a.getValue();
    			mostSongs.clear();
    			mostSongs.add(a.getKey());
    		}	else if(a.getValue()==maxNum) {
    			mostSongs.add(a.getKey());
    		}
    	}
    	for(Text artist:mostSongs) {
    		 context.write(artist,new Text(String.valueOf(maxNum)));
    	}
    	
    	
    	
    	//q2
    	context.write(new Text(), new Text());
    	context.write(new Text("Q2: loudest artist on average"), new Text());
    	double sum=0;
    	double maxValue=-10000000;
    	Text art=new Text();
    	double average=0;
    	for( Map.Entry<Text, ArrayList<Double>> a:loudness.entrySet()) {
  
  
    		
    		sum=0;
    		for(Double d:a.getValue()) {
    			sum+=d;
    		}
    		average=sum/a.getValue().size();
    		if(average>=maxValue) {
    			maxValue=average;
    			art.set(a.getKey());
    		}
    	}	
		context.write(art, new Text(String.valueOf(maxValue)));
		
		
		//q3
    	context.write(new Text(), new Text());
    	context.write(new Text("Q3: highest hottness"), new Text());
    	for(Text best:this.hottness) {
    		context.write(best, new Text(String.valueOf(this.bestHotness)));
    	}
    	
    	
		//q4
    	context.write(new Text(), new Text());
    	context.write(new Text("Q4:  artist with the highest total time spent fading"), new Text());
    	double maxFade=0;
    	ArrayList<Text> artistsWithMaxFadeTime=new ArrayList<Text>();
    	for( Map.Entry<Text, Double> a:fade.entrySet()) {
    		if(a.getValue()>maxFade) {
    			maxFade=a.getValue();
    			artistsWithMaxFadeTime.clear();
    			artistsWithMaxFadeTime.add(a.getKey());
    		}else if(a.getValue()==maxFade) {
    			artistsWithMaxFadeTime.add(a.getKey());
    		}
    	}
    	
    	for(Text t:artistsWithMaxFadeTime) {
    		context.write(t, new Text(String.valueOf(maxFade)));
    	}
    	
    	
    	
		//q5
    	context.write(new Text(), new Text());
    	context.write(new Text("Q5: What is the longest song(s)? The shortest song(s)? The song(s) of median length?"), new Text());

    	ArrayList<Map.Entry<Text, Double>> list = new ArrayList<>(song_duration.entrySet());
        list.sort(Map.Entry.comparingByValue());


        

        
        Map.Entry<Text, Double> min=list.get(0);
    	ArrayList<Map.Entry<Text, Double>> minList=new ArrayList<Map.Entry<Text, Double>> ();
        for(Map.Entry<Text, Double> a:list) {
        	if((double)a.getValue()==(double)min.getValue()) {
        		minList.add(a);
        	}
        	if(a.getValue()>min.getValue()) {
        		break;
        	}
        }
        
      

        Map.Entry<Text, Double> max=list.get(list.size()-1);
    	ArrayList<Map.Entry<Text, Double>> maxList=new ArrayList<Map.Entry<Text, Double>> ();
    	List<Map.Entry<Text, Double>> reversed = ImmutableList.copyOf(list).reverse();
    	
        for(Map.Entry<Text, Double> a:reversed) {
        	if((double)a.getValue()==(double)max.getValue()) {
        		maxList.add(a);
        	}
        	if(a.getValue()<max.getValue()) {
        		break;
        	}
        	
        }
        
        int middle=0;
		if(list.size() % 2 == 0)
		{
			middle=((list.size()/2) + 1);
		}
		else
		{
			middle=((list.size() + 1) / 2);
		}
		
        Map.Entry<Text, Double> median	= list.get(middle);
    	ArrayList<Map.Entry<Text, Double>> medianList=new ArrayList<Map.Entry<Text, Double>> ();
    	int size=0;
    	if(list.size()>250) {
    		size=100;
    	}else {
    		size=1;
    	}
    	for(int i=middle-size;i<middle+size;i++) {
    		if((double)list.get(i).getValue()==(double)median.getValue()) {
    			medianList.add(list.get(i));
    		}
    		if(list.get(i).getValue()>median.getValue()) {
    			break;
    		}
    	}
        
        for(Map.Entry<Text, Double> a:minList) {
            Text minVal=new Text("Minimum Duration:"+a.getKey());
    		context.write(minVal, new Text(String.valueOf(a.getValue())));
        }
        
        for(Map.Entry<Text, Double> a:maxList) {
            Text maxVal=new Text("Maximum Duration:"+a.getKey());
    		context.write(maxVal, new Text(String.valueOf(a.getValue())));
        }
        
        for(Map.Entry<Text, Double> a:medianList) {
            Text medianVal=new Text("Median Duration:"+a.getKey());
    		context.write(medianVal, new Text(String.valueOf(a.getValue())));
        }
    	
    	
    	
    	
    	
    	
    	
		//q6
    	context.write(new Text(), new Text());
    	context.write(new Text("Q6: What are the 10 most energetic and danceable songs? List them in descending order."), new Text());
    	

		energetic_danceable.sort(Map.Entry.comparingByValue());
		reversed = ImmutableList.copyOf(energetic_danceable).reverse();
		for(java.util.Map.Entry<Text, Double> a:reversed) {
			context.write(a.getKey(), new Text(String.valueOf(a.getValue())));
		}
    	
    	
    	
    	


    }
   
   public double processValue(int csvColumn, Text data) {
   	try {
		return 0;// Double.parseDouble(data.toString().split[15+csvColumn].trim());

		
	}catch(Exception e) {
		return 0;
	}	 
   }

}
