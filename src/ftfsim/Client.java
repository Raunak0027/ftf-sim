package ftfsim;

public class Client {
	
	String ClientId;
	Router router;
	Simulation sim;
	Client(String id,Router router, Simulation sim){
		ClientId = new String(id);
		this.router = router;
		this.sim = sim;
	}
	
	public void sendPacket(Packet packet) {
		router.packetIn(packet);
	}
	
	public void receivePacket(Packet packet) {
		System.out.println("Got it");
		// Say if it's Ack or smth
	}
	
	public void testMethod(){
		Packet packet1 = new Packet(ClientId, "SOME DESTINATION", "Packet number 1 contents", 1, 3);
		Packet packet2 = new Packet(ClientId, "SOME DESTINATION", "Packet number 2 contents", 2, 3);
		Packet packet3 = new Packet(ClientId, "SOME DESTINATION", "Packet number 3 contents", 3, 3);
		
		this.sendPacket(packet1);
		this.sendPacket(packet2);
		this.sendPacket(packet3);
	}
	
	

}
