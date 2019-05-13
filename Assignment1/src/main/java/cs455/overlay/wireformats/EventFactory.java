package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cs455.overlay.node.Node;

public class EventFactory {
	// static variable single_instance of type Singleton 
    private static EventFactory event = new EventFactory();
    private Node node=null;
    private int type=0;
    
    private EventFactory() {} ;
    
    public static EventFactory getInstance() 
    { 
        if (event == null) 
        	event = new EventFactory(); 
  
        return event; 
    } 
    
    
    public  void setNode(Node node) {
    	this.node=node;
    }
    // private constructor restricted to this class itself 
    public  void fireMessage(byte[] message) throws Exception 
    { 	

    	
    	
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(message);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		this.type = din.readInt();
		baInputStream.close();
		din.close();
		
		
    	Event event=null;		
		
    	if(MessageType.values()[type]==MessageType.REGISTER_REQUEST) {
    		event=new RegisterRequest(message);
    	}
    	else if(MessageType.values()[type]==MessageType.REGISTER_RESPONSE) {
			event=new RegisterResponse(message);
    	}		
    	else if(MessageType.values()[type]==MessageType.DEREGISTER_REQUEST) {
			event=new DeregisterRequest(message);

    	}		
    	else if(MessageType.values()[type]==MessageType.DEREGISTER_RESPONSE) {
			event=new DeregisterResponse(message);
    	}		
    	else if(MessageType.values()[type]==MessageType.MESSAGING_NODES_LIST) {
			event=new MessagingNodesList(message);
    	}	
    	else if(MessageType.values()[type]==MessageType.MESSAGE_TEST) {
			event=new MessageTest(message);
    	}	
    	else if(MessageType.values()[type]==MessageType.LINK_WEIGHTS) {
			event=new LinkWeights(message);
			
    	}	
    	else if(MessageType.values()[type]==MessageType.TASK_MESSAGE) {
			event=new MessageRequest(message);
    	}
    	else if(MessageType.values()[type]==MessageType.TASK_INITIATE) {
			event=new TaskInitiate(message);
    	}	
    	else if(MessageType.values()[type]==MessageType.TASK_COMPLETE) {
			event=new TaskComplete(message);
    	}
    	else if(MessageType.values()[type]==MessageType.PULL_TRAFFIC_SUMMARY) {
			event=new TrafficSummaryRequest(message);
    	}
    	else if(MessageType.values()[type]==MessageType.TRAFFIC_SUMMARY) {
			event=new TrafficSummary(message);
    	}
    	else if(MessageType.values()[type]==MessageType.ESTABLISH_CONNECTION) {
			event=new EstablishConnection(message);
    	}
    	else if(MessageType.values()[type]==MessageType.BIDIRECTIONAL_TEST) {
			event=new BidirectionalTest(message);
    	}
    	node.onEvent(event);
    		
    } 
  

}
