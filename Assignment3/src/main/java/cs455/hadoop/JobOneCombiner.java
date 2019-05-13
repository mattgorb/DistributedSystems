package cs455.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.collect.Iterables;

public class JobOneCombiner  extends Reducer<Text, Text, Text, Text> {
	int i=0;
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    	Text combine=new Text();
    	Text first=new Text();

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
    	
        context.write(key, combine);
    }
}
