package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.MessagingNodeDescription;


public class MessageRequest implements Event{

	
	private final MessageType type = MessageType.TASK_MESSAGE;
	
	public int payload;
	public int pathCount;
	public MessagingNodeDescription dest;
	public MessagingNodeDescription src;
	public List<MessagingNodeDescription> path;

	
	public MessageRequest(int payload,MessagingNodeDescription dest,MessagingNodeDescription src,List<MessagingNodeDescription> path  ) {

		this.pathCount=path.size();
		this.dest=dest;
		this.path=path;
		this.src=src;
		this.payload=payload;
	}
	
	public MessageRequest(byte[] marshalledBytes) throws Exception {	
		
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();

        if(MessageType.values()[type]!=MessageType.TASK_MESSAGE) {
        	throw new Exception("Invalid Request: Expected "+MessageType.TASK_MESSAGE);
        }			
		int port;

		
		this.pathCount=din.readInt();
			
		this.path=new ArrayList<MessagingNodeDescription>();    
		for(int i=0;i<this.pathCount;i++) {
	        int connLength = din.readInt();		
			byte[] ip = new byte[connLength];
			din.readFully(ip);
			
	        port = din.readInt();		
			this.path.add(new MessagingNodeDescription(new String(ip), port));
			
		}
		
        int connLength = din.readInt();		
		byte[] ip = new byte[connLength];
		din.readFully(ip);
		String add=new String(ip);
		port=din.readInt();
		this.dest=new MessagingNodeDescription(add,port);

        
        connLength = din.readInt();		
		ip = new byte[connLength];
		din.readFully(ip);
		add=new String(ip);
		port=din.readInt();
		this.src=new MessagingNodeDescription(add,port);
		this.payload=din.readInt();	
		
		baInputStream.close();
		din.close();
		
				
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(this.type.ordinal());

		
		dout.writeInt(this.pathCount);	
		
		for(int i=0;i<this.pathCount;i++) {
			byte[] conn =this.path.get(i).getIp().getBytes();
			int connLength = conn.length;
			dout.writeInt(connLength);
			dout.write(conn);		
			
			dout.writeInt(this.path.get(i).getPort());
		}
		
		byte[] conn =this.dest.getIp().getBytes();
		int connLength = conn.length;
		dout.writeInt(connLength);
		dout.write(conn);
		dout.writeInt(this.dest.getPort());
		
		conn =this.src.getIp().getBytes();
		connLength = conn.length;
		dout.writeInt(connLength);
		dout.write(conn);
		dout.writeInt(this.src.getPort());
		
		
		dout.writeInt(this.payload);	
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public int getPayload() {
		return this.payload;
	}

	
	@Override
	public MessageType getType() {
		// TODO Auto-generated method stub
		return this.type;
	}
	
	
	
}
