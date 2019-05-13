package cs455.scaling.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cs455.logger.Logger;




public class Client implements Runnable{
	
	public InetAddress serverIp;
	public int serverPort;
	
	Selector selector; 
	//ByteBuffer buffer;
	private ByteBuffer cliBuffer = ByteBuffer.allocate(8000);
	//private List pendingChanges = new LinkedList();
	private Map pendingData = new HashMap();
	private static int rate;
	
	private Sender sender;
	private ClientStatistics stats;
	
	// Maps a SocketChannel to a RspHandler
	private Map<SocketChannel, ResponseHandler> responseHandlers = Collections.synchronizedMap(new HashMap());
	private ArrayList<String> messageList=new ArrayList<String>();
	private static SocketChannel clientSocketChannel;
	
	//public static AtomicInteger messagesReceived=new AtomicInteger(0);
	public static int messagesReceived=0;
	
	public ArrayList<String> hash=new ArrayList<String>();
	
	
	public Client(String ip, int port,int messageRate) {


		try {
			this.serverIp=InetAddress.getByName(ip);
			//System.out.println(this.serverIp);
			this.serverPort=port;
			this.rate=messageRate;
			
			this.selector=SelectorProvider.provider().openSelector();
			
			this.openSocketConnection();

			
		} catch (UnknownHostException e) {
			Logger.write_errors(getClass().getName(),"Client", e.getClass().toString(), e);
		}catch (IOException e) {
			Logger.write_errors(getClass().getName(),"Client", e.getClass().toString(), e);
		}


	}
	
	
	
	
	
	
	public void openSocketConnection() {
		try {
			//Client.clientSocketChannel=SocketChannel.open(new InetSocketAddress(this.serverIp, this.serverPort));
			
			Client.clientSocketChannel=SocketChannel.open();
			Client.clientSocketChannel.configureBlocking(false);
			Client.clientSocketChannel.connect(new InetSocketAddress(this.serverIp, this.serverPort));
			Client.clientSocketChannel.register(this.selector, SelectionKey.OP_CONNECT);
			

		} catch (IOException e) {
			Logger.write_errors(getClass().getName(),"openSocketConnection", e.getClass().toString(), e);
		}
	}
	
	public void send(SocketChannel socket, SelectionKey key, byte[] data) throws IOException {
		// Register the response handler
		this.responseHandlers.put(socket, new ResponseHandler());
		
		// And queue the data we want written
		synchronized (this.pendingData) {
			
			this.messageList.add(SHA1.SHA1FromBytes(data));
			List queue = (List) this.pendingData.get(socket);
			if (queue == null) {
				queue = new ArrayList();
				this.pendingData.put(socket, queue);
			}
			queue.add(ByteBuffer.wrap(data));
		}

		this.write(key);
	}
	
	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		this.cliBuffer.clear();

		int numRead;
		try {
			numRead = socketChannel.read(this.cliBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}


		Client.messagesReceived++;
		
		//Client.messagesReceived.incrementAndGet();
		
		this.handleResponse(socketChannel, this.cliBuffer.array(), numRead);
		this.cliBuffer.clear();
	}
	
	private void handleResponse(SocketChannel socketChannel, byte[] data, int numRead) throws IOException {
		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);
		
		// Look up the handler for this channel
		ResponseHandler handler = (ResponseHandler) this.responseHandlers.get(socketChannel);
		handler.handleResponse(this.sender,rspData);
		
		
		
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {

				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
	
		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
			// Register an interest in writing on this channel

			
		} catch (IOException e) {
			Logger.write_errors(getClass().getName(),"finishConnection", e.getClass().toString(), e);
			return;
		}
		
		

    	

    	
		this.sender=new Sender(this.rate, this.clientSocketChannel,key,this);
		Thread t=(new Thread(sender));
    	t.start();
    	
    	this.stats=new ClientStatistics(this.sender,this);
    	Thread statsThread=(new Thread(stats));
    	statsThread.start();
    	
    	key.interestOps(SelectionKey.OP_READ);

	}

	
	
	
	
	
	public static void main(String[] args) {
		
		if(args.length!=3) {
			System.out.println("Need 2 arguments.");
			System.exit(1);
		}
		
		Client cli=new Client(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]));
		Thread t=new Thread(cli);
		t.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	System.out.println("Running shutdown hook, closing threads...");
		        cli.sender.setDone();

		        for(ResponseHandler h: cli.responseHandlers.values()) {
		        	h.setDone();
		        }
		        
		        cli.stats.setDone();
		    }
		});
	}

@Override
public void run() {
	while(true) {
		
		  try {
			this.selector.select();

			  Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
			  while(selectedKeys.hasNext()) {
				SelectionKey key = (SelectionKey) selectedKeys.next();
				selectedKeys.remove();
	
				if (!key.isValid()) {
					continue;
				}
	
			    if (key.isConnectable()) {
						this.finishConnection(key);
			    	
			    	
			    } else if (key.isReadable()) {

			    	this.read(key);
	
			    } else if (key.isWritable()) {

			    	this.write(key);
			    }
	
			  }
		}
		 catch (Exception e1) {
				Logger.write_errors(getClass().getName(),"run", e1.getClass().toString(), e1);
		}
	
	}
	}

}
