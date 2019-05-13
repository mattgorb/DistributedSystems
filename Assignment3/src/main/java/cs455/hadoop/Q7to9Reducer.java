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

public class Q7to9Reducer  extends Reducer<Text, Text, Text, Text> {
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
            	if(parsedData.length>5) {
            		combine.set(first+","+value);
            	}
            	else {
                	combine.set(value+","+first);
            	}
            }
            i+=1;
    	}

    	
    	if(combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).length>=10) {
    		
    		
    		//q1
    		parsedData=combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    		Text artist=new Text();

    		//q7
    		try {
		    	segmentData=parsedData[5].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=startTime.size()) {
		    				startTime.add(db);
		    			}
		    			last=startTime.get(i);
		    			startTime.set(i,last+db);
		    			
		    			if(i>=startTimeCount.size()) {
		    				startTimeCount.add((double)0);
		    			}
		    			last=startTimeCount.get(i);
		    			startTimeCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}
	    	
	    	try {
		    	segmentData=parsedData[6].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=pitch.size()) {
		    				pitch.add(db);
		    			}
		    			last=pitch.get(i);
		    			pitch.set(i,last+db);
		    			
		    			if(i>=pitchCount.size()) {
		    				pitchCount.add((double)0);
		    			}
		    			last=pitchCount.get(i);
		    			pitchCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}
	    	
	    	try {
		    	segmentData=parsedData[7].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=timbre.size()) {
		    				timbre.add(db);
		    			}
		    			last=timbre.get(i);
		    			timbre.set(i,last+db);
		    			
		    			if(i>=timbreCount.size()) {
		    				timbreCount.add((double)0);
		    			}
		    			last=timbreCount.get(i);
		    			timbreCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}
	    	
	    	try {
		    	segmentData=parsedData[8].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=maxLoudness.size()) {
		    				maxLoudness.add(db);
		    			}
		    			last=maxLoudness.get(i);
		    			maxLoudness.set(i,last+db);
		    			
		    			if(i>=maxLoudnessCount.size()) {
		    				maxLoudnessCount.add((double)0);
		    			}
		    			last=maxLoudnessCount.get(i);
		    			maxLoudnessCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}

	    	
	    	try {
		    	segmentData=parsedData[9].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=maxLoudTime.size()) {
		    				maxLoudTime.add(db);
		    			}
		    			last=maxLoudTime.get(i);
		    			maxLoudTime.set(i,last+db);
		    			
		    			if(i>=maxLoudTimeCount.size()) {
		    				maxLoudTimeCount.add((double)0);
		    			}
		    			last=maxLoudTimeCount.get(i);
		    			maxLoudTimeCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}
	    	
	    	
	    	try {
		    	segmentData=parsedData[10].trim().toString().split(" ");
		    	for( i=0;i<segmentData.length;i++) {
		    		try {
		    			db=Double.parseDouble(segmentData[i]);
		    			if(i>=startLoudness.size()) {
		    				startLoudness.add(db);
		    			}
		    			last=startLoudness.get(i);
		    			startLoudness.set(i,last+db);
		    			
		    			if(i>=startLoudnessCount.size()) {
		    				startLoudnessCount.add((double)0);
		    			}
		    			last=startLoudnessCount.get(i);
		    			startLoudnessCount.set(i,last+1);

		    		}catch(Exception e) {}
		    	}
	    	}catch(Exception e) {}
	    	
	    	
	    	//q8
    		parsedData=combine.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    		artist.set(parsedData[2]);    
    		
    		try {
    			db=Double.parseDouble(parsedData[4]);
    	    	if(!artistsList_tempo.containsKey(artist)){
    	    		artistsList_tempo.put(artist, db);
    	    		artistsList_tempoCount.put(artist, 1);
       	    		//context.write(new Text(artist), new Text(String.valueOf(db)));
    	    	}else{
    	    		db=artistsList_tempo.get(artist)+db;
    	    		artistsList_tempo.put(artist, db);
 
    	    		db=artistsList_tempoCount.get(artist)+db;
    	    		artistsList_tempoCount.put(artist, artistsList_tempoCount.get(artist)+1);
    	    	}
    	    	//context.write(new Text(String.valueOf(artistsList_tempo.containsKey(artist_))), new Text(artist_));

    	    	tempoAll+=db;
    	    	tempoAllCount+=1;
    		}catch(Exception e) {}
    		



    		
	    	segmentData=parsedData[3].trim().toString().split(" ");
	    	for( i=0;i<segmentData.length;i++) {
	    		Text term=new Text();
	    		try {
	    			term.set(segmentData[i].trim());
	    			if(!termCountAll.containsKey(term)) {
	    				termCountAll.put(term, 1);
	    			}else{
	    				termCountAll.put(term, termCountAll.get(term)+1);
	    			}
	    			
	    			if(!termCountArtist.containsKey(artist)) {
	    				termCountArtist.put(artist, new HashMap(){{put(term,1);}});
	    			}else{
	    				if(termCountArtist.get(artist).containsKey(term)) {
	    					termCountArtist.get(artist).put(term, termCountArtist.get(artist).get(term)+1);
	    				}
	    				else {
	    					termCountArtist.get(artist).put(term, 1);
	    				}
	    			}

	    		}catch(Exception e) {}
	    	}
	    	

    		
    		
	    	
	    	//q9
        	totals=0;
        	try {
        		totals=Double.parseDouble(parsedData[0].trim());

        		if(!artistsList.containsKey(parsedData[1].trim())) {
        			artistsList.put(new Text(parsedData[1].trim()),new Text(parsedData[7].trim()));
        		}
        		processTopHottness(totals, combine, parsedData);
        		
        	}catch(Exception e) {

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
  
    	
    	
		//q7
    	/*context.write(new Text(), new Text());
    	context.write(new Text("Q7: Create segment data for the average song. Include start time, pitch, timbre, max loudness,\n" + 
    			"max loudness time, and start loudness. "), new Text());

    	
    	//context.write(new Text("asdf"), new Text(String.valueOf(this.startTime.size())));
    	Text startSegment=new Text();
    	String d;
    	final DecimalFormat df2 = new DecimalFormat( "#.00" );
    	for(int i=0;i<this.startTime.size();i++) {
    		double s=(this.startTime.get(i)/this.startTimeCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Start Segment:"), startSegment);
    	
    	startSegment.set("");
    	for(int i=0;i<this.pitch.size();i++) {
    		double s=(this.pitch.get(i)/this.pitchCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Segment Pitch:"), startSegment);
    	
    	
    	startSegment.set("");
    	for(int i=0;i<this.timbre.size();i++) {
    		double s=(this.timbre.get(i)/this.timbreCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Segment Timbre:"), startSegment);
    	
    	startSegment.set("");
    	for(int i=0;i<this.maxLoudness.size();i++) {
    		double s=(this.maxLoudness.get(i)/this.maxLoudnessCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Segment Max Loudness:"), startSegment);
    	
    	startSegment.set("");
    	for(int i=0;i<this.maxLoudTime.size();i++) {
    		double s=(this.maxLoudTime.get(i)/this.maxLoudTimeCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Segment Max Loudness Time:"), startSegment);
    	
    	startSegment.set("");
    	for(int i=0;i<this.startLoudness.size();i++) {
    		double s=(this.startLoudness.get(i)/this.startLoudnessCount.get(i));
    		d=df2.format(s);
    		startSegment.set(startSegment+" "+ new Text(d));;
    	}
    	context.write(new Text("Average Segment Start Loudness:"), startSegment);
    	

    	
    	
		//q8
    	context.write(new Text(), new Text());
    	context.write(new Text("Q8: Which artist is the most generic? Which artist is the most unique?"), new Text());
    	


            
            
            ArrayList<Map.Entry<Text, Integer>> sortTop = new ArrayList<>();
            ArrayList<Map.Entry<Text, Integer>> sortBottom = new ArrayList<>();

            for(Map.Entry<Text,Integer> t:termCountAll.entrySet()) {
            	sortTop.sort(Map.Entry.comparingByValue());
            	sortBottom.sort(Map.Entry.comparingByValue());
            	
            	if(sortTop.size()>20) {
            		if(t.getValue()>sortTop.get(0).getValue()) {
            			sortTop.remove(0);
            			sortTop.add(t);
            		}
            		
            	}else {
            		sortTop.add(t);
            	}
            	if(sortBottom.size()>20) {
            		if(t.getValue()<sortBottom.get(sortBottom.size()-1).getValue()) {
            			sortBottom.remove(sortBottom.size()-1);
            			sortBottom.add(t);
            		}
            	}else {
            		sortBottom.add(t);
            	}       	
            }    
            
            
            ArrayList<Text> top=new ArrayList<Text>();
            context.write(new Text("Top 20 Most Generic Terms:"), new Text());
            for(Map.Entry<Text,Integer> t:sortTop) {
            	top.add(t.getKey());
            	context.write(new Text("\t"+t.getKey()), new Text());
            }
            context.write(new Text("Top 20 Most Unique Terms:"), new Text());        
            ArrayList<Text> bottom=new ArrayList<Text>();
            for(Map.Entry<Text,Integer> t:sortBottom) {
            	bottom.add(t.getKey());
            	context.write(new Text("\t"+t.getKey()), new Text());
            }       

            
           	context.write(new Text(), new Text());
            Map<Text, Integer> artistsTopTerms = new HashMap<Text, Integer>();
            Map<Text, Integer> artistsBottomTerms = new HashMap<Text, Integer>();
            for(Map.Entry<Text,Map<Text, Integer>> t:termCountArtist.entrySet()) {
            	
            	artistsTopTerms.put(t.getKey(),0);
            	artistsBottomTerms.put(t.getKey(),0);
            	for(Map.Entry<Text, Integer> b: t.getValue().entrySet()) {
            		if(top.contains(b.getKey())) {
            			artistsTopTerms.put(t.getKey(),artistsTopTerms.get(t.getKey())+1);
            		}
            		if(bottom.contains(b.getKey())) {
            			artistsBottomTerms.put(t.getKey(),artistsBottomTerms.get(t.getKey())+1);
            		}
            	}
            
            }
            
            
            
            int generic=0;
            int unique=0;
            ArrayList<Text> MostGeneric = new ArrayList<>();
            ArrayList<Text> MostUnique = new ArrayList<>();
            
            for(Map.Entry<Text, Integer> t:artistsTopTerms.entrySet()) {
            	if(t.getValue()>generic) {
            		MostGeneric.clear();
            		MostGeneric.add(t.getKey());
            		generic=t.getValue();
            	}else if(t.getValue()==generic) {
            		MostGeneric.add(t.getKey());
            	}
            	//context.write(t.getKey(), new Text(String.valueOf(t.getValue())));
            }
            for(Map.Entry<Text, Integer> t:artistsBottomTerms.entrySet()) {
            	if(t.getValue()>unique) {
            		MostUnique.clear();
            		MostUnique.add(t.getKey());
            		unique=t.getValue();
            	}else if(t.getValue()==unique) {
            		MostUnique.add(t.getKey());
            	}
            }
            
            context.write(new Text("Most Generic Artists:"), new Text("Amount of Generic Terms: "+Integer.toString(generic)));
            for(Text t:MostGeneric) {
            	context.write(t, new Text());
            }
            context.write(new Text("Most Unique Artists:"), new Text("Amount of Unique Terms: "+Integer.toString(unique)));
            for(Text t:MostUnique) {
            	context.write(t, new Text());
            } 	
    	
    	
    	
    	*/
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
		//q9
    	context.write(new Text(), new Text());
    	context.write(new Text("Q9: Imagine a song with a higher hotttnesss score than the song in your answer to Q3. List this\n" + 
    			"song’s tempo, time signature, danceability, duration, mode, energy, key, loudness, when it\n" + 
    			"stops fading in, when it starts fading out, and which terms describe the artist who made it.\n" + 
    			"Give both the song and the artist who made it unique names."), new Text());

    	Map<Text, Text> artistsList_revised=new HashMap<Text,Text>();
    	
    	for(Map.Entry<Text, Text> a:artistsList.entrySet()) {
    		char[] c=a.getKey().toString().toCharArray();
    		if(c[0]== 'b' && c[1]== '\'' &&c[c.length-1]== '\'') {
    			artist_.set(a.getKey().toString().substring(2, a.getKey().toString().length()-1));
            	artistsList_revised.put(artist_, a.getValue());
    		}else {
    			artist_.set(a.getKey());
            	artistsList_revised.put(artist_, a.getValue());
    		}

    	}
    	
    	

    	

    	ArrayList<Text> similarArtistsList=new ArrayList<Text>();
    	for(Map.Entry<Text, Double> song:top25HottnessSongs) {
        	parsedData=song.getKey().toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); 
        	String[] x=parsedData[10].split(" ");
        	for(int i=0;i<x.length;i++) {
        		
        		artist_.set(x[i].trim());
        		if(artistsList_revised.containsKey(artist_)) {
        			similarArtistsList.add(artistsList_revised.get(artist_));
        		}
        	}
    	}
    	

        Map<Text, Integer> similar = new HashMap<Text, Integer>();       
        for (Text i : similarArtistsList) { 
            Integer j = similar.get(i); 
            similar.put(i, (j == null) ? 1 : j + 1); 
        } 
  

    	
    	
    	
    	top25HottnessSongs.sort(Map.Entry.comparingByValue());
    	
    	Map.Entry<Text, Double> topHotSong=top25HottnessSongs.get(top25HottnessSongs.size()-1);
    	parsedData=topHotSong.getKey().toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

    	context.write(new Text("TOP SONG INFO: "), new Text(parsedData[9] +" by "+parsedData[7]));
    	context.write(new Text("\tTempo: "),new Text(parsedData[15+14]));
    	context.write(new Text("\tTime Signature: "),new Text(parsedData[15+15]));
    	context.write(new Text("\tDanceability: "),new Text(parsedData[15+4]));
    	context.write(new Text("\tDuration: "),new Text(parsedData[15+5]));
    	context.write(new Text("\tMode: "),new Text(parsedData[15+11]));
    	context.write(new Text("\tEnergy: "),new Text(parsedData[15+7]));
    	context.write(new Text("\tKey: "),new Text(parsedData[15+8]));
    	context.write(new Text("\tLoudness: "),new Text(parsedData[15+10]));
    	context.write(new Text("\tEnd Fade In: "),new Text(parsedData[15+6]));
    	context.write(new Text("\tStart Fade Out: "),new Text(parsedData[15+13]));
    	context.write(new Text("\tSimilar Artists: "),new Text(parsedData[10]));

    	
    	//top25HottnessSongs.remove(top25HottnessSongs.size()-1);
    	double tempo=0;
    	ArrayList<Double> timSig=new ArrayList<Double>();
    	double danceability=0;
    	double duration=0;
    	ArrayList<Double> mode=new ArrayList<Double>();
    	double energy=0;
    	ArrayList<Double> key=new ArrayList<Double>();
    	double loudness=0;
    	double fadeIn=0;
    	double fadeOut=0;
    	for(Map.Entry<Text, Double> song:top25HottnessSongs) {
    		parsedData=song.getKey().toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    		tempo+=Double.parseDouble(parsedData[15+14]);
    		timSig.add(Double.parseDouble(parsedData[15+15]));
    		danceability+=Double.parseDouble(parsedData[15+4]);
    		duration+=Double.parseDouble(parsedData[15+5]);
    		mode.add(Double.parseDouble(parsedData[15+11]));
    		energy+=Double.parseDouble(parsedData[15+7]);
    		key.add(Double.parseDouble(parsedData[15+8]));
    		loudness+=Double.parseDouble(parsedData[15+10]);
    		fadeIn+=Double.parseDouble(parsedData[15+6]);
    		fadeOut+=Double.parseDouble(parsedData[15+13]);
    	}
    	context.write(new Text(), new Text());
    	context.write(new Text("Higher Hotness:"), new Text());
    	tempo=tempo/top25HottnessSongs.size();
    	danceability=danceability/top25HottnessSongs.size();
    	duration=duration/top25HottnessSongs.size();
    	energy=energy/top25HottnessSongs.size();
    	loudness=loudness/top25HottnessSongs.size();
    	fadeIn=fadeIn/top25HottnessSongs.size();
    	fadeOut=fadeOut/top25HottnessSongs.size();
    	
    	context.write(new Text("Careless Girl by Four Track"), new Text(""));
    	context.write(new Text("\tTempo: "),new Text(String.valueOf(tempo)));
    	//context.write(new Text("\tTime Signature: "),new Text(String.valueOf(tempo)));
    	context.write(new Text("\tDanceability: "),new Text(String.valueOf(danceability)));
    	context.write(new Text("\tDuration: "),new Text(String.valueOf(duration)));
    	//context.write(new Text("\tMode: "),new Text(String.valueOf(tempo)));
    	context.write(new Text("\tEnergy: "),new Text(String.valueOf(energy)));
    	//context.write(new Text("\tKey: "),new Text(String.valueOf(tempo)));
    	context.write(new Text("\tLoudness: "),new Text(String.valueOf(loudness)));
    	context.write(new Text("\tEnd Fade In: "),new Text(String.valueOf(fadeIn)));
    	context.write(new Text("\tStart Fade Out: "),new Text(String.valueOf(fadeOut)));
    	//context.write(new Text("\tSimilar Artists: "),new Text(String.valueOf(tempo)));
    	
    	
    	
        Map<Double, Integer> timSigCounts = new HashMap<Double, Integer>();       
        for (Double i : timSig) { 
            Integer j = timSigCounts.get(i); 
            timSigCounts.put(i, (j == null) ? 1 : j + 1); 
        } 
        
        
        int timeSigHigh = 0;
        double timeSigOut=0;
        for (Map.Entry<Double, Integer> val : timSigCounts.entrySet()) { 
        	if ((int)val.getValue()>timeSigHigh) {
        		timeSigHigh=val.getValue();
        		timeSigOut=val.getKey();
        	}
        }
        context.write(new Text("\tTime Signature"), new Text(String.valueOf(timeSigOut)));
        
        

        
        
        Map<Double, Integer> modeCounts = new HashMap<Double, Integer>();       
        for (Double i : mode) { 
            Integer j = modeCounts.get(i); 
            modeCounts.put(i, (j == null) ? 1 : j + 1); 
        } 
        
        int modeHigh = 0;
        double modeOut=0;
        for (Map.Entry<Double, Integer> val : modeCounts.entrySet()) { 
        	if ((int)val.getValue()>modeHigh) {
        		modeHigh=val.getValue();
        		modeOut=val.getKey();
        	}
        }
        context.write(new Text("\tMode"), new Text(String.valueOf(modeOut)));
        
        Map<Double, Integer> keyCounts = new HashMap<Double, Integer>();       
        for (Double i : key) { 
            Integer j = keyCounts.get(i); 
            keyCounts.put(i, (j == null) ? 1 : j + 1); 
        } 
  
        
        int keyHigh = 0;
        double keyOut=0;
        for (Map.Entry<Double, Integer> val : keyCounts.entrySet()) { 
        	if ((int)val.getValue()>keyHigh) {
        		keyHigh=val.getValue();
        		keyOut=val.getKey();
        	}
        }
        context.write(new Text("\tKey"), new Text(String.valueOf(keyOut)));
    	

        

        for (Map.Entry<Text, Integer> val : similar.entrySet()) { 
        	if ((int)val.getValue()>1) {
                context.write(new Text("\tSimilar Artist:"),new Text(val.getKey().toString()));
        	}
        }
        
        
    	context.write(new Text(), new Text());
    	context.write(new Text(), new Text());
        
        //q10
      /*  Map<Integer, String> yearTerms=new HashMap<Integer, String>();
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
        	
*/
   

    }
   
   public double processValue(int csvColumn, Text data) {
   	try {
		return 0;// Double.parseDouble(data.toString().split[15+csvColumn].trim());

		
	}catch(Exception e) {
		return 0;
	}	 
   }

}
