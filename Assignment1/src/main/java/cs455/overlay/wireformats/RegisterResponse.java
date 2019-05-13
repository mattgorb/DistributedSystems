package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Event{
	private final MessageType message=MessageType.REGISTER_RESPONSE;
	private Status status;
	public String additionalInfo;
	

	
	public RegisterResponse(Status status,String info) {
		this.status=status;

		this.additionalInfo=info;

	}

	
	public RegisterResponse(byte[] marshalledBytes) throws Exception {	
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type=din.readInt();

        if(MessageType.values()[type]!=MessageType.REGISTER_RESPONSE) {
        	throw new Exception("Invalid Request: Expected "+MessageType.REGISTER_RESPONSE);
        }

		int status=din.readInt();
		

		
        //read info
        int infoLengt = din.readInt();		
		byte[] info = new byte[infoLengt];
		din.readFully(info);
		this.additionalInfo=new String(info);
		
		
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
		dout.writeInt(this.status.ordinal());
		
		byte[] infoBytes = this.additionalInfo.getBytes();
		int infoLength = infoBytes.length;
		dout.writeInt(infoLength);
		dout.write(infoBytes);
		
			
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		}
}
