package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummaryRequest implements Event{
	private final MessageType type = MessageType.PULL_TRAFFIC_SUMMARY;
	
	
	public TrafficSummaryRequest() {
		
	}
	
	public TrafficSummaryRequest(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type=din.readInt();
        if(MessageType.values()[type]!=MessageType.PULL_TRAFFIC_SUMMARY) {
        	throw new Exception("Invalid Request: Expected "+MessageType.PULL_TRAFFIC_SUMMARY);
        }		
		baInputStream.close();
		din.close();

	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(this.type.ordinal());
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	@Override
	public MessageType getType() {
		return this.type;
	}

}
