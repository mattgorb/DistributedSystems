package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import cs455.logger.Logger;
import cs455.overlay.transport.ClientSocket;
import cs455.overlay.transport.Sender;
import cs455.overlay.transport.Server;
import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.EstablishConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.LinkInfo;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessageType;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.RegisterRequest;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.Status;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TrafficSummary;
import cs455.overlay.wireformats.TrafficSummaryRequest;

public class Registry implements Node {
	

    
    private ArrayList<MessagingNodeDescription>  nodeList;
    private ArrayList<MessagingNodeDescription>  nodeListComplete;
    
    private EventFactory eventFactory=null;
  
    private ArrayList<TrafficSummary> summary=new ArrayList<TrafficSummary>();
    
    private Server registryServer=null;
    private Thread registryServerThread;
    
    private boolean sentConnections=false;

	public ArrayList<ClientSocket> clientSocketList=null;
    
    LinkWeights linkWeights=null;
    
    private Registry(int portNumber) {

    	try {
    		
    		
    		// Initializing Event Factory Singleton Instance
    		eventFactory = EventFactory.getInstance();
    		eventFactory.setNode(this);
    		
    		this.clientSocketList=new ArrayList<ClientSocket>();
    		
    		registryServer=new Server(this,portNumber,eventFactory);
        	registryServerThread=(new Thread(registryServer));
        	registryServerThread.start();

    		

        	
    		if(registryServerThread.isAlive()) {
    			 System.out.println("Registry ServerSocket started.");
    		}
    		
    		nodeList=new ArrayList<>();
    		
    	}catch(Exception e) {
			Logger.write_errors(getClass().getName(),"Registry", e.getClass().toString(), e);
    	}

    } 
    
    


    public synchronized void Register(RegisterRequest request) throws UnknownHostException, IOException {
    	MessagingNodeDescription newNode=new MessagingNodeDescription(request.ip,request.portNumber);
    	Socket nodeSocket=this.getClientSocket(request.ip,request.portNumber);

    	if(registryServer.originatingAddress.get(request.ip)==null) {

			
			try {

		        Sender sender = new Sender(nodeSocket);
		        RegisterResponse res=new RegisterResponse(Status.FAILURE,"Registration unsuccessful: Requested IP address doesn't match the actual IP the message is originating from.  ");
		        sender.send(res.getBytes());
			} catch (UnknownHostException e) {
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			} catch (IOException e) {
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			}
		}
    	else
    	 if(nodeList.contains(newNode)) {
			try {

		        Sender sender = new Sender(nodeSocket);
		        RegisterResponse res=new RegisterResponse(Status.FAILURE,"Registration unsuccessful: node already in registry.");
		        sender.send(res.getBytes());
			} catch (UnknownHostException e) {
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			} catch (IOException e) {
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			}
    	}else {
    		nodeList.add(newNode);

			try {
				//this.printSocketList();//for debugging

		        Sender sender = new Sender(nodeSocket);

		        RegisterResponse res=new RegisterResponse(Status.SUCCESS,"Registration successful.");
		        sender.send(res.getBytes());    
			} catch (UnknownHostException e) {
				nodeList.remove(newNode);
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			} catch (IOException e) {
				nodeList.remove(newNode);
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			}
			catch(Exception e) {
				Logger.write_errors(getClass().getName(),"Register", e.getClass().toString(), e);
			}
    	}
    }
    	


	
	//FOR DEBUGGING
	public void printSocketList() {

    	System.out.println("\n\n\nSOCKET LIST:");
    	for(ClientSocket s:this.clientSocketList) {
    		System.out.println(s.socket);

    	}
	}
    
