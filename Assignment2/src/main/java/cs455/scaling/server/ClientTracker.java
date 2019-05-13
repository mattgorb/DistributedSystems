package cs455.scaling.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientTracker implements Runnable{
	public Server server;
	private static boolean done=false;
	public ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> per_second_tracker=new ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>>();
	public ClientTracker(Server server) {
		this.server=server;
	}
	public void setDone() {
		done=true;
	}
	
	public void empty() {
		this.per_second_tracker=new ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>>();
	}
	
	@Override
	public synchronized void run() {
		while(!done) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (Entry<String, Integer> entry : this.server.clientTracker.entrySet()) {
				if(this.per_second_tracker.get(entry.getKey())==null) {
					this.per_second_tracker.put(entry.getKey(),new CopyOnWriteArrayList<Integer>());
				}
				CopyOnWriteArrayList<Integer> values=this.per_second_tracker.get(entry.getKey());
				//if(entry.getValue()>0) {
					values.add(entry.getValue());
				//}
				
				entry.setValue(0);
			}
		}
	}
}