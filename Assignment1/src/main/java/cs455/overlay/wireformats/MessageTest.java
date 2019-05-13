package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageTest implements Event {
	private final MessageType message=MessageType.MESSAGE_TEST;
	public int port;
	public String ip;
	
	public String socketIp;
	public int socketPort;
	
	
	public MessageTest(String ip, int port,String socketIp, int socketPort) {
		this.port=port;
		this.ip=ip;
		this.socketPort=socketPort;
		this.socketIp=socketIp;
	}
	
	public  MessageTest(byte[] marshalledBytes) throws Exception {	
		
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type;

		type = din.readInt();
		this.port=din.readInt();
		
        //read ip
        int ipLength = din.readInt();		
		byte[] ip = new byte[ipLength];
		din.readFully(ip);
		this.ip=new String(ip);	
		
		
		
		
		
		this.socketPort=din.readInt();
		
        //read ip
        int sockLength = din.readInt();		
		byte[] sockip = new byte[sockLength];
		din.readFully(sockip);
		this.socketIp=new String(sockip);	
		
		
		
		
		
        if(MessageType.values()[type]!=MessageType.MESSAGE_TEST) {
        	throw new Exception("Invalid Request: Expected "+MessageType.MESSAGE_TEST);
        }					
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(this.message.ordinal());	
		dout.writeInt(this.port);	
		
		byte[] ipBytes = this.ip.getBytes();
		int ipLength = ipBytes.length;
		dout.writeInt(ipLength);
		dout.write(ipBytes);
		
		
		
		dout.writeInt(this.socketPort);	
		
		byte[] sock = this.socketIp.getBytes();
		int sockLength = sock.length;
		dout.writeInt(sockLength);
		dout.write(sock);
		
		
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
