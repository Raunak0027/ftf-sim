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
	//private Hashtable<String, String> receivedMsgs;
	private Hashtable<String, String> backupMsgs;
	private Hashtable<String, Integer> clientAckCount;
	private Hashtable<String, Integer> serverReplyCount;
	private Hashtable<String, String> primaryMsgs;
	private Hashtable<String, String> backupFullMsgs;
	private static boolean duplex=false;
	
	
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
	public static boolean getMode() {
		return duplex;
	}
	
	public static void setMode(boolean mode) {
		duplex = mode;
	}
	
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
		//receivedMsgs = new Hashtable<String, String>();
		clientAckCount = new Hashtable<String, Integer>();
		backupMsgs = new Hashtable<String, String>();
		primaryMsgs = new Hashtable<String, String>();
		serverReplyCount = new Hashtable<String, Integer>();
		backupFullMsgs = new Hashtable<String, String>();
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
			//record the ACK in hashtable
			if ((packet.getPayload().equals("ACK")) && (packet.getTotal()==1)) {
				saveAck(packet);
			} else if (isPrimary(packet)){
				savePrimary(packet);
			} else {
				saveBackup(packet);
			}
		}
		
		
	}
	
	private void saveAck(Packet packet) {
		if (clientAckCount.get(packet.getSource())==null) {
			clientAckCount.put(packet.getSource(), 0);
		} else {
			clientAckCount.put(packet.getSource(),clientAckCount.get(packet.getSource()) + 1);
		}
		
		if (primaryMsgs.get(packet.getSource())!=null) {
			sendOutput(packet.getSource());
		}
	}
	
	private void saveBackup(Packet packet) {
		//if it is a remainder of a failure
		if (primaryMsgs.get(packet.getSource())!=null) {
			savePrimary(packet);
		} else {			
			if ((backupMsgs.get(packet.getSource())==null)&&(packet.getPosition()==0)) {
				backupMsgs.put(packet.getSource(),packet.getPayload());
			} else if (backupMsgs.get(packet.getSource())!=null){
				backupMsgs.put(packet.getSource(),backupMsgs.get(packet.getSource())+ packet.getPayload());
			} else {
				
			}
			
			if ((backupMsgs.get(packet.getSource())!=null) && (packet.getPosition()+1==packet.getTotal())) {
				System.out.println("MOVED TO BACKUPFULL");
				backupFullMsgs.put(packet.getSource(),backupMsgs.get(packet.getSource()));
				backupMsgs.remove(packet.getSource());
			}
			
		}	
	}
	
	private void savePrimary(Packet packet) {
		if ((primaryMsgs.get(packet.getSource())==null)&&(packet.getPosition()==0)) {
			primaryMsgs.put(packet.getSource(),packet.getPayload());
		} else if (primaryMsgs.get(packet.getSource())!=null){
			primaryMsgs.put(packet.getSource(),primaryMsgs.get(packet.getSource())+ packet.getPayload());
		} else {
		}		
		
		//modify
		if(packet.getPosition()+1==packet.getTotal()){
			sendOutput(packet.getSource());
		}else{
			// Send ACK
			System.out.println("Current total message: " + primaryMsgs.get(packet.getSource()));
			System.out.println("Server Sending ACK back to client");
			Packet packetReply = new Packet("SERVER", packet.getSource(), "ACK", 0, 1);
			sendPacket(packetReply);
		}
	}
	
	private void sendOutput(String source) {
		System.out.println("Got Final Packet!");
		//String source = packet.getSource();
		String finalMsg = primaryMsgs.get(source);
		boolean last = false;
		//receivedMsgs.remove(packet.getSource());
		System.out.println("Final Message: " + finalMsg);
		
		// Send result
		System.out.println("Sending result to client...");
		
		//needs restructuring
		String msg = reverseString(finalMsg);
		Packet[] replyPackets = createPackets(msg, source);
		
		int packetSendCount = 0;
		System.out.println(packetSendCount);

		if (clientAckCount.get(source)!=null) {
			packetSendCount = clientAckCount.get(source)+ 1;
		}
		sendPacket(replyPackets[packetSendCount]);
		if (packetSendCount == replyPackets.length - 1) {
			primaryMsgs.remove(source);
			clientAckCount.remove(source);
			last = true;
		}
		talkToServer(source, packetSendCount,last);
	}
	
	// Tells the other server from the duo that a response has been fully sent
	//need to add String parameter to handle deletion when msg is done
	private void talkToServer(String source, int lastSentPacket, boolean last) {
		//String source = packet.getSource();
		
		if (duplex == true) {
			if (this.getIP().equals("192.168.1.1")) { 
				for (int i=0;i<=otherServers.size(); i++) {
					if (otherServers.get(i).getIP().equals("192.168.1.2")) {
						otherServers.get(i).receiveFromServer(source,lastSentPacket,last);
					}
				}
			} else 	if (this.getIP().equals("192.168.1.2")) { 
				for (int i=0;i<=otherServers.size(); i++) {
					if (otherServers.get(i).getIP().equals("192.168.1.1")) {
						otherServers.get(i).receiveFromServer(source, lastSentPacket,last);
					}
				}
			}	
		} else {
			
		}
	}
	
	//TODO
	private void receiveFromServer(String source, int lastSentPacket, boolean last) {
		if (last) {
			backupFullMsgs.remove(source);
			serverReplyCount.remove(source);
			clientAckCount.remove(source);
			System.out.println("REMOVED ");
		}
		if (backupFullMsgs.get(source)!=null) {
			serverReplyCount.put(source, lastSentPacket);
			System.out.println("CHECK");
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
	
	private boolean isPrimary(Packet packet) {
		int lastDigitServer = Integer.parseInt(this.getIP().substring(this.getIP().length() -1));
		int lastDigitClient = Integer.parseInt(packet.getSource());
		if (duplex==false) { 
			return true;
		} else {
			if (((lastDigitServer%2==0) && (lastDigitClient%2==0)) ||
				((lastDigitServer%2==1) && (lastDigitClient%2==1)))	{
				return true;
			} else {
				return false;
			}
		}	
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
	
	// ALSO SWITCHES MODES. MINOR BUG WITH SWITCH TO SIMPLEX
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
				if (otherServers.get(loopCount).getIP().equals("192.168.1.1")||
					otherServers.get(loopCount).getIP().equals("192.168.1.2")){
					duplex = false;
					System.out.println("SWITCHED TO SIMPLEx");
					System.out.println(this.getIP());
					System.out.println(loopCount);
					System.out.println(totalOtherServers);
				} 	
				//Remove from router
				connectedRouter.deallocateIP(otherServers.get(loopCount).getIP());
				connectedRouter.removeNode(otherServers.get(loopCount));
				otherServers.remove(loopCount);

				break serverListLoop;
			}else{
				//MODE SWITCHING
				if (otherServers.isEmpty()) {
					duplex = false;
				} else {
					if ((connectedRouter.getIPTable().get("192.168.1.1")!= null) &&
							(connectedRouter.getIPTable().get("192.168.1.2")!=null)) {
						duplex = true;
						System.out.println("DUPLEX MODE");
					}
				}
				//System.out.println("(" + this.getIP() + ") ("+ currentTimeInMillis +") Server at " 
				loopCount++;
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
	
	
	
	public String reverseString(String msg){
		String reverse = new StringBuffer(msg).reverse().toString();
		return reverse;
	}
}
