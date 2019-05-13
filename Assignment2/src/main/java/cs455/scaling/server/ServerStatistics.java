package cs455.scaling.server;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStatistics implements Runnable{
	public Server server;
	public ClientTracker clientTracker;
	private static DecimalFormat df2 = new DecimalFormat(".##");
	
	private static boolean done=false;
	public ServerStatistics(Server server, ClientTracker clientTracker) {
		this.server=server;
		this.clientTracker=clientTracker;
		df2.setRoundingMode(RoundingMode.UP);
	}
	
	public void setDone() {
		done=true;
	}

	@Override
	public void run() {

		while(!done) {
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String time=LocalDateTime.now().toString();
			
			double totalStd=0;
			double totalMean=0;
			int clientsConnected=0;
			for (Entry<String, CopyOnWriteArrayList<Integer>> entry : this.clientTracker.per_second_tracker.entrySet()) {
				double clientSum=0;
				for(int i: entry.getValue()) {
					clientSum=clientSum+i;
					//System.out.println(i);
				}
				
				if(entry.getValue().size()>0) {
					
					clientsConnected++;
				}
				
				double mean=clientSum/entry.getValue().size();
				//System.out.println(entry.getValue().size());
				
				
				double std=0;
				for(int i: entry.getValue()) {
					std=std+Math.pow(((double)i-mean),2);
				}				
				std=Math.sqrt(std/entry.getValue().size());
				totalStd=totalStd+std;
				totalMean=totalMean+mean;

				entry.getValue().clear();
			}
			
			totalStd=totalStd/this.clientTracker.per_second_tracker.size();
			totalMean=totalMean/this.clientTracker.per_second_tracker.size();	
			
			String std_formatted=df2.format(totalStd);
			String mean_formatted=df2.format(totalMean);
			
			
			double sent=this.server.messagesSent/20;
			
			System.out.println("["+time+"]:"+ " Server Throughput: "+sent+" (messages/second),"+
					" Active Connections: "+clientsConnected+
					" Mean Per-Client Throughput: " +mean_formatted+" (messages/second),"+
					" Std. Dev. of Per-Client Throughput: "+std_formatted
					);

						
			this.server.setSent();
			this.server.messagesReceived=0;

			this.clientTracker.empty();
		}
	}

}