    public synchronized void Deregister(DeregisterRequest request) throws UnknownHostException, IOException {
    	Socket nodeSocket=this.getClientSocket(request.ip,request.portNumber);
    	//First check whether the address requesting to be added matches its actual address.  
    	if(registryServer.originatingAddress.get(request.ip)==null) {			
			try {
				//nodeSocket = new Socket(request.ip, request.portNumber);
		        Sender sender = new Sender(nodeSocket);
		        RegisterResponse res=new RegisterResponse(Status.FAILURE,"Deregistration unsuccessful: Requested IP address doesn't match the actual IP the message is originating from.  ");
		        sender.send(res.getBytes());
			} catch (UnknownHostException e) {
				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
			} catch (IOException e) {
				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
			}
		}else {
	    	MessagingNodeDescription newNode=new MessagingNodeDescription(request.ip,request.portNumber);
	    	Boolean found=false;
    		nodeList.remove(newNode);
    	    for(MessagingNodeDescription n : nodeList) {
    	        if(n.getIp().equals(request.ip) && n.getPort()==request.portNumber) {
    	        	nodeList.remove(n);
    	        	found=true;
    	            break;
    	        }
    	    }
    		if(found) {
    			//Socket nodeSocket;
    			try {
    				//nodeSocket = new Socket(request.ip, request.portNumber);
    		        Sender sender = new Sender(nodeSocket);
    		        DeregisterResponse res=new DeregisterResponse(Status.SUCCESS,"Deregistration successful.");
    		        System.out.println("Messaging node deregisted: "+request.ip+":"+request.portNumber);
    		        sender.send(res.getBytes());
    			} catch (UnknownHostException e) {
    				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
    			} catch (IOException e) {
    				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
    			}
    		}else {
    			//Socket nodeSocket;
    			try {
    				//nodeSocket = new Socket(request.ip, request.portNumber);
    		        Sender sender = new Sender(nodeSocket);
    		        RegisterResponse res=new RegisterResponse(Status.FAILURE,"Registration unsuccessful: Node not found in registry list.");
    		        sender.send(res.getBytes());
    			} catch (UnknownHostException e) {
    				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
    			} catch (IOException e) {
    				Logger.write_errors(getClass().getName(),"Deregister", e.getClass().toString(), e);
    			}
    		}

    	}
    	
    	
    }
    
    public synchronized static void GenerateOverlay_AssignWeights(Registry registry, int conn) {
    	if(conn<registry.nodeList.size()  ) {
    		//add overlay for odd number of connections
    		//if(conn%2==0) {
        		Map<MessagingNodeDescription, ArrayList> map = new HashMap<MessagingNodeDescription, ArrayList>();
        		for(MessagingNodeDescription n:registry.nodeList) {
        			map.put(n, new ArrayList<MessagingNodeDescription>());
        		}
        		int i=0;

        		while(i<conn) {
            		for(int j=0;j<registry.nodeList.size();j++) {
            			if((j+1+i)>=registry.nodeList.size())
            			{
            				int diff=(j+1+i)-registry.nodeList.size();
            				map.get(registry.nodeList.get(j)).add(registry.nodeList.get(diff)); 
            				map.get(registry.nodeList.get(diff)).add(registry.nodeList.get(j)); 

            			}else {
            				map.get(registry.nodeList.get(j)).add(registry.nodeList.get(j+1+i));  
            				map.get(registry.nodeList.get(j+1+i)).add(registry.nodeList.get(j));    
            				
            			}
            		}
        			i=i+2;
        		}
        		

        		for( MessagingNodeDescription key : map.keySet()) {
        			MessagingNodesList list=new MessagingNodesList(map.get(key).size(),map.get(key));
        			Socket nodeSocket;
        			try {
        				nodeSocket = registry.getClientSocket(key.getIp(),key.getPort());
        				
        		        Sender sender = new Sender(nodeSocket);
        		        sender.send(list.getBytes());
        			} catch (UnknownHostException e) {
        				Logger.write_errors("Registry","GenerateOverlay_AssignWeights", e.getClass().toString(), e);
        			} catch (IOException e) {
        				Logger.write_errors("Registry","GenerateOverlay_AssignWeights", e.getClass().toString(), e);
        			}
        		}
        		
        		
        		
        		ArrayList<LinkInfo> linkInfo=new ArrayList<LinkInfo>();
        		for( MessagingNodeDescription key : map.keySet()) {
        			ArrayList<MessagingNodeDescription> partners=map.get(key);  
        			for(MessagingNodeDescription b:partners) {
        				LinkInfo newLink=new LinkInfo(key.getIp(),b.getIp(),key.getPort(),b.getPort(),(int)(Math.random()*10)+1);
        				if(map.get(b).contains(key));
        				map.get(b).remove(key);
        				linkInfo.add(newLink);
        			}
        		}     
        		
        		
        		//using this to send to first node in connection so that I can establish connections.   
        		registry.linkWeights=new LinkWeights(linkInfo);
        		
        		for(LinkInfo info : linkInfo) {
        			
        			Socket nodeSocket;
        			try {
        				
        				nodeSocket =registry.getClientSocket(info.ip1,info.port1);
        				//nodeSocket = new Socket(key.getIp(), key.getPort());
        		        Sender sender = new Sender(nodeSocket);
        		        EstablishConnection est=new EstablishConnection(info.ip2,info.port2, info.ip1,info.port1);
        		        sender.send(est.getBytes());
        			} catch (UnknownHostException e) {
        				Logger.write_errors("Registry","GenerateOverlay_AssignWeights", e.getClass().toString(), e);
        			} catch (IOException e) {
        				Logger.write_errors("Registry","GenerateOverlay_AssignWeights", e.getClass().toString(), e);
        			}
        		}       		
        		

        		
    	}else {
    		System.out.println("Number of connections needs to be less than number of nodes.  Current number of nodes in registry: "+registry.nodeList.size()+", requested connections:" +conn);
    	}
    }
    
