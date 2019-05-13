package cs455.scaling.client;

public class ResponseHandler {
	private byte[] rsp = null;
	
	private static boolean done=false;
	public synchronized boolean handleResponse(Sender sender,byte[] rsp) {
		this.rsp = rsp;
		if(sender.hash.contains(new String(this.rsp))) {
			sender.hash.remove(new String(this.rsp));
		}

		//this.notify();
		return true;
	}
	public void setDone() {
		done=true;
	}
	
	public synchronized void waitForResponse(Sender sender) {
		while(!done) {
			try {
				this.wait();
				
			} catch (InterruptedException e) {
			}
		}
	}
}
