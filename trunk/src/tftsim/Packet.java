package tftsim;

public class Packet {
	
	private String source;
	private String destination;
	private String payload;
	private int packetPosition;
	private int messagePacketTotal;
	
	Packet(String s, String d, String pL, int pP, int mPT ){
		this.source = s;
		this.destination = d;
		this.payload = pL;
		this.packetPosition = pP;
		this.messagePacketTotal = mPT;
	}
	
	public String getSource(){
		return this.source;
	}
	
	public String getDest(){
		return this.destination;
	}
	
	public String getPayload(){
		return this.payload;
	}
	
	public int getPosition(){
		return this.packetPosition;
	}
	
	public int getTotal(){
		return this.messagePacketTotal;
	}

}
