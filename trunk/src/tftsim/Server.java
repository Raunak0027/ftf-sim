package tftsim;

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
	
	// deathPeriod is the period of time in milliseconds,
	// after not hearing a shout from another server, that we
	// determine the server to be dead.
	//
	// This value should be greater than the period between shouting
	// and greater than the period between attempting to detect dead servers.
	// You can test the death detection by setting this value less than the
	// shoutDelay and detectDeadDelay
	private long deathPeriod = 200;
	
	
	// These values are the delay between shouting and the delay between looking
	// for dead servers.
	// NOTE: These values should ideally not be equal as the shouting and detecting threads
	// will be working on the server lists at the same time.
	private long shoutDelay = 150;
	private long detectDeadDelay = 150;
	
	
	Server(Router router, String MAC){
		MACAddress = new String(MAC);
		IPAddress = null;
		alive = true;
		primary = false;
		connectedRouter = router;
		otherServers = new ArrayList<Server>();
		shoutsHeard = new Hashtable<String, Long>();
		
		router.addNode(this);
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
		System.out.println("(" + this.getIP() + ") Server: " + this.getMAC() + " has been set as primary");
	}
	
	public void setBackup(){
		primary = false;
		System.out.println("(" + this.getIP() + ") Server: " + this.getMAC() + " has been set as backup");
	}
	
	public void knowServer(Server otherServer){
		otherServers.add(otherServer);
		System.out.println("(" + this.getIP() + ") Server: " + this.getMAC() + " now knows server: " + otherServer.getMAC() + " at IP: " + otherServer.getIP());
		this.updateShoutHeard(otherServer.getIP());
	}
	
	public void forgetServer(Server otherServer){
		otherServers.remove(otherServer);
	}
	
	public void sendData(String destination, String packet){
		
	}
	
	public void shoutOut(){
		int numberOfOtherServers = otherServers.size();
		int shoutCount = 0;
		
		while(shoutCount < numberOfOtherServers){
			System.out.println("(" + this.getIP() + ") Shouting to: " + otherServers.get(shoutCount).getIP());
			try{
				otherServers.get(shoutCount).hearShout(this.getIP());
			}catch(Exception e){
				
			}
			shoutCount++;
			numberOfOtherServers = otherServers.size();
		}
		
		
	}
	
	public void hearShout(String source){
		System.out.println("(" + this.getIP() + ") Shout heard from: " + source);
		updateShoutHeard(source);
	}
	
	private void updateShoutHeard(String ip){
		Calendar currentTime = Calendar.getInstance();
		Long currentTimeInMillis = currentTime.getTimeInMillis();
		shoutsHeard.remove(connectedRouter.getMAC(ip));
		shoutsHeard.put(connectedRouter.getMAC(ip), currentTimeInMillis);
		System.out.println("(" + this.getIP() + ") Shout recorded from: " + ip + " at time: " + currentTimeInMillis);
		
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

			if(currentTimeInMillis > (shoutsHeard.get(mac) + deathPeriod)){
				System.out.println("!!!======= DEAD SERVER DETECTED =========!!!");
				System.out.println("!!! (" + this.getIP() + ") Server: " + mac + " at " + ip + " is dead. Removing from known servers!");
				System.out.println("!!!======================================!!!");
				otherServers.remove(loopCount);
				break serverListLoop;
			}else{
				System.out.println("(" + this.getIP() + ") ("+ currentTimeInMillis +") Server at " + ip + " not detected as dead. Last shout heard at " + shoutsHeard.get(mac));
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
		
		Runnable doShouts = new Runnable() {
			public void run() {
				while(alive){
					
					try{
						shoutOut();
						Thread.sleep(shoutDelay);
					}catch(Exception e){
						// do nothing
					}
				}
			}
		};
		
		
		Runnable removeDeadServers = new Runnable() {
			public void run(){
				while(alive){
					try{
						removeDeads();
						Thread.sleep(detectDeadDelay);
					}catch(Exception e){
						
					}
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
