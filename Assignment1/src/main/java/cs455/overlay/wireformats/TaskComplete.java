package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import cs455.overlay.node.MessagingNodeDescription;

public class TaskComplete implements Event{
	private final MessageType type = MessageType.TASK_COMPLETE;
	public MessagingNodeDescription messagingNode;
	
	public TaskComplete(MessagingNodeDescription messagingNode) {
		this.messagingNode=messagingNode;
		
	}
	public TaskComplete(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type=din.readInt();
        if(MessageType.values()[type]!=MessageType.TASK_COMPLETE) {
        	throw new Exception("Invalid Request: Expected "+MessageType.TASK_COMPLETE);
        }		
        
		int connLength = din.readInt();		
		byte[] ip = new byte[connLength];
		din.readFully(ip);
		
		int port = din.readInt();		
		
		
		this.messagingNode=new MessagingNodeDescription(new String(ip), port);

		baInputStream.close();
		din.close();

	}
	
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(this.type.ordinal());
		

		byte[] conn =this.messagingNode.getIp().getBytes();
		int connLength = conn.length;
		dout.writeInt(connLength);
		dout.write(conn);		
		
		dout.writeInt(this.messagingNode.getPort());

		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	@Override
	public MessageType getType() {
		// TODO Auto-generated method stub
		return this.type;
	}

}
