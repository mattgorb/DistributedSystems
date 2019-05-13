package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskInitiate implements Event{
	private final MessageType message=MessageType.TASK_INITIATE;
	public int rounds;
	
	public TaskInitiate(int rounds) {
		this.rounds=rounds;
	}
	
	public TaskInitiate(byte[] marshalledBytes) throws Exception {	
		
		ByteArrayInputStream baInputStream =new ByteArrayInputStream(marshalledBytes);
		DataInputStream din=new DataInputStream(new BufferedInputStream(baInputStream));
		int type;

		type = din.readInt();
		
		this.rounds=din.readInt();
		
        if(MessageType.values()[type]!=MessageType.TASK_INITIATE) {
        	throw new Exception("Invalid Request: Expected "+MessageType.TASK_INITIATE);
        }					
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(this.message.ordinal());	
		dout.writeInt(this.rounds);	
		
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
