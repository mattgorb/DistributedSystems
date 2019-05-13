package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.transport.ClientSocket;
import cs455.overlay.wireformats.Event;

public interface Node {
	public void onEvent(Event event) throws UnknownHostException, IOException ;
	public void addSender(ClientSocket socket);
	public void setCurrent(ClientSocket sock);
	public ClientSocket  socketInList(String ip, int port) ;
}
