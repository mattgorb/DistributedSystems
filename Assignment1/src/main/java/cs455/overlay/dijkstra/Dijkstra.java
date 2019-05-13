package cs455.overlay.dijkstra;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cs455.logger.Logger;
import cs455.overlay.node.MessagingNodeDescription;
import cs455.overlay.wireformats.LinkInfo;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodesList;

public class Dijkstra {
	MessagingNodesList connectedNodes;
	LinkWeights weights;
	
	String ip;
	int port;
	
	//ip,port=currentNode
	//nodes=connection
	public Dijkstra(String ip, int port,MessagingNodesList connectedNodes, LinkWeights weights) {
		this.ip=ip;
		this.port=port;
		this.connectedNodes=connectedNodes;
		this.weights=weights;
	}
	
	public void minDistance(MessagingNodeDescription currentNode) {
		
	}
	
	public Map<MessagingNodeDescription, Integer> findConnectedNodes(MessagingNodeDescription current) {
		Map<MessagingNodeDescription, Integer> connections = new HashMap<MessagingNodeDescription, Integer>();

		for(LinkInfo l:this.weights.linkInfo) {
			if(l.ip1.equalsIgnoreCase(current.getIp()) && l.port1==current.getPort()) {
				//connections.add(l)
				connections.put(new MessagingNodeDescription(l.ip2,l.port2),l.weight);
			}
			if(l.ip2.equalsIgnoreCase(current.getIp()) && l.port2==current.getPort()) {
				//connections.add(l)
				connections.put(new MessagingNodeDescription(l.ip1,l.port1),l.weight);
			}
		}
		return connections;
	}
	
	
	public Map<MessagingNodeDescription, Integer> getAllNodes() {
		Map<MessagingNodeDescription, Integer> nodes = new HashMap<MessagingNodeDescription, Integer>();
		for(LinkInfo l:this.weights.linkInfo) {
			MessagingNodeDescription link1=new MessagingNodeDescription(l.ip1,l.port1);
			MessagingNodeDescription link2=new MessagingNodeDescription(l.ip2,l.port2);			
			//if(!nodes.containsKey(link1)) {
			if(!nodes.keySet().stream().anyMatch(x -> x.getIp().equals(link1.getIp()) && x.getPort()==link1.getPort())){
				nodes.put(link1,Integer.MAX_VALUE);
			}
			if(!nodes.keySet().stream().anyMatch(x -> x.getIp().equals(link2.getIp()) && x.getPort()==link2.getPort())){
				nodes.put(link2,Integer.MAX_VALUE);
			}
		}
		return nodes;
	}
	
	public ArrayList<MessagingNodeDescription> nodeList() {
		ArrayList<MessagingNodeDescription> nodes = new ArrayList<MessagingNodeDescription>();
		for(LinkInfo l:this.weights.linkInfo) {
			MessagingNodeDescription link1=new MessagingNodeDescription(l.ip1,l.port1);
			MessagingNodeDescription link2=new MessagingNodeDescription(l.ip2,l.port2);			
			//if(!nodes.contains(link1)) {
			if(!nodes.stream().anyMatch(x -> x.getIp().equals(link1.getIp()) && x.getPort()==link1.getPort())){
				nodes.add(link1);
			}
			if(!nodes.stream().anyMatch(x -> x.getIp().equals(link2.getIp()) && x.getPort()==link2.getPort())){
				nodes.add(link2);
			}
		}
		return nodes;
	}
	
	
	public Map.Entry<MessagingNodeDescription, Integer> getMinEntry(Map<MessagingNodeDescription, Integer> map,ArrayList<MessagingNodeDescription> nodeList) {
		
		Map.Entry<MessagingNodeDescription, Integer> minEntry = null;
		for (Map.Entry<MessagingNodeDescription, Integer> entry : map.entrySet())
		{
		    if (minEntry == null || entry.getValue().compareTo(minEntry.getValue())<=0)
		    {
		    	//if(nodeList.contains(entry)
		    		if(nodeList.stream().anyMatch(x -> x.getIp().equals(entry.getKey().getIp()) && x.getPort()==entry.getKey().getPort()))
		    		{
		    			minEntry = entry;
		    		}
		    }
		}
		return minEntry;
		
	}
	
	public MessagingNodeDescription getMatchingObject(Set<MessagingNodeDescription> nodes,MessagingNodeDescription object ) {
		for(MessagingNodeDescription entry:nodes) {
			if(entry.getIp().equals(object.getIp()) && entry.getPort()==object.getPort()){
				return entry;
			}
		}
		return null;
	}
	
	
	public Map<MessagingNodeDescription, List<MessagingNodeDescription>> compute() {
		try {
			MessagingNodeDescription node=null;
			
			Map<MessagingNodeDescription, Integer> allNodes=getAllNodes();//list of shortest paths from each node to source node. 

			Map<MessagingNodeDescription, List<MessagingNodeDescription>> paths = new HashMap<MessagingNodeDescription, List<MessagingNodeDescription>>();
			
			for(Map.Entry<MessagingNodeDescription, Integer> entry:allNodes.entrySet()) {
				paths.put(entry.getKey(), new ArrayList<MessagingNodeDescription>());
			}

			allNodes.put(getMatchingObject(allNodes.keySet(),new MessagingNodeDescription(this.ip,this.port)),0);

			ArrayList<MessagingNodeDescription> nodeList=nodeList();
			while(nodeList.size()>0) {
				Map.Entry<MessagingNodeDescription, Integer> minEntry = getMinEntry(allNodes, nodeList);		

				for(MessagingNodeDescription n:nodeList) {
					if(n.getIp().equalsIgnoreCase(minEntry.getKey().getIp()) && n.getPort()==minEntry.getKey().getPort()) {
						nodeList.remove(n);
						break;
					}
				}
				Map<MessagingNodeDescription, Integer> neighbors=findConnectedNodes(minEntry.getKey());

				for(Map.Entry<MessagingNodeDescription, Integer> entry:neighbors.entrySet()) {
					int dist=allNodes.get(minEntry.getKey())+entry.getValue();

					node=getMatchingObject(allNodes.keySet(),entry.getKey());

					if(dist<allNodes.get(node)) {
						List<MessagingNodeDescription> p=new ArrayList<MessagingNodeDescription>();
						p.addAll(paths.get(minEntry.getKey()));
						p.add(entry.getKey());

						allNodes.put(node, dist);
						paths.put(node,p);
					}
					
				}
			}
			
			/*System.out.println("test:");
			System.out.println("THIS PORT: "+this.port);

			for(Map.Entry<MessagingNodeDescription, Integer> entry:allNodes.entrySet()) {
				System.out.println("NODE: "+entry.getKey().getPort()+" dist:"+entry.getValue());
			}
			System.out.println("ACTUAL CONNECTIONS: ");
			for(MessagingNodeDescription n: this.connectedNodes.connections) {
				System.out.println("NODE: "+n.getPort());
			}
			System.out.println("\nPATHS:");
			for(Map.Entry<MessagingNodeDescription, List<MessagingNodeDescription>> entry:paths.entrySet()) {
				System.out.println("NODE: "+entry.getKey().getPort());
				System.out.println("size: "+entry.getValue().size());
				for(int i=0;i< entry.getValue().size();i++) {
					System.out.println("Goes to: "+entry.getValue().get(i).getPort());
				}
				
			}
			System.out.println("\nEND");*/
			return paths;
		}catch(Exception e) {
			Logger.write_errors(getClass().getName(),"Dijkstra", e.getClass().toString(), e);
			return null;
		}
		
		
        
	}
}
