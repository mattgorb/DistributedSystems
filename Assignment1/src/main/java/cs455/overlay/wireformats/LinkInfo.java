package cs455.overlay.wireformats;

public class LinkInfo {
	public String ip1;
	public String ip2;
	public int port1;
	public int port2;
	public int weight;
	public LinkInfo(String ip1,String ip2, int port1,int port2, int weight) {
		this.ip1=ip1;
		this.ip2=ip2;
		this.port1=port1;
		this.port2=port2;
		this.weight=weight;
	}
	

}
