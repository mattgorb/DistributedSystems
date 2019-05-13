package cs455.hadoop;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
public class JobOneMapper extends Mapper<LongWritable, Text, Text, Text> {
	private Text data = new Text();
	private Text song = new Text();
	
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] parsedData=line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if(parsedData[1].equals("song_id")||parsedData[1].equals("artist_familiarity")) {
        	return;
        }
        if(parsedData.length>20) {
        	song.set(parsedData[1].trim());
        	data.set(line);
        	context.write(song,data);
        }else if(parsedData.length>9){
	       	 song.set(parsedData[8].trim());
	       	 data.set(line);
	       	 context.write(song,data);
        }

    }
}
