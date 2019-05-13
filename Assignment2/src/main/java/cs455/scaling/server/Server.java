package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cs455.logger.Logger;

public class Server implements Runnable
{
	private InetAddress ip;
	private int port;
	public Selector selector;
	ServerSocketChannel serverSocketChannel ;//= ServerSocketChannel.open();
	//public WorkerThread worker;
	String sha1;
	
	public int numConnections=0;
	public int messagesSent=0;
	
	public int messagesReceived=0;
	
	//private ByteBuffer = ByteBuffer.allocate(8000);
	private static ThreadPoolManager threadPoolManager;
	
	public ConcurrentHashMap<String, Integer> clientTracker=new ConcurrentHashMap<String,Integer>();

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	public Map pendingData = new HashMap();
	
	public HashMap<SelectionKey, Boolean> keys = new HashMap<SelectionKey, Boolean>();
	public Server(int port)
	{
		try {
			this.ip= InetAddress.getLocalHost();
			//this.worker=worker;
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.port=port;
		
		System.out.println("Listening at IP: " + this.ip.getHostAddress()+" on port "+ port);				
		Initialize();
	}
	
	
	

	

	private void read(SelectionKey key) throws IOException {

		SocketChannel socketChannel = (SocketChannel) key.channel();
		//System.out.println("ADDING READ");
		Task task = new Task(socketChannel,key,this,null,0);		
		if(keys.containsKey(key))
		{
			if(!keys.get(key))
			{
				
				try {
					keys.put(key, true);
					
					threadPoolManager.addRead(task);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			try {
				keys.put(key, true);
				threadPoolManager.addRead(task);
			} catch (Exception e) {
				Logger.write_errors(getClass().getName(),"read", e.getClass().toString(), e);
			}
		}
	}
	public synchronized  void incrementSent(String key) {
		this.messagesSent++;
		int count = this.clientTracker.containsKey(key) ? this.clientTracker.get(key) : 0;
		this.clientTracker.put(key, count + 1);
	}
	
	public  void setSent() {
		this.messagesSent=0;
	}

	private void write(SelectionKey key) throws IOException {

		SocketChannel socketChannel = (SocketChannel) key.channel();
		Task task=new Task(socketChannel,key,this,null, 1);

		Server.threadPoolManager.addWrite(task);

	}
	
	
	
	public void Initialize() {
		try {
			this.selector=SelectorProvider.provider().openSelector();
			this.serverSocketChannel=ServerSocketChannel.open();
			this.serverSocketChannel.configureBlocking(false);
			
			this.serverSocketChannel.socket().bind(new InetSocketAddress(this.ip, this.port));
			this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

		} catch (IOException e) {
			Logger.write_errors(getClass().getName(),"Initialize", e.getClass().toString(), e);
		}
	}
	
	
	@Override
	public void run() {
		try {
			
			while(true) {

				// Wait for an event one of the registered channels
				this.selector.selectNow();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {

						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				
				  }
			}	
		}catch(Exception e) {
			Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
		}
		
	}
	

	
	private  void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		this.serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);

		Task task=new Task(socketChannel,key,this,null, 2);
		Server.threadPoolManager.addAccept(task);
		

		String connection=socketChannel.socket().getInetAddress().toString()+":"+socketChannel.socket().getPort();
		
		this.numConnections++;
		this.clientTracker.put(connection, 0);
	}
	
	
	public static void main(String[] args) {
		if(args.length!=4) {
			System.out.println("Need 4 arguments.");
			System.exit(1);
		}
		Server server=new Server(Integer.parseInt(args[0]));
		//ThreadPoolManager threadPoolManager = null;
		ClientTracker tracker = new ClientTracker(server); ;
		ServerStatistics stats=new ServerStatistics(server, tracker);
		try {
			
			new Thread(server).start();
			

			threadPoolManager = new ThreadPoolManager(Integer.parseInt(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3]),server);
			threadPoolManager.open();
			new Thread(threadPoolManager).start();
			
			
			new Thread(tracker).start();			
			
			new Thread(stats).start();
			

			
			
		}
		catch(NumberFormatException e) {
			Logger.write_errors("Registry","main", e.getClass().toString(), e);
			System.out.println("Need a valid port.");
			
			System.exit(1);			
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    public void run() {
				System.out.println("Running shutdown hook, closing threads...");
				
				threadPoolManager.setDone();
				tracker.setDone();
				stats.setDone();
		    }
		});
		

	}
}
