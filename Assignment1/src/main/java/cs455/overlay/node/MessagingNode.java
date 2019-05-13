package cs455.overlay.node;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import cs455.logger.Logger;
import cs455.overlay.dijkstra.Dijkstra;
import cs455.overlay.transport.ClientSocket;
import cs455.overlay.transport.Receiver;
import cs455.overlay.transport.Sender;
import cs455.overlay.transport.Server;
import cs455.overlay.wireformats.BidirectionalTest;
import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.EstablishConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.LinkInfo;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessageRequest;
import cs455.overlay.wireformats.MessageTest;
import cs455.overlay.wireformats.MessageType;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.RegisterRequest;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.Status;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TrafficSummary;
import cs455.overlay.wireformats.TrafficSummaryRequest;



public class MessagingNode implements Node{
	private String registryIp;
	private int registryPort;
	private LinkWeights linkWeights=null;
	
	public Server messagingNodeServer;

	private MessagingNodesList connectedNodes=null;
	
	private int sentTracker=0;
	private int receiveTracker=0;
	private int relayTracker=0;
	private long sendSummation=0;
	private long receiveSummation=0;
    private EventFactory eventFactory=null;
	private MessagingNodeDescription serverDescription;
	
	private Receiver clientNodeReceiver = null;
	private Thread clientNodeReceiverThread;
	private Thread messagingNodeServerThread=null;
	
	public ClientSocket current=null;
	

	public ArrayList<ClientSocket> clientSocketList=null;
	
	Map<MessagingNodeDescription, List<MessagingNodeDescription>> shortestPathCache;
	
	public MessagingNode(String[] args) {
		//First find a port to start server on.  
    	try {
    		
    		// Initializing Event Factory Singleton Instance
    		eventFactory = EventFactory.getInstance();
    		eventFactory.setNode(this);
    		
    		this.clientSocketList=new ArrayList<ClientSocket>();
       		this.messagingNodeServer=new Server(this,this.eventFactory);

    		this.messagingNodeServerThread=(new Thread(this.messagingNodeServer));
    		this.messagingNodeServerThread.start();
        	
    	}catch(Exception e) {
    		Logger.write_errors(getClass().getName(),"MessagingNode", e.getClass().toString(), e);
    	}
    	

    	
    	
    	this.registryIp=args[0];
    	this.registryPort=Integer.parseInt(args[1]);   	
    	Register();
	}

	
		

	public synchronized void Register() {
        Socket registrySocket;
		try {
			this.serverDescription=new MessagingNodeDescription(this.messagingNodeServer.ip,this.messagingNodeServer.port);
			
    		registrySocket=this.getRegistrySocket(this.registryIp, this.registryPort);
    		
    		this.clientNodeReceiver=new Receiver(registrySocket);
    		this.clientNodeReceiverThread=(new Thread(this.clientNodeReceiver));
    		this.clientNodeReceiverThread.start();
    		this.clientNodeReceiver.setEventFactory(this.eventFactory);
    	
			
	        Sender sender = new Sender(registrySocket);
	        RegisterRequest request=new RegisterRequest(this.serverDescription.getIp(),this.serverDescription.getPort());
	        sender.send(request.getBytes());
	        //registrySocket.close();
		} catch (UnknownHostException e) {
			Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
		} catch (Exception e) {
			Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
		} 

	}
	
	
	
	
	
