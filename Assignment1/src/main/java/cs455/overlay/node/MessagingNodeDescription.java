package cs455.overlay.node;

public class MessagingNodeDescription {
	private String ip;
	private int port;
	
	
	public MessagingNodeDescription(String ip, int port){
		this.setIp(ip);
		this.setPort(port);
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public boolean compare(String ip, int port) {
		return this.ip.contentEquals(ip) && this.port==port;
	}
	
	public boolean compareObject(MessagingNodeDescription node) {
		return this.ip.contentEquals(node.ip) && this.port==node.port;
	}
	
}
