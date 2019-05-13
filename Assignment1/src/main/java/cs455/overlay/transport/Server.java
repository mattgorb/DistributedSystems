package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cs455.logger.Logger;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.EventFactory;

public class Server extends Thread implements Runnable {
	private ServerSocket serverSocket;
	public String ip;
	public int port;
	public Node node;
	

	private Receiver receiver;
	private Thread receiverThread;
	
	private EventFactory ef;
	public Socket socket =null;
	
	public Map<String,Socket> originatingAddress;

	private volatile boolean done = false;
	
	public void setDone()
	{
		done = true;
	}

	//This constructor is for messaging nodes where port number doesn't matter.  
	public Server(Node node, EventFactory event) {
		try {
			this.node=node;
			this.ef=event;
			int connectAtRandomPort = (int)(Math.random() * 65000 + 1);
			serverSocket = connectToServer(connectAtRandomPort);
			
			this.ip=InetAddress.getLocalHost().getHostAddress();
			this.port=serverSocket.getLocalPort();
			
			this.originatingAddress=new HashMap<String,Socket>();
			

			
			System.out.println("Listening at IP: " + this.ip+" on port "+ serverSocket.getLocalPort());				
			
		} catch (UnknownHostException e) {
  			Logger.write_errors(getClass().getName(),"Server", e.getClass().toString(), e);
		}
		
	}	
	
	public Server(Node node,int portNumber, EventFactory event) {
		try {
			this.ef=event;
			this.node=node;

			serverSocket = connectToServer(portNumber);
			this.originatingAddress=new HashMap<String,Socket>();


			System.out.println("Listening at IP: " + InetAddress.getLocalHost().getHostAddress()+" on port: "+ serverSocket.getLocalPort());		

		} catch (UnknownHostException e) {
  			Logger.write_errors(getClass().getName(),"Server", e.getClass().toString(), e);
		} 
	}



	
	public  ServerSocket connectToServer(int portNum) {

		for(int i=portNum;i<portNum+50;i++) {
			try {
				ServerSocket serverSocket = new ServerSocket(i);
				this.ip=InetAddress.getLocalHost().getHostAddress();
				this.port=serverSocket.getLocalPort();
				return serverSocket;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	


	
	
	
	@Override
	public   void run() {

            while (Thread.currentThread().isAlive()) {
	            	try {

	            			//Block on accepting connections. Once it has received a connection it will return a socket for us to use.
	            			this.socket = serverSocket.accept();

	            			
		    				//need this to check whether the originating address is equal to the string ip they passed
	            			synchronized (this.socket) {
			    				this.originatingAddress.put(this.socket.getRemoteSocketAddress().toString().substring(1).split(":")[0],this.socket);
			    				if(this.node.socketInList(this.socket.getInetAddress().toString().substring(1),this.socket.getPort())==null) {
				    				ClientSocket so= new ClientSocket(this.socket.getInetAddress().toString().substring(1),this.socket.getPort(),this.socket);
				    				this.node.addSender(so);//socket.getInetAddress().toString().substring(1)+":"+socket.getPort(),socket);
				    				this.node.setCurrent(so);

			    				}
			    				else {
			    					this.node.setCurrent(this.node.socketInList(this.socket.getInetAddress().toString().substring(1),this.socket.getPort()));
			    				}
			    				
			    				
	            			}

		    					
							receiver = new Receiver(this.socket);
							receiverThread = new Thread(receiver);
							receiverThread.start();
							receiver.setEventFactory(ef);
							
							

	            	} 
	            	  catch (SocketException e) {
	          				Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            			break;
	            	} catch (IOException e) {
	        				Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
	            			break;

					} catch (Exception e) {
						Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
					}    			
            }

	}

}

