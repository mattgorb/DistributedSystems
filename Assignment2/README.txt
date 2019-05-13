TO RUN:
1.  cd assignment2
2.  gradle build
3.  cd build/classes/java/main
4.  java cs455.scaling.server.Server (unused port) 10 10 10
5.  java cs455.scaling.client.Client (RegistryIP) (RegistryPORT) 4
6.  Registry console: setup-overlay 4
7   Registry console: send-overlay-link-weights
8.  Registry console: start 5000

**Only batching the writes, not the reads.  I had a bug trying to batch the reads where messages were being lost when batch_size>number of clients.  

Notes: 
No libraries used, Gradle file contains 'java-library' for unit testing purposes. I tested this program locally and on CS lab machines using the script provided in this direction titled pa2_script.sh.  

File Descriptions:

cs455.logger 
	This file has a boolean value 'printStackTrace' set to false.  You can set too true to printStacktrace. 

cs455.scaling.Client
	The main client code used to startup the sender/receiver threads and handle connections.  
cs455.scaling.client.ClientDataEvent
	Used for handling an event in the 
cs455.scaling.client.ClientStatistics
	Every 20 seconds this thread fires, counting the number of messages sent and messages received.  
cs455.scaling.client.ResponseHandler
	Handles responses and holds hashes.  
cs455.scaling.client.Sender
	Main sending code.  Included in here is the thread.sleep(1000/message rate).  
cs455.scaling.client.SHA1
	Provided code in assignment sheet.   

cs455.scaling.server.ClientTracker
	This tracks clients per second.  This is used for the ServerStatistics thread to compute per-second statistics.  
Cs455.scaling.server.Server
cs455.scaling.server.ServerStatistics
	This thread queries the client tracker every 20 seconds and performs calculations on the mean and standard deviation of each client.  
cs455.scaling.server.Task
	A task is an event.  Java NIO read, writes, and accepts here, and when a worker thread executes this the task is run.  
cs455.scaling.server.ThreadPoolManager
	Manages the threads and allocates the workers.  This is where the code for batch size and batch time is.  
cs455.scaling.server.WorkerThread
	The worker thread executes tasks and are handled by the threadpoolmanager.  
	
	