	@Override
	public   void onEvent(Event event) {
    	if(event.getType()==MessageType.REGISTER_RESPONSE) {
    		RegisterResponse reg=(RegisterResponse)event;
    		System.out.println(reg.additionalInfo+"\n");

    	}
    	if(event.getType()==MessageType.DEREGISTER_RESPONSE) {
    		DeregisterResponse reg=(DeregisterResponse)event;
    		System.out.println(reg.additionalInfo);
    		if(reg.status==Status.SUCCESS) {
    			System.out.println("Closing...");
    			
    			
    			//Thread cleanup
    			this.clientNodeReceiver.setDone();
    			this.messagingNodeServer.setDone();
    			System.exit(0);
    		}
    	}
    	
    	if(event.getType()==MessageType.MESSAGING_NODES_LIST) {
    		
    		this.connectedNodes=(MessagingNodesList)event;
    		
    	}		
    	if(event.getType()==MessageType.ESTABLISH_CONNECTION) {
    		
    		EstablishConnection conn=(EstablishConnection)event;
    		Connect(conn);

    	}	
    	if(event.getType()==MessageType.MESSAGE_TEST) {
    		MessageTest test=(MessageTest)event;
			
    		ConnectBidirectional(test);


    	}		
    	if(event.getType()==MessageType.LINK_WEIGHTS) {
    		

    		
    		this.linkWeights=(LinkWeights)event;
    		TestConnections(this.connectedNodes);
    		
    		

    		
    		//process shortest paths here, after receiving link weights. 
        	Dijkstra shortestPaths =new Dijkstra(this.serverDescription.getIp(),this.serverDescription.getPort(),this.connectedNodes,this.linkWeights);
        	this.shortestPathCache=shortestPaths.compute();
        	
        	//helper function to clean up path
        	removeSelfFromShortestPath();
    		
    		System.out.println("Link weights are received and processed. Ready to send messages." );
    		
    	}	
    	if(event.getType()==MessageType.BIDIRECTIONAL_TEST) {
    		BidirectionalTest b= (BidirectionalTest)event;

    	}	
    	if(event.getType()==MessageType.TASK_INITIATE) {
    		
    		System.out.println("Running...");
    		if(this.shortestPathCache==null) {
            	Dijkstra shortestPaths =new Dijkstra(this.serverDescription.getIp(),this.serverDescription.getPort(),this.connectedNodes,this.linkWeights);
            	this.shortestPathCache=shortestPaths.compute();
            	
            	removeSelfFromShortestPath();
    		}

    		Run((TaskInitiate)event);
    		TaskComplete();
    	}	
    	if(event.getType()==MessageType.TASK_MESSAGE) {
    		ProcessMessage((MessageRequest)event);
    	}		
    	if(event.getType()==MessageType.PULL_TRAFFIC_SUMMARY) {
    		SendTrafficSummary((TrafficSummaryRequest)event);

    	}	
	}
	
	public  synchronized void removeSelfFromShortestPath() {
		boolean found=false;

		//Map containing info on different paths, need to remove this node from the path so we don't send to self.  
		for(Map.Entry<MessagingNodeDescription, List<MessagingNodeDescription>> entry:this.shortestPathCache.entrySet()) {
			if(entry.getKey().compare(this.serverDescription.getIp(),this.serverDescription.getPort())) {
				this.shortestPathCache.remove(entry.getKey());
				found=true;
				break;
			}
		}
		if(!found) {
			System.out.println("FAILED.");
		}
	}
	
	public synchronized void SendTrafficSummary(TrafficSummaryRequest summary){
		try {
			TrafficSummary newSummary=new TrafficSummary(this.sentTracker,this.receiveTracker,this.relayTracker,this.sendSummation,this.receiveSummation,this.serverDescription);
	        
			Socket sendTo;
			sendTo = this.getRegistrySocket(this.registryIp, this.registryPort);
	        Sender sender = new Sender(sendTo);
	        sender.send(newSummary.getBytes());
	        //sendTo.close();
	        clearTrafficVariables();
		} catch (UnknownHostException e) {
			Logger.write_errors(getClass().getName(),"SendTrafficSummary", e.getClass().toString(), e);
			
		} catch (Exception e) {

			Logger.write_errors(getClass().getName(),"SendTrafficSummary", e.getClass().toString(), e);
		} 
		
	}
	
	public synchronized void clearTrafficVariables() {
		this.relayTracker=0;
		this.receiveSummation=0;
		this.receiveTracker=0;
		this.sendSummation=0;
		this.sentTracker=0;
	}
	