    public static void sendLinkWeights(Registry registry) {
    	if(registry.linkWeights==null) {
    		System.out.println("Please setup overlay with 'setup-overlay' command");
    	}
    	else {
        	for(MessagingNodeDescription node:registry.nodeList) {
    			Socket nodeSocket;
    			try {
    				nodeSocket =registry.getClientSocket(node.getIp(), node.getPort());

    		        Sender sender = new Sender(nodeSocket);
    		        sender.send(registry.linkWeights.getBytes());
    			} catch (UnknownHostException e) {
    				Logger.write_errors("Registry","sendLinkWeights", e.getClass().toString(), e);
    			} catch (IOException e) {
    				Logger.write_errors("Registry","sendLinkWeights", e.getClass().toString(), e);
    			}
        	}
    	}
    	registry.sentConnections=true;
    	
    }
    
    
    public synchronized static void InitiateRun(Registry registry, int rounds) {
		TaskInitiate initiate=new TaskInitiate(rounds);
		registry.nodeListComplete=new ArrayList<MessagingNodeDescription>(registry.nodeList);
    	for(int i=0;i<registry.nodeList.size();i++) {

    		Socket nodeSocket;
    		try {//registry.getClientSocket(key.getIp(),key.getPort());
    		
    			nodeSocket =registry.getClientSocket(registry.nodeList.get(i).getIp(), registry.nodeList.get(i).getPort());
    	        Sender sender = new Sender(nodeSocket);
    	        sender.send(initiate.getBytes());
    		} catch (UnknownHostException e) {
				Logger.write_errors("Registry","InitiateRun", e.getClass().toString(), e);
    		} catch (IOException e) {
				Logger.write_errors("Registry","InitiateRun", e.getClass().toString(), e);
    		}
    	}
    	

    }
    
    
    public static void CommandLineInput(Registry registry)
    {	
        Scanner in = new Scanner(System.in);
        while(true)
        {
            String input = in.nextLine();
            if(input.equals("list-messaging-nodes"))
            {
    			int a=1;
    			System.out.println("Node List:");
    			for(MessagingNodeDescription i:registry.nodeList) {
    				System.out.println("Node "+a+", ip: "+i.getIp()+" port: "+i.getPort());
    				a++;
    			}
            }
            if(input.equals("list-weights"))
            {
            	if(registry.linkWeights==null) {
            		System.out.println("Please create overlay first with the comand 'setup-overaly #Connections'");

            	}
            	else {
            		//send link weights to all messaging nodes.  
                	System.out.println("Connection Weights:");
                	for(LinkInfo info:registry.linkWeights.linkInfo) {
                		System.out.println("Node 1: "+info.ip1+":"+info.port1);
                		System.out.println("Node 2:"+info.ip2+":"+info.port2);
                		System.out.println("Weight:"+info.weight+"\n");
                	}


            	}

            }
            if(input.startsWith("setup-overlay"))
            {
	            	int numConnections;
	            	try{
	            		numConnections=Integer.parseInt(new String(input).split(" ")[1]);
	            	}catch(Exception e) {
	            		numConnections=4;
	            		System.out.println("Failed to parse connection count, setting connections to 4. ");
	            	}
	            	GenerateOverlay_AssignWeights(registry, numConnections);
            }
            if(input.equals("send-overlay-link-weights"))
            {
            	sendLinkWeights(registry);
            }
            if(input.startsWith("start")){
            	if(registry.sentConnections==false) {
            		System.out.println("Please send connections to nodes with command 'send-overlay-link-weights' before running");
            	}
            	else {
			    	int numConnections;
			    	registry.summary.clear();	
			    	try{
			    		numConnections=Integer.parseInt(new String(input).split(" ")[1]);
			    	
			    	}catch(Exception e) {
			    		numConnections=4;
			    		System.out.println("Failed to parse rounds, setting to 4. ");
			    	}
			    	InitiateRun(registry, numConnections);
            	}

            }
        }
    }
    
    public void onEvent(Event event) throws UnknownHostException, IOException {
	    	if(event.getType()==MessageType.REGISTER_REQUEST) {
	    			Register((RegisterRequest)event);
	    	}
	    	else if(event.getType()==MessageType.DEREGISTER_REQUEST) {
				Deregister((DeregisterRequest)event);
	    	}
	    	else if(event.getType()==MessageType.TASK_COMPLETE) {
	    		CheckTasksComplete((TaskComplete)event);
	    	}
	    	else if(event.getType()==MessageType.TRAFFIC_SUMMARY) {
	    		PrintTrafficSummary((TrafficSummary)event);
	    	}
    }
    
