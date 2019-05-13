package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cs455.logger.Logger;

public class Sender {

	
    public Socket sendToSocket;
    private DataOutputStream dout;
	
	public Sender(Socket socket) {

			this.sendToSocket=socket;
			try {
				this.sendToSocket.setSendBufferSize(1000000);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	public void send(byte[] data) {
		try {

			dout=new DataOutputStream(this.sendToSocket.getOutputStream());
			//synchronized (this.sendToSocket) {
				int dataLength = data.length;
				dout.writeInt(dataLength);
				dout.write(data, 0, dataLength);
				dout.flush();		

			//}

		} catch(SocketException e) {
			Logger.write_errors(getClass().getName(),"send", e.getClass().toString(), e);
			return;
		}
		catch (IOException e) {
			
			Logger.write_errors(getClass().getName(),"send", e.getClass().toString(), e);
		}
	}
	

}
