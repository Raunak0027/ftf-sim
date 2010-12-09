package ftfsim;

public class Client {
	
	String ClientId;
	Router router;
	Simulation sim;
	Packet[] packetArray;
	int packetCounter = 1;
	Packet[] receivedArray;
	String receivedMessage = "";
	String messageSent  = "";
	
	Client(String id,Router router, Simulation sim){
		ClientId = new String(id);
		this.router = router;
		router.addClient(this);
		this.sim = sim;
		sim.writeToClientConsole(new Integer(ClientId), "Client Created! ID: " + ClientId);
	}
	
	public void sendPacket(Packet packet) {
		router.packetIn(packet);
	}
	
	public void receivePacket(Packet packet) {
		//System.out.println("Got it");
		// Say if it's Ack or smth
		
		System.out.println("Client: Reveived Packet");
		
		if((packet.getPayload().contentEquals("ACK")) && (packet.getTotal() == 1))
		{
			// server ACK
			// check if nemore packets
			// if any then send otherwise do nothing
			System.out.println("Client: Got ACK");
			try{
			sim.writeToClientConsole(new Integer(ClientId),"Client received ACK");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if(packetCounter < packetArray.length)
			{

				// send the next packet
				try{
					Thread.sleep(500);
				}catch(Exception e){
					
				}
				sendPacket(packetArray[packetCounter++]);
			}
			
			else
			{
				// do nothing - wait for output
				//can add a timer to show waiting time
			}
		}else
		{
			// arrange packets in a list
			System.out.println("Client: Got message packet.");
			
			if(packet.getPosition()+1 != packet.getTotal())
			{
				sendPacket(new Packet(ClientId,"SOME DESTIONATION","ACK",0,1));
				System.out.println("Client: Send ACK for received result packet.");
				sim.writeToClientConsole(new Integer(ClientId), "Received Payload: \"" + packet.getPayload() +  "\" Sending ACK...");
			}
				
			try{
				Thread.sleep(500);
			}catch(Exception e){
				
			}
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
		messageSent = msgToSend;
		createPackets(msgToSend);
		sendPacket(packetArray[0]);
		packetCounter = 1;
	}
	
	private void assemblePacket(Packet receivedPacket)
	{
		System.out.println("Entered assemblePacket()");
		if(receivedArray==null){
			receivedArray = new Packet[receivedPacket.getTotal()];
		}
		receivedArray[receivedPacket.getPosition()] = receivedPacket;
		
		int checkCounter = 0;
		boolean noPacketsLeft = true;
		
		
		while(checkCounter < receivedArray.length)
		{
			System.out.println("Checking for null packets at index: " + checkCounter);
			if(receivedArray[checkCounter] == null)
			{
				noPacketsLeft = false;
				System.out.println("Packet: " + checkCounter + " is null");
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
			
			if(messageSent.contentEquals(this.reverseString(receivedMessage))){
				sim.writeToClientConsole(new Integer(ClientId), "CORRECT MESSAGE RECEIVED!! Client ID: " + ClientId);
			}
			
			 
			
			receivedArray = null;
			receivedMessage = "";
			packetCounter = 1;
		}
	}
	
	public String reverseString(String msg){
		String reverse = new StringBuffer(msg).reverse().toString();
		return reverse;
	}
	
	public String getReceivedMsg(){
		return receivedMessage;
	}
	
	public int getPacketCount(){
		return packetCounter;
	}
	
	public Packet[] getPacketArray(){
		return packetArray;
	}
}
