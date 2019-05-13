package cs455.scaling.client;

import java.time.LocalDateTime;

import cs455.logger.Logger;

public class ClientStatistics implements Runnable{
	public Sender sender;
	public Client client;
	private static boolean done=false;
	
	
	public ClientStatistics(Sender sender, Client client) {
		this.sender=sender;
		this.client=client;
	}
	
	public void setDone() {
		done=true;
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!done) {
			try {
				Thread.sleep(20000);
				//Thread.sleep(sender.executionTime);
			} catch (InterruptedException e) {
				Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
			}
			String time=LocalDateTime.now().toString();


			long sent=sender.getSent();
			
			System.out.println("["+time+"]:"+ " Total Sent Count: "+sent+" Total Received Count: "+client.messagesReceived);
			//client.messagesReceived.set(0);
			Client.messagesReceived=0;
			sender.executionTime=0;
			sender.setSent();

		}
	}

	
}
