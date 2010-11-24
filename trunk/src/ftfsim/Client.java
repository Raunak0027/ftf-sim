package ftfsim;

public class Client {
	
	String ClientId;
	Router router;
	Simulation sim;
	Packet[] packetArray;
	int packetCounter = 1;
	Packet[] receivedArray;
	String receivedMessage = "";
	
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
			
			if(packetCounter < packetArray.length)
			{
				// send the next packet
				sendPacket(packetArray[packetCounter++]);
			}
			
			else
			{
				// do nothing - wait for output
			}
		}
		
		else
		{
			// arrange packets in a list
			assemblePacket(packet);
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
	
	private void createPackets(String msgToSend)
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
	
	public void  startSending(String msgToSend)
	{
		createPackets(msgToSend);
		sendPacket(packetArray[0]);
	}
	
	private void assemblePacket(Packet receivedPacket)
	{
		receivedArray = new Packet[receivedPacket.getTotal()];
		receivedArray[receivedPacket.getPosition()] = receivedPacket;
		
		int checkCounter = 0;
		boolean noPacketsLeft = true;
		
		while(checkCounter < receivedArray.length)
		{
			if(receivedArray[checkCounter] == null)
			{
				noPacketsLeft = false;
			}				
		}
		
		if(noPacketsLeft)
		{
			checkCounter = 0;
			
			while(checkCounter < receivedArray.length)
			{
				receivedMessage = receivedMessage + receivedArray[checkCounter++].getPayload();
			}
			
			System.out.println("Message received: " + receivedMessage);
		}
	}
}
