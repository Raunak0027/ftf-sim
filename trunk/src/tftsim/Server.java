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
	
	Server(Router router, String MAC){
		MACAddress = new String(MAC);
		IPAddress = null;
		alive = true;
		primary = false;
		connectedRouter = router;
		otherServers = new ArrayList<Server>();
		shoutsHeard = new Hashtable();
		
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
			otherServers.get(shoutCount).hearShout(this.getIP());
			shoutCount++;
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
	
	
	
	
	
	
	public void giveLife(){
		
		Runnable doShouts = new Runnable() {

			public void run() {
				
				while(alive){
					
					shoutOut();
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						
					}
				}
			}
		};
		
		Thread doShoutsThread = new Thread(doShouts);
		
		doShoutsThread.start();
		
	}
	
	public void killServer(){
		this.alive = false;
	}
	
	
}
