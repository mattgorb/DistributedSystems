TO RUN:
1.  cd assignment1
2.  gradle build
3.  cd build/classes/java/main
4.  java cs455.overlay.node.Registry (integer of unused port)
5.  java cs455.overlay.node.MessagingNode (RegistryIP) (RegistryPORT)
6.  Registry console: setup-overlay 4
7   Registry console: send-overlay-link-weights
8.  Registry console: start 5000

**I am electing to keep a log in my main run method which writes to the console every 5000 messages sent.  Run time is about 30-35 seconds for 5,000 rounds.  

Notes: 

No libraries used, Gradle file contains 'java-library' for unit testing purposes. I tested this program locally and on 5 remote CS machines/10 total instances several times.  I tested up to 50,000 rounds/250,000 total messages sent per node and everything ran ok locally, 25,000 rounds/125,000 total messages sent per node on remote machines and everyone was good.  I am logging the number of messages sent every 5,000 messages, this shouldn't overload the console when testing the application but it likely slows it down a touch. 

File Descriptions:

cs455.logger 
	This file has a boolean value 'printStackTrace' set to false.  You can set too true to printStacktrace. 

cs455.overlay.dijkstra
	This file has only been tested on even numbered number of nodes and even numbered overlay numbers.  For example, I tested this on 10/4, 4/2, 8/4, and 8/2

cs455.overlay.node.MessagingNode
	This file has the brunt of the work.  printSocketList() can be used to verify bidirectional connection sockets between nodes.  To connect two nodes, I sent a request from the registry to the first node in 'linkweights'.  This creates a new connection socket and sends a MessageTest to the second node.  The second nodes server reads in the request, adds the socket to the clientSocketList, and verifies its able to find that socket.  This was the trickiest part of the assignment and had my head spinning.  
	When running the main test, I synchronize the SendMessage method, and send the entire path in the message.  getConnectionSocket_viaDestination finds the correct socket to use given the node we want to connect to.  I likely over-sychronized this file, but I wanted to be safe and make sure this worked.  

cs455.overlay.node.MessagingNodeDescription
	This file contains descriptions the port and id of each node as well as an equality file to compare objects. 

cs455.overlay.node.Node
	I needed to add a few methods to this interface to account for adding sockets to nodes via the Server file. I left these methods empty in registry file, which is a bit sloppy, but I ran out of time on this homework.  

cs455.overlay.node.Registry 
	Everything is straight forward in this file.  For generating an overlay, I only tested positive numbers of nodes.  I believe negative numbers will work, if my math is correct, however I stuck to the case that will for sure be tested.  

cs455.overlay.transport.ClientSocket
	This file contains information about a socket.  I hold the information about the client socket itself as well as the address the socket should be communicating with.  

cs455.overlay.transport.Receiver
	Very basic file for receiver thread.  fireMessage is used to output to event factory and eventually MessagingNode/Registry. 

cs455.overlay.transport.Sender. 
	Basic file for sending thread, nothing special at all here.  

cs455.overlay.transport.Server
	This file keeps information on the originating address, which is how the registry can tell whether the requested node matches its ip.  It is synchronously adds and sets ClientSocket objects to the MessagingNodes.  

Wire formats:
	BidirectionalTest, MessageTest, EstablishConnection: Tests used for connecting two nodes to each other. 
	RegisterRequest, RegisterResponse, DeregisterRequest/DeregisterResponse: Registration and deregistration. 
	Event: Interface for sending data.  All messages implement this.  
	EventFactory: For sending messages to the right place. 
	LinkInfo/LinkWeights: For node link weights
	MessageType: Enum for message types.  
	Status: Enum for status codes
	TaskComplete/TaskInitiate: Start/End run
	TrafficSummary/TrafficSummaryRequest: For requesting and sending traffic summary. 
	
	

