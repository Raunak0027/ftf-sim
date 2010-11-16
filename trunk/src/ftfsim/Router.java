package ftfsim;

import java.util.Hashtable;

public class Router {
	
	private Hashtable<String, String> IPTable;
	private Hashtable<String, Server> nodeTable;
	private Simulation sim;
	
	Router(Simulation sim){
		IPTable = new Hashtable<String, String>();
		nodeTable = new Hashtable<String, Server>();
		this.sim = sim;
		writeToConsole("Router initiated with IPTable and NodeTable");
	}
	
	private void writeToConsole(String msg){
		
		sim.getRouterConsole().append(msg + "\n");		
		sim.getRouterConsole().setCaretPosition(sim.getRouterConsole().getDocument().getLength());
		
	}
	
	public void packetIn(String source, String destination, String packet){
		
	}
	
	public void packetOut(String source, String destination, String packet){
		
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
