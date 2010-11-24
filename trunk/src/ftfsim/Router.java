package ftfsim;

import java.util.Hashtable;

public class Router {
	
	private Hashtable<String, String> IPTable;
	private Hashtable<String, Server> nodeTable;
	private Hashtable<String, Client> clientTable;
	private Simulation sim;
	
	Router(Simulation sim){
		IPTable = new Hashtable<String, String>();
		nodeTable = new Hashtable<String, Server>();
		clientTable = new Hashtable<String, Client>();
		this.sim = sim;
		writeToConsole("Router initiated with IPTable and NodeTable");
	}
	
	private void writeToConsole(String msg){
		
		sim.getRouterConsole().append(msg + "\n");		
		sim.getRouterConsole().setCaretPosition(sim.getRouterConsole().getDocument().getLength());
		
	}
	
	public void packetIn(Packet packet){
		String MAC = IPTable.get("192.168.1.1");
		Server dest = nodeTable.get(MAC);
		dest.receivePacket(packet);
	}
	
	public void packetOut(Packet packet){
		String clientId = packet.getDest();
		System.out.println(clientId);
		Client client = clientTable.get(clientId);
		client.receivePacket(packet);
	}
	
	public void addNode(Server server){
		nodeTable.put(server.getMAC(), server);
		String msg = "Machine added to network: " + server.getMAC();
		this.writeToConsole(msg);
	}
	
	public void removeNode(Server server){
		nodeTable.remove(server.getMAC());
		String msg = "Machine removed from network: " + server.getMAC();
		this.writeToConsole(msg);
	}
	
	public void allocateIP(String ip, String mac){
		IPTable.remove(ip);
		IPTable.put(ip, mac);
		sim.getIpTableModel().addRow(new Object[]{mac, ip});
		String msg = "IP: " + ip + " allocated to: " + mac;
		this.writeToConsole(msg);
	}
	
	public void deallocateIP(String ip){
		IPTable.remove(ip);
		String msg = "IP: " + ip + " deallocated";
		this.writeToConsole(msg);
	}
	
	public String getMAC(String ip){
		return IPTable.get(ip);
	}

}
