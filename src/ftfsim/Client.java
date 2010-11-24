package ftfsim;

public class Client {
	
	String ClientId;
	Router router;
	Simulation sim;
	Packet[] packetArray;
	
	Client(String id,Router router, Simulation sim){
		ClientId = new String(id);
		this.router = router;
		router.addClient(this);
		this.sim = sim;
	}
	
	public void sendPacket(Packet packet) {
		router.packetIn(packet);
	}
	
	public void receivePacket(Packet packet) {
		System.out.println("Got it");
		// Say if it's Ack or smth
		
		if(packet.getPayload().contentEquals("ACK"))
		{
			// server ACK
			// check if nemore packets
			// if any then send otherwise do nothing
		}
		
		else
		{
			// arrange packets in a list
		}
	}
	
	public void testMethod(){
		Packet packet1 = new Packet(ClientId, "SOME DESTINATION", "Packet number 1 contents", 1, 3);
		Packet packet2 = new Packet(ClientId, "SOME DESTINATION", "Packet number 2 contents", 2, 3);
		Packet packet3 = new Packet(ClientId, "SOME DESTINATION", "Packet number 3 contents", 3, 3);
		
		this.sendPacket(packet1);
		this.sendPacket(packet2);
		this.sendPacket(packet3);
	}
	
	public void createPackets(String msgToSend)
	{
		// create packets based on the string and store
		int index = (msgToSend.length()/3)+1;
		packetArray = new Packet[index];
		int counter = 0;
		
		while(msgToSend.length() != 0)
		{
			if(msgToSend.length() >= 3)
			{
				// extract three and remove from string
				Packet packet = new Packet(ClientId, "SOME DESTINATION",msgToSend.substring(0, 3),counter, index);
				msgToSend = msgToSend.substring(3, msgToSend.length());
				packetArray[counter] = packet;
			}
			
			else if(msgToSend.length() == 2)
			{
				// extract 2 and remove
				Packet packet = new Packet(ClientId, "SOME DESTINATION",msgToSend.substring(0, 2),counter, index);
				msgToSend = "";
				packetArray[counter] = packet;
			}
			
			else
			{
				//extract one and remove
				Packet packet = new Packet(ClientId, "SOME DESTINATION",msgToSend.substring(0, 1),counter, index);
				msgToSend = "";
				packetArray[counter] = packet;
			}
			
			counter++;
		}
	}
}
