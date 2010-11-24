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
	
	public void sendData(Packet packet){
		
	}
	
	public void receiveData(Packet packet){
		
		
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
