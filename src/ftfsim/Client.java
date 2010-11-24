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
		//System.out.println("Got it");
		// Say if it's Ack or smth
		
		if(packet.getPayload().contentEquals("ACK"))
		{
			// server ACK
			// check if nemore packets
			// if any then send otherwise do nothing
			
			sim.writeToClientConsole(new Integer(ClientId),"Client received ACK");
			
			if(packetCounter < packetArray.length)
			{
				// send the next packet
				sendPacket(packetArray[packetCounter++]);
			}
			
			else
			{
				// do nothing - wait for output
			}
		}else
		{
			// arrange packets in a list
			System.out.println("Here????");
			assemblePacket(packet);
		}
	}
	
	public void testMethod(){
		startSending("This is a message. A much longer message... VERY LONG MESSAGE. Blah blah blah....");
	}
	
	private void createPackets(String msgToSend)
	{
		// create packets based on the string and store
		double lengthOfMsg = (double) msgToSend.length();
		double numberOfPackets = (lengthOfMsg/3);
		numberOfPackets = Math.ceil(numberOfPackets);
		
		System.out.println(numberOfPackets);
		
		int index = (int) numberOfPackets;
		
		System.out.println(index);
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
		System.out.println("Entered assemblePacket()");
		receivedArray = new Packet[receivedPacket.getTotal()];
		receivedArray[receivedPacket.getPosition()] = receivedPacket;
		
		int checkCounter = 0;
		boolean noPacketsLeft = true;
		
		
		while(checkCounter < receivedArray.length)
		{
			System.out.println("Checking for more packets...");
			if(receivedArray[checkCounter] == null)
			{
				noPacketsLeft = false;
				System.out.println("Still packets left?");
			}
			checkCounter++;
		}
		
		if(noPacketsLeft)
		{
			checkCounter = 0;
			
			while(checkCounter < receivedArray.length)
			{
				receivedMessage = receivedMessage + receivedArray[checkCounter++].getPayload();
			}
			
			sim.writeToClientConsole(new Integer(ClientId), "Message received: " + receivedMessage);
		}
	}
}
