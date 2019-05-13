package cs455.scaling.server;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadPoolManager implements Runnable
{	
	public ConcurrentLinkedQueue<WorkerThread> workerQueue=new ConcurrentLinkedQueue<WorkerThread>();
	
	
	private final int numThread;
	private Server server;

	private int batchSize;
	private long batchTime;
	
	private static boolean done=false;
	private long start_seconds_read;
	private long start_seconds_write;
	//private long timeDiff;
	//public ArrayList<WorkerThread> workers=new ArrayList<WorkerThread>();

	ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
	
	ConcurrentLinkedQueue<Runnable> read = new ConcurrentLinkedQueue<Runnable>();
	ConcurrentLinkedQueue<Runnable> write = new ConcurrentLinkedQueue<Runnable>();
	
	public ThreadPoolManager(int numThread, int batchSize, int batchTime, Server server) {
		this.numThread=numThread;
		this.server=server;
		this.batchSize=batchSize;
		this.batchTime=batchTime;
		//this.resetTime_read();
		this.resetTime_write();
	}
	
	public synchronized void resetTime_read() {
		Date start=new Date();
		this.start_seconds_read=start.getTime();

	}
	public synchronized void resetTime_write() {
		Date start=new Date();
		this.start_seconds_write=start.getTime();


	}
	public synchronized long getDiff(long sec) {
		
		Date d = new Date();
		long x= (d.getTime()-sec)/1000;
		return x;
	}
	
	public  void addWrite(Runnable task) {
		write.add(task);
		if(write.size()>=this.batchSize) {
				resetTime_write();
            	
				WorkerThread t=this.workerQueue.poll();
				if(t!=null) {
					ConcurrentLinkedQueue<Runnable> tempQueue = new ConcurrentLinkedQueue<Runnable>();
					for(int i=0;i<this.batchSize;i++) {
						Runnable topTask=write.poll();
						
						if(topTask!=null) {
							
							tempQueue.add(topTask);
						}
						else {
							break;
						}	
					}

					t.assign(tempQueue);
					synchronized(t){		            
						t.notify();
					}
				}

	            try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public synchronized void addRead(Runnable task) {
		read.add(task);
		//System.out.println("read: "+read.size());
		//Not batching the reads due to a bug
		//if(	read.size()>=this.batchSize) {
        	resetTime_read();

			WorkerThread t=this.workerQueue.poll();
			if(t!=null) {
				ConcurrentLinkedQueue<Runnable> tempQueue = new ConcurrentLinkedQueue<Runnable>();
				for(int i=0;i<this.batchSize;i++) {
					Runnable topTask=read.poll();
					
					if(topTask!=null) {
						
						tempQueue.add(topTask);
					}
					else {
						break;
					}	
				}
				t.assign(tempQueue);
				synchronized(t){		            
					t.notify();
				}
			}
	//	}
	}

	
	public synchronized void addAccept(Runnable task) {
		queue.add(task);
		//not batching accept calls due to a bug.  
		//if(queue.size()>=this.batchSize) {
            
			WorkerThread t=this.workerQueue.poll();
			if(t!=null) {
				ConcurrentLinkedQueue<Runnable> tempQueue = new ConcurrentLinkedQueue<Runnable>();
				for(int i=0;i<this.batchSize;i++) {
					Runnable topTask=queue.poll();

					if(topTask!=null) {
						tempQueue.add(topTask);
					}
					else {
						//System.out.println("HERE"); 
						break;
					}	
				}
				t.assign(tempQueue);
				
				
				synchronized(t){		            
					t.notify();
				}
			}

		//}
	}
	public void open() {
		for(int i=0;i<numThread;i++) {
			///WorkerThread worker=new WorkerThread(queue,new String("Thread "+i),this);
			WorkerThread worker=new WorkerThread(new String("Thread "+i), this,2);
			Thread t=new Thread(worker);
			t.start();
			//workers.add(worker);
			
			this.workerQueue.add(worker);
		}
		
	}


	public void setDone() {
		done=true;
		for(WorkerThread w:this.workerQueue) {
			w.setDone();
		}
	}

	
	
	public synchronized void checkQueues(ConcurrentLinkedQueue<Runnable> queueToCheck, long diff, boolean write) {
		try {
				WorkerThread t=this.workerQueue.poll();
	            
				if(t!=null) {
					ConcurrentLinkedQueue<Runnable> tempQueue = new ConcurrentLinkedQueue<Runnable>();
					while(queueToCheck!=null) {
						Runnable topTask=queueToCheck.poll();
						
						if(topTask!=null) {
							
							tempQueue.add(topTask);
						}
						else {
							break;
						}	
					}

					t.assign(tempQueue);
					synchronized(t){		            
						t.notify();
					}
				}

		            try {
						 Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


		}catch(Exception e) {
			System.out.println(e);
		}

	}

	@Override
	public void run() {
		
		while(!done) {
			synchronized(this) {
				//not batching the reads due to a bug.  
				/*long timeDiff=getDiff(this.start_seconds_read);
				if(timeDiff>this.batchTime) {
					System.out.println("HERE");
					if(this.read.size()<this.batchSize) {
						resetTime_read();
						checkQueues(this.read, timeDiff,false);
					}
				}*/
				
				long timeDiff=getDiff(this.start_seconds_write);
				if(timeDiff>this.batchTime) {
					//System.out.println("HERE");
					if(this.write.size()<this.batchSize) {
						resetTime_write();
						checkQueues(this.write, timeDiff,true);
					}
				}				

			}
			
		}
		
	}

	

}
