package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import cs455.logger.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
public class WorkerThread implements Runnable{
	//private List queue = new LinkedList();
	
	private static boolean stopped =false;
	
	private ThreadPoolManager tpm;
	
	private ConcurrentLinkedQueue queue= new ConcurrentLinkedQueue<Runnable>();
	private String name;
	private int batchSize;
	private static boolean done=false;
	
	public WorkerThread(ConcurrentLinkedQueue queue, String name, ThreadPoolManager tpm) {
		this.queue=queue;
		this.name=name;
		this.tpm=tpm;
		
		
	}
	public WorkerThread(String name, ThreadPoolManager tpm, int batchSize) {
		this.name=name;	
		this.tpm=tpm;
		this.batchSize=batchSize;
	}

	public void assign(ConcurrentLinkedQueue queue) {
		this.queue=queue;
	}
	
	public void setDone() {
		done=true;
	}

	
	
	public synchronized void run() {

		while(!done) {
               try{
                    this.wait();
               while(this.queue.size()>0) {
	           	    Task task = (Task)queue.poll();
	              	if(task!=null) {
	              		task.run();
	              		/*if(task.type==0) {
	              			task.selection.interestOps(SelectionKey.OP_READ);
	              			task.run();
	              			//task.selection.interestOps(SelectionKey.OP_WRITE);
	              		}
	              		if(task.type==1) {
	              			
	              			task.selection.interestOps(SelectionKey.OP_WRITE);
	              			task.run();
	              			//task.selection.interestOps(SelectionKey.OP_READ);
	              		}
	              		if(task.type==2) {
	              			
	              			task.selection.interestOps(SelectionKey.OP_ACCEPT);
	              			task.run();
	              			//task.socket.register(task.server.selector, SelectionKey.OP_READ);
	              		}	   */  		
	              		
	              	}      
              }
           
               this.tpm.workerQueue.add(this);


                
               } catch (InterruptedException ex) {  
		        	Logger.write_errors(getClass().getName(),"run", ex.getClass().toString(), ex);
                   
               }catch(Exception e) {
		        	Logger.write_errors(getClass().getName(),"run", e.getClass().toString(), e);
               }


		}
	}
	
	public synchronized void stop() {
		this.stopped=true;
	}
}
