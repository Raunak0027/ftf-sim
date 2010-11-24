package ftfsim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;



public class Server {

	private String MACAddress;
	private String IPAddress;
	private boolean alive;
	private Router connectedRouter;
	private boolean primary;
	private ArrayList<Server> otherServers;
	private Hashtable<String, Long> shoutsHeard;
	private Simulation sim;
	private Hashtable<String, String> receivedMsgs;
	
	
	// deathPeriod is the period of time in milliseconds,
	// after not hearing a shout from another server, that we
	// determine the server to be dead.
	//
	// This value should be greater than the period between shouting
	// and greater than the period between attempting to detect dead servers.
	// You can test the death detection by setting this value less than the
	// shoutDelay and detectDeadDelay
	//private long deathPeriod = 200;
	
	
	// These values are the delay between shouting and the delay between looking
	// for dead servers.
	// NOTE: These values should ideally not be equal as the shouting and detecting threads
	// will be working on the server lists at the same time.
	private long shoutDelay = 50;
	private long detectDeadDelay = 60;
	
	
	public long getDeathPeriod(){
		return sim.getDeathPeriod();
	}
	
	public long getShoutDelay(){
		return shoutDelay * sim.getSimRate();
	}
	
	public long getDetectDeadDelay(){
		return detectDeadDelay * sim.getSimRate();
	}
	
	Server(Router router, String MAC, Simulation sim){
		MACAddress = new String(MAC);
		IPAddress = null;
		this.sim = sim;
		alive = true;
		primary = false;
		connectedRouter = router;
		otherServers = new ArrayList<Server>();
		shoutsHeard = new Hashtable<String, Long>();
		receivedMsgs = new Hashtable<String, String>();
		
		
		router.addNode(this);
		
	}
	
	private void writeToConsole(String msg){
		
		sim.getConsoleTextArea().append(msg + "\n");
		if(sim.getChckbxAutoscroll().isSelected()){
			sim.getConsoleTextArea().setCaretPosition(sim.getConsoleTextArea().getDocument().getLength());
		}
	}

	private void writeToDeathConsole(String msg){
		
		sim.getDeathConsole().append(msg + "\n");		
		sim.getDeathConsole().setCaretPosition(sim.getDeathConsole().getDocument().getLength());
		
	}
	
	
	public String getMAC(){
		return MACAddress;
	}
	
	
	
	public void setIP(String ip){
		IPAddress = ip;
		connectedRouter.allocateIP(this.IPAddress, this.MACAddress);
	}
	
	public String getIP(){
		return IPAddress;
	}
	
	public void setPrimary(){
		primary = true;
		String msg = "(" + this.getIP() + ") Server: " + this.getMAC() + " has been set as primary";
		this.writeToConsole(msg);
	}
	
	public boolean getPrimary(){
		return primary;
	}
	
	public void setBackup(){
		primary = false;
		String msg = "(" + this.getIP() + ") Server: " + this.getMAC() + " has been set as backup";
		this.writeToConsole(msg);
	}
	
	public void knowServer(Server otherServer){
		
		
		if(!otherServers.contains(otherServer)){
			otherServers.add(otherServer);
			String msg = "(" + this.getIP() + ") Server: " + this.getMAC() + " now knows server: " 
			+ otherServer.getMAC() + " at IP: " + otherServer.getIP();
			
			this.writeToConsole(msg);
			this.updateShoutHeard(otherServer.getIP());
		}

		
	}
	
	public void forgetServer(Server otherServer){
		otherServers.remove(otherServer);
	}
	
	public void sendPacket(Packet packet){
		connectedRouter.packetOut(packet);
	}
	
	
	
	public void receivePacket(Packet packet){
		
		if(alive){
			
			System.out.println("Received packet.");
			
			if(receivedMsgs.get(packet.getSource())==null){
				receivedMsgs.put(packet.getSource(), packet.getPayload());
			}else{
				receivedMsgs.put(packet.getSource(), receivedMsgs.get(packet.getSource()) + packet.getPayload());
			}
			
			
			
			if(packet.getPosition()+1==packet.getTotal()){
				System.out.println("Got Final Packet!");
				String finalMsg = receivedMsgs.get(packet.getSource());
				receivedMsgs.remove(packet.getSource());
				System.out.println("Final Message: " + finalMsg);
				
				// Send result
				System.out.println("Sending result to client...");
				
				String msg = "This is a reply. PLEASE WORK!!!!!";
				Packet[] replyPackets = createPackets(msg, packet.getSource());
				
				int packetSendCount = 0;
				while(packetSendCount < replyPackets.length){
					sendPacket(replyPackets[packetSendCount]);
					packetSendCount++;
				}
				
			}else{
				// Send ACK
				System.out.println("Current total message: " + receivedMsgs.get(packet.getSource()));
				System.out.println("Server Sending ACK back to client");
				Packet packetReply = new Packet("SERVER", packet.getSource(), "ACK", 0, 1);
				sendPacket(packetReply);
			}
			
			
			

		}
		
		
	}
	
	
	private Packet[] createPackets(String msgToSend, String dest)
	{
		// create packets based on the string and store
		double lengthOfMsg = (double) msgToSend.length();
		double numberOfPackets = (lengthOfMsg/3);
		numberOfPackets = Math.ceil(numberOfPackets);
		
		System.out.println(numberOfPackets);
		
		int index = (int) numberOfPackets;
		
		System.out.println(index);
		Packet[] packetArray = new Packet[index];
		int counter = 0;
		
		while(msgToSend.length() != 0)
		{
			if(msgToSend.length() >= 3)
			{
				// extract three and remove from string
				Packet packet = new Packet("SERVER", dest, msgToSend.substring(0, 3),counter, index);
				msgToSend = msgToSend.substring(3, msgToSend.length());
				packetArray[counter] = packet;
			}
			
			else if(msgToSend.length() == 2)
			{
				// extract 2 and remove
				Packet packet = new Packet("SERVER", dest, msgToSend.substring(0, 2),counter, index);
				msgToSend = "";
				packetArray[counter] = packet;
			}
			
			else
			{
				//extract one and remove
				Packet packet = new Packet("SERVER", dest, msgToSend.substring(0, 1),counter, index);
				msgToSend = "";
				packetArray[counter] = packet;
			}
			
			counter++;
		}
		return packetArray;
	}
	
