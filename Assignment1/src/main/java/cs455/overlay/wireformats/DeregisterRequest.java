package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class DeregisterRequest implements Event{
	private final MessageType message=MessageType.DEREGISTER_REQUEST;
	public String ip;
	public int portNumber;

	public DeregisterRequest(String ip, int portNum) {
		this.ip=ip;
		this.portNumber=portNum;
	}
	
	
	public DeregisterRequest(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();
		
        if(MessageType.values()[type]!=MessageType.DEREGISTER_REQUEST) {
        	throw new Exception("Invalid Request: Expected "+MessageType.DEREGISTER_REQUEST);
        }
        

        //read ip
        int ipLength = din.readInt();		
		byte[] ip = new byte[ipLength];
		din.readFully(ip);
		this.ip=new String(ip);
		
		this.portNumber = din.readInt();
	
		
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
		//dout.writeInt(2);
		
		byte[] ipBytes = this.ip.getBytes();
		int ipLength = ipBytes.length;
		dout.writeInt(ipLength);
		dout.write(ipBytes);
		
		dout.writeInt(this.portNumber);		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		}


}
