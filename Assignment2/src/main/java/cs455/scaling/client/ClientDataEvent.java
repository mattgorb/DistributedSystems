package cs455.scaling.client;

import java.nio.channels.SocketChannel;


public class ClientDataEvent {
	public Client client;
	public SocketChannel socket;

	
	public ClientDataEvent(Client client, SocketChannel socket) {
		this.client = client;
		this.socket = socket;
	}
}
