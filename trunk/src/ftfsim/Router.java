package ftfsim;

import java.util.Enumeration;
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
		try {
			if (IPTable.size()==1) {
				if (IPTable.get("192.168.1.1")!=null) {
					String MAC = IPTable.get("192.168.1.1");
					Server dest = nodeTable.get(MAC);
					dest.receivePacket(packet);
				} else {
					String MAC = IPTable.get("192.168.1.2");
					Server dest = nodeTable.get(MAC);
					dest.receivePacket(packet);
				}
			} else if (IPTable.size()>=2) {
				String MAC1 = IPTable.get("192.168.1.1");
				Server dest1 = nodeTable.get(MAC1);
				dest1.receivePacket(packet);
				String MAC2 = IPTable.get("192.168.1.2");
				Server dest2 = nodeTable.get(MAC2);
				dest2.receivePacket(packet);
			} else {
				
			}
		} catch (Exception e) {
			
		}
			
	}
	
	public void packetOut(Packet packet){
		String clientId = packet.getDest();
		
		Client client = clientTable.get(clientId);
		client.receivePacket(packet);
	}
	
	public void addNode(Server server){
		nodeTable.put(server.getMAC(), server);
		String msg = "Machine added to network: " + server.getMAC();
		this.writeToConsole(msg);
	}
	
	public void addClient(Client client){
		clientTable.put(client.ClientId, client);
		this.writeToConsole("Client " + client.ClientId + " Added to Router");
	}
	
	public void removeNode(Server server){
		nodeTable.remove(server.getMAC());
		String msg = "Machine removed from network: " + server.getMAC();
		this.writeToConsole(msg);
	}
	
	public void allocateIP(String ip, String mac){
		IPTable.remove(ip);
		IPTable.put(ip, mac);
		//sim.getIpTableModel().addRow(new Object[]{mac, ip});
		String msg = "IP: " + ip + " allocated to: " + mac;
		this.writeToConsole(msg);
	}
	
	public void deallocateIP(String ip){
		
		
		
		IPTable.remove(ip);
		String msg = "IP: " + ip + " deallocated";
		/*
		int numberOfRows = sim.getIpTableModel().getRowCount();
		int count = 0;
		
		while(count<numberOfRows){
			sim.getIpTableModel().removeRow(count);
		}
		
		Enumeration<String> keys = IPTable.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            //System.out.println(" Key :" + key + " Value: " + IPTable.get(key));
            
            sim.getIpTableModel().addRow(new Object[]{IPTable.get(key), key});
        }
		*/
		this.writeToConsole(msg);
	}
	
	public String getMAC(String ip){
		return IPTable.get(ip);
	}
	
	public Hashtable getIPTable() {
		return IPTable;
	}

}
