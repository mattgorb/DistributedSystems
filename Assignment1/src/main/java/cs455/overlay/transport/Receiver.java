package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cs455.logger.Logger;
import cs455.overlay.wireformats.EventFactory;

public class Receiver extends Thread implements Runnable{

	private Socket incomingConnectionSocket;
	private DataInputStream inputStream;
	
	private static EventFactory factory;
	
	private volatile boolean done = false;
	
	public void setDone()
	{
		done = true;
	}
	
	public Receiver(Socket socket) throws SocketException {
			this.incomingConnectionSocket=socket;

	}
	
	public void setEventFactory(EventFactory event) {
		factory=event;
	}

	
	@Override
	public  void run() {
			byte[] data = null;
            while (true) {
	            	try {
	            			inputStream = new DataInputStream(incomingConnectionSocket.getInputStream());
		    				int dataLength = inputStream.readInt();

		    				
	            			data = new byte[dataLength];
	            			inputStream.readFully(data, 0, dataLength);

		    				factory.fireMessage(data);	            				

	            	} 
	            	catch(EOFException e) {
	            		//Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            		break;
	            		
	            	}
	            	  catch (SocketException e) {
	            		  	Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            			break;
	            	} catch (IOException e) {
	            			Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            			break;

					}catch(NullPointerException e) {
						Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            		break;
	            	} 
	            	catch (Exception e) {
	            		Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
						break;
					}    		
	            	
            }

	}
}






