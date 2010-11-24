package ftfsim;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;

public class ClientGUIFrame extends JInternalFrame {
	
	private static final long serialVersionUID = 7152343225641563835L;
	private JTextArea consoleArea;



	/**
	 * Create the frame.
	 */
	public ClientGUIFrame(int clientId, JPanel f) {

		this.setTitle("Client" + clientId);
        this.setBounds(558, 400+(clientId*120), 385, 123);
        f.add(this);
        
        JScrollPane consoleScroll = new JScrollPane();
        this.getContentPane().add(consoleScroll, BorderLayout.CENTER);
        
        consoleArea = new JTextArea();
        consoleScroll.setViewportView(consoleArea);
        this.setVisible(true);
		

	}

	public JTextArea getClientConsole() {
		return consoleArea;
	}
	
	public void writeToConsole(String msg){
		getClientConsole().append(msg + "\n");
	}
}