	public void shoutOut(){
		int numberOfOtherServers = otherServers.size();
		int shoutCount = 0;
		
		while(shoutCount < numberOfOtherServers){
			String msg = "(" + this.getIP() + ") Shouting to: " + otherServers.get(shoutCount).getIP();
			this.writeToConsole(msg);

			try{
				otherServers.get(shoutCount).hearShout(this.getIP());
			}catch(Exception e){
				
			}
			shoutCount++;
			numberOfOtherServers = otherServers.size();
		}
		
		
	}
	
	
	
	public void hearShout(String source){
		String msg = "(" + this.getIP() + ") Shout heard from: " + source;
		this.writeToConsole(msg);
		
		updateShoutHeard(source);
	}
	
	private void updateShoutHeard(String ip){
		Calendar currentTime = Calendar.getInstance();
		Long currentTimeInMillis = currentTime.getTimeInMillis();
		shoutsHeard.remove(connectedRouter.getMAC(ip));
		shoutsHeard.put(connectedRouter.getMAC(ip), currentTimeInMillis);
		String msg = "(" + this.getIP() + ") Shout recorded from: " + ip + " at time: " + currentTimeInMillis;
		
		this.writeToConsole(msg);
		
	}
	
	public void killServer(){
		this.alive = false;
	}
	
	private void removeDeads(){
		
		int totalOtherServers = this.otherServers.size();
		int loopCount = 0;
		
		serverListLoop:
		while(loopCount<totalOtherServers){
			Calendar currentTime = Calendar.getInstance();
			Long currentTimeInMillis = currentTime.getTimeInMillis();
			
			String mac = new String(otherServers.get(loopCount).getMAC());
			String ip = new String(otherServers.get(loopCount).getIP());

			if(currentTimeInMillis > (shoutsHeard.get(mac) + this.getDeathPeriod())){
				
				String msg = "!!! (" + this.getIP() + ") Server: " + mac + " at " + ip + " is dead. Removing from known servers!";
				
				writeToDeathConsole(msg);
				
				otherServers.remove(loopCount);
				break serverListLoop;
			}else{
				//System.out.println("(" + this.getIP() + ") ("+ currentTimeInMillis +") Server at " 
				//		+ ip + " not detected as dead. Last shout heard at " + shoutsHeard.get(mac));
			}
			
			loopCount++;
		}
		
	}
	
	
	
	
	
	
	public void giveLife(){
		
		
		/* 
		 * This is where we define the server's internal processes
		 * for e.g. doShouts, removeDeadServers, etc...
		 *
		 * Note: when defining processes - ensure that the main code
		 * is wrapped in "while(alive){ ... }" so that processes only
		 * run when the server is functional. Also, within the loop,
		 * ensure that there is at least one "Thread.sleep(...);" 
		 * statement to avoid high CPU usage. This statement has
		 * to be wrapped in a try/catch block.
		 * 
		 */
		
		this.alive = true;
		
		Runnable doShouts = new Runnable() {
			public void run() {
				
				int numberOfOtherServers = otherServers.size();
				int count = 0;
				
				while(count < numberOfOtherServers){

					try{
						otherServers.get(count).hearShout(getIP());
					}catch(Exception e){
						
					}
					count++;
					numberOfOtherServers = otherServers.size();
				}
				
				
				while(alive){
					
					try{
						shoutOut();
						Thread.sleep(getShoutDelay());
					}catch(Exception e){
						// do nothing
					}
					
					//System.out.print(alive);
				}
			}
		};
		
		
		Runnable removeDeadServers = new Runnable() {
			public void run(){
				while(alive){
					try{
						removeDeads();
						//writeToConsole("Removing Deads");
						
						Thread.sleep(getDetectDeadDelay());
					}catch(Exception e){
						
					}
					//System.out.print(alive);
				}
			}
		};
		
		
		// Initialise and start threads
		Thread doShoutsThread = new Thread(doShouts);
		Thread removeDeadServersThread = new Thread(removeDeadServers);
		doShoutsThread.start();
		removeDeadServersThread.start();
		
	}
	
	
	
	
}
