package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.MessagingNodeDescription;

public class TrafficSummary implements Event{
	private final MessageType type = MessageType.TRAFFIC_SUMMARY;
	
	public int sentTracker=0;
	public int receiveTracker=0;
	public int relayTracker=0;
	public long sendSummation=0;
	public long receiveSummation=0;
	public MessagingNodeDescription messagingNode;
	
	public TrafficSummary(	 int sentTracker,
							 int receiveTracker,
							 int relayTracker,
							 long sendSummation,
							 long receiveSummation, MessagingNodeDescription messagingNode) {
		this.sentTracker=sentTracker;
		this.receiveTracker=receiveTracker;
		this.relayTracker=relayTracker;
		this.sendSummation=sendSummation;
		this.receiveSummation=receiveSummation;
		this.messagingNode=messagingNode;
		
	}
	
	public TrafficSummary(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type=din.readInt();
        if(MessageType.values()[type]!=MessageType.TRAFFIC_SUMMARY) {
        	throw new Exception("Invalid Request: Expected "+MessageType.TRAFFIC_SUMMARY);
        }	
        
		this.sentTracker=din.readInt();
		this.receiveTracker=din.readInt();
		this.relayTracker=din.readInt();
		this.sendSummation=din.readLong();
		this.receiveSummation=din.readLong();
		
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
		dout.writeInt(this.sentTracker);
		dout.writeInt(this.receiveTracker);
		dout.writeInt(this.relayTracker);
		dout.writeLong(this.sendSummation);
		dout.writeLong(this.receiveSummation);
		
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
