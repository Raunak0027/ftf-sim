package cs333.coral;

public class Simulation {

	public static void main(String[] args) {
		
		// Perform system setup
		Router router = new Router();
		Server serverA = new Server(router, "00-0C-F1-56-98-AD");
		Server serverB = new Server(router, "00-0B-22-B3-01-FF");
		Server serverC = new Server(router, "00-0A-B2-33-F1-11");
		
		serverA.setIP("192.168.1.1"); // server is notifying router of IP also
		serverB.setIP("192.168.1.2");
		serverC.setIP("192.168.1.3");
		
		serverA.knowServer(serverB);
		serverA.knowServer(serverC);
		
		serverB.knowServer(serverA);
		serverB.knowServer(serverC);
		
		serverC.knowServer(serverA);
		serverC.knowServer(serverB);
		
		serverA.setPrimary();
		serverB.setBackup();
		serverC.setBackup();
		
		
		
		serverA.giveLife();
		serverB.giveLife();
		serverC.giveLife();
		
		
		//Client clientA = new Client("123.123.123.1");
		

	}

}