    public synchronized void PrintTrafficSummary(TrafficSummary summary) {

	    	if(this.nodeListComplete.size()>0) {
	        	for(int i=0;i<this.nodeListComplete.size();i++) {
	        		if(this.nodeListComplete.get(i).compareObject(summary.messagingNode)) {
	        			this.nodeListComplete.remove(i);
	        			break;
	        		}
	        	} 
	        	this.summary.add(summary);
	        	
	    	}
	    	if(this.nodeListComplete.size()==0) {
	        	
	    		int totalReceived=0;
	    		int totalRelayed=0;
	    		int totalSent=0;
	    		System.out.println("TRAFFIC SUMMARY:");
	    		for(TrafficSummary s:this.summary) {
	    			System.out.println("Messaging Node: "+s.messagingNode.getIp()+":"+s.messagingNode.getPort());
	    			System.out.println("\t# Sent: "+s.sentTracker);
	    			System.out.println("\t# Received: "+s.receiveTracker);
	    			System.out.println("\t# Summation Sent: "+s.sendSummation);
	    			System.out.println("\t# Summation Received: "+s.receiveSummation);
	    			System.out.println("\t# Relayed: "+s.relayTracker+"\n");
	    			totalReceived+=s.receiveTracker;
	    			totalRelayed+=s.relayTracker;
	    			totalSent+=s.sentTracker;
	    		}
	    		System.out.println("TOTAL MESSAGES SENT: "+totalSent);
	    		System.out.println("TOTAL MESSAGES RECEIVED: "+totalReceived);
	    		System.out.println("TOTAL MESSAGES RELAYED: "+totalRelayed);

	    	}
    }
    
    public synchronized void CheckTasksComplete(TaskComplete taskComplete) {

	    	if(this.nodeListComplete.size()>0) {
	        	for(int i=0;i<this.nodeListComplete.size();i++) {
	        		if(this.nodeListComplete.get(i).compareObject(taskComplete.messagingNode)) {
	        			this.nodeListComplete.remove(i);
	        			break;
	        		}
	        	}    		
	    	}
	
	    	if(this.nodeListComplete.size()==0) {
	    		System.out.println("Received all. Thread.sleeping for 15 seconds...");
	    		try {
	    			
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					Logger.write_errors("Registry","CheckTasksComplete", e.getClass().toString(), e);
				}

	        	for(int i=0;i<this.nodeList.size();i++) {
	        		TrafficSummaryRequest summary_request=new TrafficSummaryRequest();
	        		Socket nodeSocket;
	        		try {
	        			nodeSocket = new Socket(this.nodeList.get(i).getIp(), this.nodeList.get(i).getPort());
	        	        Sender sender = new Sender(nodeSocket);
	        	        sender.send(summary_request.getBytes());
	        	    
	        		} catch (UnknownHostException e) {
	        			Logger.write_errors(getClass().getName(),"CheckTasksComplete", e.getClass().toString(), e);
	        		} catch (IOException e) {
	        			Logger.write_errors(getClass().getName(),"CheckTasksComplete", e.getClass().toString(), e);
	        		}
	        	}
	    		this.nodeListComplete=new ArrayList<MessagingNodeDescription>(this.nodeList);
	    	}



    }

	public static void main(String[] args) {
		
		int registryPort = 0;
		
		if(args.length!=1) {
			System.out.println("Need a valid port.");
			System.exit(1);
		}
		
		try {
			registryPort=Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			Logger.write_errors("Registry","main", e.getClass().toString(), e);
			System.out.println("Need a valid port.");
			
			System.exit(1);			
		}

		CommandLineInput(new Registry(registryPort));
	}




	@Override
	public void addSender(ClientSocket socket) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setCurrent(ClientSocket sock) {
		// TODO Auto-generated method stub
		
	}
	
	public  synchronized  Socket getClientSocket(String ip, int clientPort) throws UnknownHostException, IOException {

    	for(ClientSocket s:this.clientSocketList) {
    		//System.out.println(s.socket);
    		if(ip.equalsIgnoreCase(s.ip)&&clientPort==s.port) {
    			return s.socket;
    		}
    		
    	}
    	return addClientSocket(ip,clientPort);
	}
	
	public  synchronized Socket addClientSocket(String ip, int clientPort) throws UnknownHostException, IOException {
		Socket newSocket=new Socket(ip,clientPort);
		
		
		this.clientSocketList.add(new ClientSocket(ip,clientPort,newSocket));
		return newSocket;
		
	}


	@Override
	public ClientSocket socketInList(String ip, int port) {
		// TODO Auto-generated method stub
		return null;
	}





}
