package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LinkWeights implements Event{
	public ArrayList<LinkInfo> linkInfo;
	public MessageType message=MessageType.LINK_WEIGHTS;
	
	public int count;
	
	public LinkWeights(ArrayList<LinkInfo> linkInfo) {
		this.linkInfo=linkInfo;
		this.count=linkInfo.size();
	}
	
	public LinkWeights(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();

		this.linkInfo=new ArrayList<LinkInfo>();
		
		this.count=din.readInt();
        if(MessageType.values()[type]!=MessageType.LINK_WEIGHTS) {
        	throw new Exception("Invalid Request: Expected "+MessageType.LINK_WEIGHTS);
        }												
        
		for(int i=0;i<this.count;i++) {
	        int connLength = din.readInt();		
			byte[] ip1 = new byte[connLength];
			din.readFully(ip1);
			
	        connLength = din.readInt();		
			byte[] ip2 = new byte[connLength];
			din.readFully(ip2);

			int port1=din.readInt();
			int port2=din.readInt();
			int weight=din.readInt();
			LinkInfo desc=new LinkInfo(new String(ip1),new String(ip2),port1,port2,weight);
			this.linkInfo.add(desc);
			
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
		dout.writeInt(this.count);	
		
		for(int i=0;i<this.count;i++) {
			byte[] conn =this.linkInfo.get(i).ip1.getBytes();
			int connLength = conn.length;
			dout.writeInt(connLength);
			dout.write(conn);		
			
			conn =this.linkInfo.get(i).ip2.getBytes();
			connLength = conn.length;
			dout.writeInt(connLength);
			dout.write(conn);		
			
			dout.writeInt(this.linkInfo.get(i).port1);
			dout.writeInt(this.linkInfo.get(i).port2);
			dout.writeInt(this.linkInfo.get(i).weight);
			
		}
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	@Override
	public MessageType getType() {
		return this.message;
	}

}
