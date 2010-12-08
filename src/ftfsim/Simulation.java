package ftfsim;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JInternalFrame;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.awt.Color;


public class Simulation extends JFrame {


	private static final long serialVersionUID = -6622337722353296721L;


	// Set up environment
	private static Simulation sim;
	private static Router router;
	private static Server[] servers;
	private static int instantiatedServerCount;
	private static int instantiatedClientCount;
	private static int startedClientCount;
	private Client[] clients = new Client[1000];
	public int simRate;
	public int deathPeriod;


	// Declare exposed visual elements
	static JInternalFrame[] serverFrames = new JInternalFrame[10];
	static ClientGUIFrame[] clientFrames = new ClientGUIFrame[20];
	private JTextField macAddressField;
	private JButton btnCreateServer;
	private JTextArea routerConsole;
	private static JPanel mainPanel;
	private JTextArea consoleTextArea;
	private JCheckBox chckbxAutoscroll;
	private JSlider sldrSimRate;
	private JSlider sldrDeath;
	private JTextArea deathConsole;
	private JTextField serverIndexTxt;


	// Set up other...
	private ImageIcon image = new ImageIcon(this.getClass().getResource("/resources/logo.png"));
	private String[] macExamples = new String[5];
	int macExampleCount;
	private DefaultTableModel ipTableModel;
	private JTextArea globalClientConsole;
	private JLabel lblCorrectResults;




