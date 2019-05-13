package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import cs455.logger.Logger;
import cs455.scaling.client.SHA1;

public class Task implements Runnable{
	public SocketChannel socket;
	public Server server;
	public SelectionKey selection;
	public byte[] data;
	public int type;
	
	
	private ByteBuffer  buff=ByteBuffer.allocate(8192);
	
	public Task(SocketChannel socket, SelectionKey selection, Server server, byte[] data, int type) {
		this.socket = socket;
		this.selection = selection;
		this.server=server;
		this.data=data;
		this.type=type;
	
		
	}

	@Override
	public void run() {
		//System.out.println(this.type);
		if(this.type==0) {
			// Clear out our read buffer so it's ready for new data
			this.buff.clear();
			// Attempt to read off the channel
			int numRead = 0;
			try {
				
				
				while(this.buff.hasRemaining() && numRead!=-1)
				{

					numRead = socket.read(this.buff);

				}
				
				
			} catch (IOException e) {
				// The remote forcibly closed the connection, cancel
				// the selection key and close the channel.
				this.selection.cancel();
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return;
			}

			
			buff.flip();
			if (numRead == -1) {

				try {
					this.selection.channel().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.selection.cancel();
				return;
			}
			
			if(numRead>0) {
				String s=SHA1.SHA1FromBytes(this.buff.array());
				this.data=s.getBytes();//new String("Hello").getBytes();
				this.server.messagesReceived++;
				addSendData(socket);
			}
			
			
			String key=this.socket.socket().getInetAddress().toString()+":"+this.socket.socket().getPort();
			this.server.incrementSent(key);

		}
		else if(this.type==1){

			synchronized (this.server.pendingData) {
				List queue = (List) this.server.pendingData.get(socket);
				// Write until there's not more data ...
				while (!queue.isEmpty()) {
					ByteBuffer buf = (ByteBuffer) queue.get(0);

					try {
						socket.write(buf);
					} catch (IOException e) {
			        	Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
					}
					if (buf.remaining() > 0) {
						// ... or the socket's buffer fills up
						break;
					}
					queue.clear();
				}
				selection.interestOps(SelectionKey.OP_READ);
			}
			this.server.keys.put(selection, false);
		}
		else {
			try {
				this.socket.register(this.server.selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
			}	
		}
	}
	
	
	public  void addSendData(SocketChannel socketChannel) {		
		
		synchronized (this.server.pendingData) {
			List queue = (List) this.server.pendingData.get(socketChannel);
			if (queue == null) {
				queue = new ArrayList();
				this.server.pendingData.put(socketChannel, queue);
			}
			queue.add(ByteBuffer.wrap(this.data));
		}
		selection.interestOps(SelectionKey.OP_WRITE);
		//System.out.println("REaddd");
	}

}
