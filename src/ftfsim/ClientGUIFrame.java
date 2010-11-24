package ftfsim;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClientGUIFrame extends JInternalFrame {
	
	private static final long serialVersionUID = 7152343225641563835L;
	private JTextArea consoleArea;
	private Client attachedClient;



	/**
	 * Create the frame.
	 */
	public ClientGUIFrame(Client client, JPanel f) {

		attachedClient = client;
		this.setTitle("Client" + client.ClientId);
        this.setBounds(558, 400+(new Integer(client.ClientId)*120), 385, 176);
        f.add(this);
        getContentPane().setLayout(null);
        
        JScrollPane consoleScroll = new JScrollPane();
        consoleScroll.setBounds(0, 0, 361, 75);
        this.getContentPane().add(consoleScroll);
        
        consoleArea = new JTextArea();
        consoleScroll.setViewportView(consoleArea);
        
        JButton btnSendTest = new JButton("Send Test");
        btnSendTest.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		
        		Runnable doTest = new Runnable() {
        			public void run(){
        				attachedClient.testMethod();
        			}
        		};
        		
        		Thread doTestThread = new Thread(doTest);
        		doTestThread.start();
        		
        	}
        });
        btnSendTest.setBounds(10, 87, 117, 29);
        getContentPane().add(btnSendTest);
        this.setVisible(true);
		

	}

	public JTextArea getClientConsole() {
		return consoleArea;
	}
	
	public void writeToConsole(String msg){
		System.out.println("Got to writeToConsole method...");
		getClientConsole().append(msg + "\n");
		System.out.println("Appended msg...");
	}
}