	private synchronized void TaskComplete() {
		try {
			TaskComplete newMessage=new TaskComplete(this.serverDescription);
	        
			Socket sendTo;
			sendTo =  this.getRegistrySocket(this.registryIp, this.registryPort);
	        Sender sender = new Sender(sendTo);
	        sender.send(newMessage.getBytes());

		} catch (UnknownHostException e) {
			Logger.write_errors(getClass().getName(),"TaskComplete", e.getClass().toString(), e);

		} catch (Exception e) {
			Logger.write_errors(getClass().getName(),"TaskComplete", e.getClass().toString(), e);
		} 
		System.out.println("Task Complete. ");

	}
	
	private  void Run(TaskInitiate taskInitiate) {
		List<MessagingNodeDescription> keysAsArray = new ArrayList<MessagingNodeDescription>(this.shortestPathCache.keySet());
		
		Random r = new Random();
		
		int randomInt;
		List<MessagingNodeDescription> path=null;
		
		for(int i=0;i<taskInitiate.rounds;i++) {
			if(i%1000==0 && i!=0) {
				System.out.println(getCurrentNumberSent()+" message sent.");
			} 
			for(int j=0;j<5;j++) {

				try {

					MessagingNodeDescription randomNode=keysAsArray.get(r.nextInt(keysAsArray.size()));
					path=this.shortestPathCache.get(randomNode);
					randomInt=r.nextInt();
					MessageRequest newMessage=new MessageRequest(randomInt, randomNode,this.serverDescription,path);
					
					this.SendMessage(newMessage,path.get(0));

			        this.incrementSent();
			        this.addSentSummation(newMessage.getPayload());
			        this.addSentSummation(1);
				} catch (Exception e) {
					Logger.write_errors(getClass().getName(),"Run", e.getClass().toString(), e);

				} 
			}
            try {
            	//This helped to not overload the systems when running.  
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		}
	}
	
	private synchronized int getCurrentNumberSent() {
		return this.sentTracker;
	}
	
	private void ProcessMessage(MessageRequest messageRequest) {
		if(messageRequest.dest.compare(this.serverDescription.getIp(), this.serverDescription.getPort())) {
			this.incrementReceived();
			this.addReceiveSummation(messageRequest.getPayload());
		}


		else {

			messageRequest.path.remove(0);

			try {
				MessageRequest newMessage=new MessageRequest(messageRequest.payload, messageRequest.dest,this.serverDescription,messageRequest.path);

				this.SendMessage(newMessage,messageRequest.path.get(0));
			}
			catch (Exception e) {
				Logger.write_errors(getClass().getName(),"ProcessMessage", e.getClass().toString(), e);
			} 
			this.incrementRelay();
		}
	}

	
	private synchronized void SendMessage(MessageRequest messageRequest,MessagingNodeDescription sendToNode) {
		Sender sender = null ;
		ClientSocket sendTo=null;
		try {
            
            sendTo = this.getConnectionSocket_viaDestination(sendToNode.getIp(), sendToNode.getPort());
            if(sendTo==null) {
            	System.out.println("Failed to find bidirectional socket. ");
            }
	        sender = new Sender(sendTo.socket);
	        
	        sender.send(messageRequest.getBytes());


	        //return true;
		} catch (IOException e) {
			Logger.write_errors(getClass().getName(),"SendMessage", e.getClass().toString(), e);
			//return false;
			
		
		}catch (Exception e) {
			Logger.write_errors(getClass().getName(),"SendMessage", e.getClass().toString(), e);
			//return false;
		}

	}
	
	
	private synchronized  void incrementSent() {
		this.sentTracker++;
	}
	private synchronized  void incrementReceived() {
		this.receiveTracker++;
	}
	private synchronized  void incrementRelay() {
		this.relayTracker++;
	}
	private synchronized  void addSentSummation(int add) {
		this.sendSummation+=add;
	}
	private synchronized  void addReceiveSummation(int add) {
		this.receiveSummation+=add;
	}
	

	private static void PrintShortestPath(MessagingNode messagingNode) {


		if(messagingNode.shortestPathCache==null) {
			if(messagingNode.linkWeights!=null) {
				Dijkstra shortestPaths =new Dijkstra(messagingNode.serverDescription.getIp(),messagingNode.serverDescription.getPort(),messagingNode.connectedNodes,messagingNode.linkWeights);
				messagingNode.shortestPathCache=shortestPaths.compute();
				
	        	//helper function to clean up path
				messagingNode.removeSelfFromShortestPath();
			}
			else {
				System.out.println("Need link weights before we can compute shortest path.  Please set the overlay in the registry.");
				return;
			}
			

			
		}
		
		System.out.println("\nLink Weights:");
		for(LinkInfo info:messagingNode.linkWeights.linkInfo) {
			System.out.println(info.ip1+" "+info.port1+" "+info.ip2+" "+info.port2+" "+info.weight+" " );
		}
		System.out.println("\n");

		
		System.out.println("This node:  "+messagingNode.serverDescription.getIp()+":"+messagingNode.serverDescription.getPort());
		for(Map.Entry<MessagingNodeDescription, List<MessagingNodeDescription>> entry:messagingNode.shortestPathCache.entrySet()) {
			System.out.println("PATH TO: "+entry.getKey().getIp()+":"+entry.getKey().getPort());

			for(int i=0;i< entry.getValue().size();i++) {
				System.out.println("\t"+entry.getValue().get(i).getIp()+":"+entry.getValue().get(i).getPort());
			}
			System.out.println("\n");
		}
	}
	
	
	
    public synchronized static void CommandLineInput(MessagingNode messagingNode)
    {	
    	Scanner in = new Scanner(System.in);
        while(true)
        {

            String input = in.nextLine();
            if(input.equals("print-shortest-path"))
            {
            	PrintShortestPath(messagingNode);
            }
            if(input.equals("exit-overlay"))
            {
		        Socket registrySocket;
				try {
					registrySocket=messagingNode.getRegistrySocket(messagingNode.registryIp, messagingNode.registryPort);
			        Sender sender = new Sender(registrySocket);
			        DeregisterRequest request=new DeregisterRequest(messagingNode.serverDescription.getIp(),messagingNode.serverDescription.getPort());
			        sender.send(request.getBytes());

			        registrySocket.close();
					
				} catch (UnknownHostException e) {
					Logger.write_errors("MessagingNode","TestConnections", e.getClass().toString(), e);
				} catch (IOException e) {
					Logger.write_errors("MessagingNode","TestConnections", e.getClass().toString(), e);
				}

            }


        }
        
    }
	
	public static void main(String[] args) {
		if(args.length !=2) {
			System.out.println("Need a valid registration host and port.");
			System.exit(1);
		}
		
		MessagingNode messagingNode=new MessagingNode(args);
		
		/*Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		    	  
		        System.out.println("Running Shutdown Hook...");
		        
		        Socket registrySocket;
				try {
					registrySocket = messagingNode.getRegistrySocket(messagingNode.registryIp, messagingNode.registryPort);
			        Sender sender = new Sender(registrySocket);
			        DeregisterRequest request=new DeregisterRequest(messagingNode.serverDescription.getIp(),messagingNode.serverDescription.getPort());
			        sender.send(request.getBytes());
					registrySocket.close();
				} catch (UnknownHostException e) {
					Logger.write_errors("MessagingNode","TestConnections", e.getClass().toString(), e);
				} catch (IOException e) {
					Logger.write_errors("MessagingNode","TestConnections", e.getClass().toString(), e);
				}

		      }
		    });	*/
		
		CommandLineInput(messagingNode);
		

	}



	public synchronized Socket getRegistrySocket(String ip, int clientPort) throws UnknownHostException, IOException {

    	for(ClientSocket s:this.clientSocketList) {
    		if(ip.equalsIgnoreCase(s.ip)&&clientPort==s.port) {
    			return s.socket;
    		}
    		
    	}

    	return addClientSocket(ip,clientPort);

	}
	
	public Socket addClientSocket(String ip, int clientPort) throws UnknownHostException, IOException {
		Socket newSocket=new Socket(ip,clientPort);

		
		this.clientSocketList.add(new ClientSocket(ip,clientPort,newSocket));

		return newSocket;
		
	}
	


	private synchronized void Connect(EstablishConnection connect) {
			try {
				Socket s=new Socket(connect.ip,connect.portNumber);
				ClientSocket cli=new ClientSocket(s.getInetAddress().toString().substring(1),s.getPort(),s);
				
				cli.originatingIp=connect.ip;
				cli.originatingPort=connect.portNumber;
			
				this.clientNodeReceiver=new Receiver(cli.socket);
		    	this.clientNodeReceiverThread=(new Thread(this.clientNodeReceiver));
		   		this.clientNodeReceiverThread.start();
		   		this.clientNodeReceiver.setEventFactory(this.eventFactory);
		        Sender sender = new Sender(cli.socket);

		        MessageTest request=new MessageTest(this.messagingNodeServer.ip, this.messagingNodeServer.port,s.getLocalAddress().toString().substring(1),s.getLocalPort());
		        sender.send(request.getBytes());

		        this.addSender(cli);

				System.out.println("Connection established: "+connect.ip+":"+connect.portNumber);
		        
			} catch (UnknownHostException e) {
				Logger.write_errors(getClass().getName(),"TestConnections", e.getClass().toString(), e);
			} catch (Exception e) {
				Logger.write_errors(getClass().getName(),"TestConnections", e.getClass().toString(), e);
			} 
		
		

		
	}
	
	
	public synchronized void ConnectBidirectional(MessageTest test) {

		try {
			ClientSocket cli = null;


			if(getConnectionSocket_viaSocketInfo(test.socketIp, test.socketPort)!=null) {
				cli=getConnectionSocket_viaSocketInfo(test.socketIp, test.socketPort);
				cli.originatingIp=test.ip;
				cli.originatingPort=test.port;
				System.out.println("Bidirectional connection established: "+test.ip+":"+test.port);
			}
			else {
				System.out.println("Failed to find matching socket.");
			}

	        
		}  catch (Exception e) {
			Logger.write_errors(getClass().getName(),"TestConnections", e.getClass().toString(), e);
		} 
	
		
	

	
}
	
	
	

	public synchronized ClientSocket getConnectionSocket_viaSocketInfo(String ip, int port) {
		
		for(ClientSocket s:this.clientSocketList) {
    		if(ip.equalsIgnoreCase(s.ip)&& port==s.port) {
    			return s;
    		}
    	}
		return null;
	}
	
	public synchronized  ClientSocket getConnectionSocket_viaDestination(String ip, int port) {
		
		for(ClientSocket s:this.clientSocketList) {
    		if(ip.equalsIgnoreCase(s.originatingIp)&& port==s.originatingPort) {
    			return s;
    		}
    	}

		System.out.println("Not Found");
		return null;
	}
	
	public synchronized  ClientSocket socketInList(String ip, int port) {
		
		for(ClientSocket s:this.clientSocketList) {
    		if(ip.equalsIgnoreCase(s.ip)&& port==s.port) {
    			return s;
    		}
    	}
		return null;

	}


	private synchronized void TestConnections(MessagingNodesList list) {

		for(int i=0;i<list.numConnections;i++) {
			try {
				ClientSocket cli = getConnectionSocket_viaDestination(list.connections.get(i).getIp(), list.connections.get(i).getPort());		        
		        
				Sender sender = new Sender(cli.socket);
		        BidirectionalTest request=new BidirectionalTest(this.messagingNodeServer.port);
		        sender.send(request.getBytes());



			}  catch (Exception e) {
				Logger.write_errors(getClass().getName(),"TestConnections", e.getClass().toString(), e);
			} 
		}
		
		System.out.println("\nAll connections are tested and established. Number of connections: "+list.numConnections);
		
	}

	@Override
	public synchronized void addSender(ClientSocket socket) {
		
		this.clientSocketList.add(socket);
		
	}



	@Override
	public synchronized void setCurrent(ClientSocket sock) {
		this.current=sock;
	}



	
	public void printSocketList() {

    	System.out.println("\n\n\nSOCKET LIST:");
    	for(ClientSocket s:this.clientSocketList) {
    		//System.out.println(s.socket);
    		System.out.println("Comm: "+s.port+ " Orig: "+s.originatingPort+ "\t Socket:"+s.socket);

    		
    	}

	}
}
