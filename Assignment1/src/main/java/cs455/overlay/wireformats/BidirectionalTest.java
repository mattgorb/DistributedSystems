package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BidirectionalTest implements Event{
	private final MessageType message=MessageType.BIDIRECTIONAL_TEST;
	public String test="TEST SUCCEEDED";
	public int port;
	
	public BidirectionalTest(int port) {
		this.port=port;
	}

	public BidirectionalTest(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();
		this.port=din.readInt();
		
        if(MessageType.values()[type]!=MessageType.BIDIRECTIONAL_TEST) {
        	throw new Exception("Invalid Request: Expected "+MessageType.BIDIRECTIONAL_TEST);
        }												
        
        //read ip
        int ipLength = din.readInt();		
		byte[] test = new byte[ipLength];
		din.readFully(test);
		this.test=new String(test);	

		
		baInputStream.close();
		din.close();
		
	}
	
	public MessageType getType() {
		
		return message;
	}
	
	
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(this.message.ordinal());
		dout.writeInt(this.port);		
		byte[] ipBytes = this.test.getBytes();
		int ipLength = ipBytes.length;
		dout.writeInt(ipLength);
		dout.write(ipBytes);
		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
}
