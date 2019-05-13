package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cs455.overlay.node.MessagingNodeDescription;

public class MessagingNodesList  implements Event {
	private final MessageType message=MessageType.MESSAGING_NODES_LIST;

	public int numConnections;
	public ArrayList<MessagingNodeDescription> connections;
	
	
	public MessagingNodesList(int numConnections, ArrayList<MessagingNodeDescription> connections) {

		this.numConnections=numConnections;
		this.connections=connections;
	}
	
	public MessagingNodesList(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();
		
		this.numConnections=din.readInt();
		this.connections=new ArrayList<MessagingNodeDescription>();
        if(MessageType.values()[type]!=MessageType.MESSAGING_NODES_LIST) {
        	throw new Exception("Invalid Request: Expected "+MessageType.MESSAGING_NODES_LIST);
        }												
        
		for(int i=0;i<this.numConnections;i++) {
	        int connLength = din.readInt();		

			byte[] conn = new byte[connLength];
			din.readFully(conn);

			int port=din.readInt();
			MessagingNodeDescription desc=new MessagingNodeDescription(new String(conn),port);
			this.connections.add(desc);
			//System.out.println(connections);
			
		}

		baInputStream.close();
		din.close();
		
	}


	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(this.message.ordinal());
		dout.writeInt(this.numConnections);	
		
		for(int i=0;i<numConnections;i++) {
			byte[] conn =this.connections.get(i).getIp().getBytes();
			int connLength = conn.length;
			dout.writeInt(connLength);
			dout.write(conn);		
			
			dout.writeInt(this.connections.get(i).getPort());
		}
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}


	@Override
	public MessageType getType() {
		return message;
	}
}

