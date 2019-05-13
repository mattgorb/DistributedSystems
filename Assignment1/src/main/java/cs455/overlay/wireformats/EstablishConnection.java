package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EstablishConnection implements Event {
	private final MessageType message=MessageType.ESTABLISH_CONNECTION;
	public String ip;
	public int portNumber;
	
	public String thisIp;
	public int thisPort;
	
	public EstablishConnection(String ip, int portNum, String thisIp, int thisPort) {
		this.ip=ip;
		this.portNumber=portNum;
		this.thisIp=thisIp;
		this.thisPort=thisPort;
	}

	public EstablishConnection(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type=din.readInt();
		
		
        if(MessageType.values()[type]!=MessageType.ESTABLISH_CONNECTION) {
        	throw new Exception("Invalid Request: Expected "+MessageType.ESTABLISH_CONNECTION);
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