	// Construct GUI and other
	public Simulation() {



		macExampleCount = 0;
		instantiatedServerCount = 0;
		instantiatedClientCount = 0;
		ipTableModel = new DefaultTableModel();

		ipTableModel.addColumn("MAC Address");
		ipTableModel.addColumn("IP Address");

		// macExamples[0] = "00-0B-22-B3-01-FF"; used as default in text field
		macExamples[0] = "00-0C-F1-56-98-AD";
		macExamples[1] = "00-0A-B2-33-F1-11";
		macExamples[2] = "1F-14-F6-21-AA-CA";
		macExamples[3] = "1C-15-F4-22-AA-BA";
		macExamples[4] = "2F-12-12-33-AB-CC";

		setSize(1166, 716);
		setTitle("Simulation");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		setLocation(size.width/2 - getWidth()/2, 
				size.height/2 - getHeight()/2);
		getContentPane().setLayout(null);

		mainPanel = new JPanel();
		mainPanel.setBounds(0, 0, 1166, 694);
		getContentPane().add(mainPanel);
		mainPanel.setLayout(null);

		JInternalFrame simSettingsFrame = new JInternalFrame("Simulation Settings");
		try {
			simSettingsFrame.setSelected(true);
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		simSettingsFrame.setToolTipText("Adjust variables in the simulation environment");
		simSettingsFrame.setBounds(16, 6, 358, 663);
		mainPanel.add(simSettingsFrame);
		simSettingsFrame.getContentPane().setLayout(null);

		JTabbedPane settingsTabs = new JTabbedPane(JTabbedPane.TOP);
		settingsTabs.setBounds(0, 0, 334, 159);
		simSettingsFrame.getContentPane().add(settingsTabs);

		JPanel speedPanel = new JPanel();
		settingsTabs.addTab("Speed", null, speedPanel, null);

		sldrSimRate = new JSlider();
		sldrSimRate.setBounds(107, 5, 200, 44);
		sldrSimRate.setMajorTickSpacing(4);
		sldrSimRate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				simRate = getSldrSimRate().getValue();
			}
		});

		sldrSimRate.setMinimum(1);
		sldrSimRate.setMinorTickSpacing(2);
		sldrSimRate.setSnapToTicks(true);
		sldrSimRate.setPaintTicks(true);
		sldrSimRate.setPaintLabels(true);
		sldrSimRate.setValue(10);
		sldrSimRate.setMaximum(20);

		sldrDeath = new JSlider();
		sldrDeath.setBounds(107, 59, 200, 44);
		sldrDeath.setPaintLabels(true);
		sldrDeath.setSnapToTicks(true);
		sldrDeath.setMajorTickSpacing(10);
		sldrDeath.setPaintTicks(true);
		sldrDeath.setMinorTickSpacing(1);
		sldrDeath.setValue(20);
		sldrDeath.setMaximum(40);
		speedPanel.setLayout(null);
		speedPanel.add(sldrSimRate);
		speedPanel.add(sldrDeath);

		JTextPane txtpnSlowdownmultiplier = new JTextPane();
		txtpnSlowdownmultiplier.setBackground(UIManager.getColor("Button.background"));
		txtpnSlowdownmultiplier.setText("Slowdown:\n(multiplier)");
		txtpnSlowdownmultiplier.setBounds(6, 5, 102, 44);
		speedPanel.add(txtpnSlowdownmultiplier);

		JTextPane txtpnDeathPeriodms = new JTextPane();
		txtpnDeathPeriodms.setText("Death period:\n(ms/100)");
		txtpnDeathPeriodms.setBackground(UIManager.getColor("Button.background"));
		txtpnDeathPeriodms.setBounds(6, 59, 102, 44);
		speedPanel.add(txtpnDeathPeriodms);

		JTabbedPane serverPane = new JTabbedPane(JTabbedPane.TOP);
		serverPane.setBounds(0, 157, 334, 146);
		simSettingsFrame.getContentPane().add(serverPane);

		JPanel createServerPanel = new JPanel();
		serverPane.addTab("Create Server", null, createServerPanel, null);
		createServerPanel.setLayout(null);

		JLabel lblMacAddress = new JLabel("MAC Address:");
		lblMacAddress.setBounds(6, 12, 97, 16);
		createServerPanel.add(lblMacAddress);

		macAddressField = new JTextField();
		macAddressField.setBounds(103, 6, 153, 28);
		createServerPanel.add(macAddressField);
		macAddressField.setText("00-0B-22-B3-01-FF");
		macAddressField.setColumns(10);

		JLabel lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(6, 40, 97, 16);
		createServerPanel.add(lblIpAddress);




		btnCreateServer = new JButton("Create Server");

		btnCreateServer.setBounds(139, 65, 117, 29);
		createServerPanel.add(btnCreateServer);

		JPanel killServerPanel = new JPanel();
		serverPane.addTab("Kill Server", null, killServerPanel, null);
		killServerPanel.setLayout(null);

		JButton btnKill = new JButton("Kill");
		btnKill.setBounds(150, 7, 51, 29);
		killServerPanel.add(btnKill);

		serverIndexTxt = new JTextField();
		serverIndexTxt.setBounds(89, 6, 61, 28);
		killServerPanel.add(serverIndexTxt);
		serverIndexTxt.setColumns(10);

		JLabel lblKillByIndex = new JLabel("Kill by Index");
		lblKillByIndex.setBounds(6, 12, 78, 16);
		killServerPanel.add(lblKillByIndex);
		btnKill.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				servers[new Integer(getServerIndexTxt().getText())].killServer();
			}
		});

		JTabbedPane clientsPane = new JTabbedPane(JTabbedPane.TOP);
		clientsPane.setBounds(0, 305, 334, 306);
		simSettingsFrame.getContentPane().add(clientsPane);

		JPanel createClientPanel = new JPanel();
		clientsPane.addTab("Clients", null, createClientPanel, null);
		createClientPanel.setLayout(null);

		JButton btnCreateClient = new JButton("Create Client");
		btnCreateClient.setBounds(6, 6, 106, 29);
		createClientPanel.add(btnCreateClient);
		
		JButton btnCreateClients = new JButton("Create 100 Clients");
		btnCreateClients.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

				Runnable create100Clients = new Runnable(){
					public void run(){
						int count = 0;
						while(count < 100){
							createClientWithOutGUI();
							count++;
						}
					}
				};
				
				Thread create100ClientsThread = new Thread(create100Clients);
				create100ClientsThread.start();
			}
		});
		btnCreateClients.setBounds(119, 6, 153, 29);
		createClientPanel.add(btnCreateClients);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 47, 301, 113);
		createClientPanel.add(scrollPane_1);
		
		globalClientConsole = new JTextArea();
		scrollPane_1.setViewportView(globalClientConsole);
		
		JLabel lblCorrect = new JLabel("Correct Results");
		lblCorrect.setBounds(130, 172, 96, 16);
		createClientPanel.add(lblCorrect);
		
		lblCorrectResults = new JLabel("0");
		lblCorrectResults.setBounds(238, 172, 61, 16);
		createClientPanel.add(lblCorrectResults);
		
		JButton btnDebug = new JButton("Debug");
		btnDebug.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				int loopCount = 0;
				System.out.println(instantiatedClientCount);
				
				while(loopCount < instantiatedClientCount){
					System.out.println("Current Message at Client: " + loopCount+ "" + clients[loopCount].getReceivedMsg() + " Packet Array Size = " + clients[loopCount].getPacketArray().length);
					loopCount++;
				}
				
			}
		});
		btnDebug.setBounds(1, 172, 117, 29);
		createClientPanel.add(btnDebug);
		
		JButton btnStartClients = new JButton("Start Clients");
		btnStartClients.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				int loopCount = 0;
				while(loopCount < instantiatedClientCount){
					startClients();
					loopCount++;
				}
				
				
			}
		});
		btnStartClients.setBounds(155, 204, 117, 29);
		createClientPanel.add(btnStartClients);
		btnCreateClient.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				createClient();        		
			}
		});
		btnCreateServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

				if(btnCreateServer.isEnabled()){

					if(router.getMAC("192.168.1.1")==null){
						createServer(sim, router, macAddressField.getText(), "192.168.1.1");
					}else if(router.getMAC("192.168.1.2")==null){
						createServer(sim, router, macAddressField.getText(), "192.168.1.2");
					}else{
						createServer(sim, router, macAddressField.getText(), "192.168.1." + (instantiatedServerCount + 1));
					}
					//ipAddressField.setText("192.168.1." + (instantiatedServerCount + 1));
					macAddressField.setText(macExamples[macExampleCount]);

					if(macExampleCount<4){
						macExampleCount++;
					}
					/*
					if(instantiatedServerCount>3){
						btnCreateServer.setEnabled(false);
						ipAddressField.setText("");
						ipAddressField.setEnabled(false);
						macAddressField.setText("");
						macAddressField.setEnabled(false);

					}
					*/

				}
			}
		});
		sldrDeath.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				deathPeriod = getSldrDeath().getValue() * 100;
			}
		});
		deathPeriod = sldrDeath.getValue() * 100;

		simRate = sldrSimRate.getValue();

		JInternalFrame consoleFrame = new JInternalFrame("Console");

		try {
			consoleFrame.setSelected(true);
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		consoleFrame.setBounds(377, 6, 431, 404);
		mainPanel.add(consoleFrame);
		consoleFrame.getContentPane().setLayout(null);

		JScrollPane consoleScroll = new JScrollPane();
		consoleScroll.setBounds(6, 34, 395, 167);
		consoleFrame.getContentPane().add(consoleScroll);






		consoleTextArea = new JTextArea();
		consoleTextArea.setText("Welcome to FTF-Sim - Fast Transparent Failover...\n");
		consoleTextArea.setEditable(false);
		consoleScroll.setViewportView(consoleTextArea);
		consoleTextArea.setFont(new Font("Courier New", Font.PLAIN, 13));

		chckbxAutoscroll = new JCheckBox("AutoScroll");
		chckbxAutoscroll.setSelected(true);
		chckbxAutoscroll.setBounds(304, 2, 97, 23);
		consoleFrame.getContentPane().add(chckbxAutoscroll);

		JLabel lblGlobal = new JLabel("Global");
		lblGlobal.setBounds(6, 9, 45, 16);
		consoleFrame.getContentPane().add(lblGlobal);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 241, 395, 112);
		consoleFrame.getContentPane().add(scrollPane);

		deathConsole = new JTextArea();
		scrollPane.setViewportView(deathConsole);
		deathConsole.setFont(new Font("Courier New", Font.BOLD, 13));
		deathConsole.setForeground(Color.RED);
		deathConsole.setBackground(Color.BLACK);

		JLabel lblDeathDetections = new JLabel("Death Detections");
		lblDeathDetections.setToolTipText("");
		lblDeathDetections.setBounds(6, 213, 109, 16);
		consoleFrame.getContentPane().add(lblDeathDetections);

		JInternalFrame routerStatsFrame = new JInternalFrame("Router Stats");
		routerStatsFrame.setBounds(815, 47, 316, 314);
		mainPanel.add(routerStatsFrame);
		routerStatsFrame.getContentPane().setLayout(null);

		JScrollPane routerConsoleScroll = new JScrollPane();
		routerConsoleScroll.setBounds(6, 34, 280, 228);
		routerStatsFrame.getContentPane().add(routerConsoleScroll);

		routerConsole = new JTextArea();
		routerConsole.setFont(new Font("Courier New", Font.PLAIN, 13));
		routerConsoleScroll.setViewportView(routerConsole);

		JLabel lblRouterConsole = new JLabel("Router Console:");
		lblRouterConsole.setBounds(6, 6, 100, 16);
		routerStatsFrame.getContentPane().add(lblRouterConsole);

		JLabel label = new JLabel("", image, JLabel.CENTER);
		label.setBounds(0, 546, 400, 144);
		mainPanel.add(label);

		JButton btnNewSimulation = new JButton("New Simulation");
		btnNewSimulation.setEnabled(false);
		btnNewSimulation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {



				int count = 0;

				while(count<instantiatedServerCount){

					if(servers[count]!=null){
						System.out.println("Killing...");
						servers[count].killServer();
					}

					servers[count] = null;
					count++;
				}

				sim.dispose();
				sim = new Simulation();


			}
		});
		btnNewSimulation.setBounds(1036, 6, 124, 29);
		mainPanel.add(btnNewSimulation);


		routerStatsFrame.setVisible(true);
		consoleFrame.setVisible(true);
		simSettingsFrame.setVisible(true);
		initDataBindings();

		router = new Router(this);

		this.consoleTextArea.append("Router created and initialised\n");

		servers = new Server[10];

		this.consoleTextArea.append("Server array declared - ready to create servers!\n");

		this.setVisible(true);


	}




	public static void main(String[] args) throws Exception {

		sim = new Simulation();





	}





	public void createServer(Simulation sim, Router router, String macAddress, String ip){

		int nextAvailableIndex = instantiatedServerCount;
		servers[nextAvailableIndex] = new Server(router, macAddress, sim);
		servers[nextAvailableIndex].setIP(ip);
		servers[nextAvailableIndex].giveLife();

		this.consoleTextArea.append("Server: " + macAddress + " created! IP Assigned: " + ip + "\n" );

		int count = 0;
		while(count < nextAvailableIndex){
			servers[nextAvailableIndex].knowServer(servers[count]);
			servers[count].knowServer(servers[nextAvailableIndex]);
			count++;
		}


		instantiatedServerCount++;;


	}

	public void createClient(){
		clients[instantiatedClientCount] = new Client(new String("" + instantiatedClientCount), router , sim);
		clientFrames[instantiatedClientCount] = new ClientGUIFrame(clients[instantiatedClientCount], mainPanel);



		instantiatedClientCount++;
	}
	
	public void createClientWithOutGUI(){
		clients[instantiatedClientCount] = new Client(new String("" + instantiatedClientCount), router , sim);
		//clientFrames[instantiatedClientCount] = new ClientGUIFrame(clients[instantiatedClientCount], mainPanel);
		
		instantiatedClientCount++;
	}
	
	public void startClients(){
		
		
			Runnable startClients = new Runnable(){
				
				public void run(){
					clients[startedClientCount].startSending("This is a test. This is a test. This is a test.");
				}
			};
			
			Thread startClientsThread = new Thread(startClients);
			startClientsThread.start();
			startedClientCount++;
		
			
		
		
		
	}
	

	public void writeToClientConsole(int clientId, String msg){
		// System.out.println("Trying to write: " + msg + " to client: " + clientId + " console.");
		
		if(msg.startsWith("CORRECT MESSAGE RECEIVED!!")){
			int numberOfCorrect = new Integer(getLblCorrectResults().getText());
			numberOfCorrect++;
			getLblCorrectResults().setText("" + numberOfCorrect);
		}
		
		try{
			clientFrames[clientId].writeToConsole(msg);
			getGlobalClientConsole().append(msg + "\n");
		}catch(Exception e){
			getGlobalClientConsole().append(msg + "\n");
		}

	}







	public JTextArea getConsoleTextArea() {
		return consoleTextArea;
	}
	protected void initDataBindings() {
	}
	public JCheckBox getChckbxAutoscroll() {
		return chckbxAutoscroll;
	}
	public JSlider getSldrSimRate() {
		return sldrSimRate;
	}
	public int getSimRate(){
		return simRate;
	}
	public long getDeathPeriod(){
		return deathPeriod;
	}
	public JSlider getSldrDeath() {
		return sldrDeath;
	}
	//public DefaultTableModel getIpTableModel(){
	//	return ipTableModel;
	//}
	//public JTable getIpJTable() {
	//	return ipJTable;
	//}
	public JButton getBtnCreateServer() {
		return btnCreateServer;
	}
	public JTextArea getRouterConsole() {
		return routerConsole;
	}
	public JPanel getMainPanel() {
		return mainPanel;
	}
	public JTextArea getDeathConsole() {
		return deathConsole;
	}
	public JTextField getServerIndexTxt() {
		return serverIndexTxt;
	}
	public JTextArea getGlobalClientConsole() {
		return globalClientConsole;
	}
	public JLabel getLblCorrectResults() {
		return lblCorrectResults;
	}
}
