package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import cs455.logger.Logger;

public class Sender implements Runnable{
	ByteBuffer buffer;
	
	private static SocketChannel clientSocketChannel;
	private static int messageRate;
	private static SelectionKey key;
	private static Client client;
	private static boolean done=false;
	
	
	public ArrayList<String> hash=new ArrayList<String>();

	private int messagesSent;
	public long executionTime=0;
	public Sender(int messageRate,SocketChannel clientSocketChannel,SelectionKey key, Client client) {
		Sender.messageRate=messageRate;
		Sender.clientSocketChannel=clientSocketChannel;
		Sender.key=key;
		Sender.client=client;
	}
	
	public  synchronized void increment() {
		messagesSent++;
	}
	public synchronized long getSent() {
		return messagesSent;
	}
	
	public synchronized void setSent() {
		messagesSent=0;
	}
	
	public void setDone() {
		done=true;
	}

	@Override
	public void run(){
		ClientDataEvent dataEvent = new ClientDataEvent(this.client,this.clientSocketChannel);
		while(!done) {

			long startTime = System.currentTimeMillis();
		    	byte[] payload=new byte[8192];
		    	Random random = new Random();
		    	random.nextBytes(payload);

		        hash.add(SHA1.SHA1FromBytes(payload));
		              
		        try {
		        	this.increment();
		        	dataEvent.client.send(Sender.clientSocketChannel, this.key,  payload);
		        	
		        	long endtime = System.currentTimeMillis();
		        	executionTime+=(endtime-startTime);
		            Thread.sleep(1000/this.messageRate);
		        } catch (IOException | InterruptedException  e) {
		        	Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
		        }

		        


		}
	}

}
