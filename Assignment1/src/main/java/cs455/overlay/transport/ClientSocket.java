package cs455.overlay.transport;

import java.net.Socket;

public class ClientSocket {

	public String ip;
	public int port;
	public Socket socket;
	public String originatingIp;
	public int originatingPort;
	
	public ClientSocket(String ip, int sendPort, String originatingIp, int originatingPort, Socket socket){
		this.socket=socket;

	}

	public ClientSocket(String ip, int sendPort, Socket socket){
		this.ip=ip;
		this.port=sendPort;
		this.socket=socket;

		
	}
}
