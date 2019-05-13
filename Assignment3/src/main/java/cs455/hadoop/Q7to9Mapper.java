package cs455.hadoop;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
public class Q7to9Mapper extends Mapper<LongWritable, Text, Text, Text> {
	private Text data = new Text();
	private Text song = new Text();
	private Text segment=new Text();
	int j=0;
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
       	segment.set("");
        String line = value.toString();
        String[] parsedData=line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if(parsedData[1].equals("song_id")||parsedData[1].equals("artist_familiarity")) {
        	return;
        }
        if(parsedData.length>20) {
        	song.set(parsedData[1].trim());
        	data.set(parsedData[2]+","+parsedData[14]+","+parsedData[15]+","+parsedData[4]+","+parsedData[5]+","+parsedData[11]
        			+","+parsedData[7]+","+parsedData[8]+","+parsedData[10]+","+parsedData[6]+","+parsedData[13]);
        	context.write(song,data);
        }else if(parsedData.length>9){
	       	 song.set(parsedData[8].trim());
	       	/* String[] segmentData=parsedData[11].trim().toString().split(" ");
	       	 if(segmentData.length>5) {
	       		 j=5;
	       	 }else {
	       		 j=segmentData.length;
	       	 }
	       	 for(int i=0;i<j;i++) {
	       		 if(i==4) {
	       			segment.set(segment+segmentData[i]);
	       		 }else {
	       			segment.set(segment+segmentData[i]+" ");
	       		 }
	       	 }
	       	 data.set(parsedData[14]+","+segment);*/
	       	 
	        data.set(parsedData[3]+","+parsedData[7]+","+parsedData[9]+","+parsedData[10]);
	       	 context.write(song,data);

        }

    }
}