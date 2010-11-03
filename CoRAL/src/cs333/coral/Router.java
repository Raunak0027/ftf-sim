package cs333.coral;

import java.util.Hashtable;

public class Router {
	
	private Hashtable<String, String> IPTable;
	private Hashtable<String, Server> nodeTable;
	
	Router(){
		IPTable = new Hashtable<String, String>();
		nodeTable = new Hashtable<String, Server>();
	}
	
	public void packetIn(String source, String destination, String packet){
		
	}
	
	public void packetOut(String source, String destination, String packet){
		
	}
	
	public void addNode(Server server){
		nodeTable.put(server.getMAC(), server);
		System.out.println("(router) Machine added to network: " + server.getMAC());
	}
	
	public void removeNode(Server server){
		nodeTable.remove(server.getMAC());
		System.out.println("(router) Machine removed from network: " + server.getMAC());
	}
	
	public void allocateIP(String ip, String mac){
		IPTable.remove(ip);
		IPTable.put(ip, mac);
		System.out.println("(router) IP: " + ip + " allocated to: " + mac);
	}
	
	public void deallocateIP(String ip){
		IPTable.remove(ip);
		System.out.println("(router) IP: " + ip + " deallocated");
	}
	
	public String getMAC(String ip){
		return IPTable.get(ip);
	}

}
