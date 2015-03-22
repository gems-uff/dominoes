package boundary;
//package edu.uci.ics.archtrace.gui.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

//import edu.uci.ics.archtrace.connectors.ConnectionListener;
//import edu.uci.ics.archtrace.connectors.ConnectorManager;

/**
 * Splash screen used in long operations to inform the user about progress.
 *  - ArchTrace loading
 *  - Architecture/Repository loading
 *
 * @author Leo Murta (murta@ics.uci.edu, murta@cos.ufrj.br) - Aug 25, 2004
 */
public class SplashScreen extends JDialog {
	
	/**
	 * Message of current ArchTrace status
	 */
	JLabel statusLabel;
	
	/**
	 * Message of current memory usage
	 */
	JLabel memoryLabel;
	
	/**
	 * Construct a modal splash screen
	 */
	public SplashScreen(JFrame owner) {
		super(owner);
		this.setLocationRelativeTo(owner);
		this.init();
	}
	
	/**
	 * Construct a non-modal splash screen
	 */
	public SplashScreen() {
		super();
		this.init();
	}
		
	/**
	 * Create/Configure the splash screen
	 */
	private void init() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setBackground(new Color(245, 245, 245));
		
		memoryLabel = new JLabel("loading tiles...");
		memoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(memoryLabel, BorderLayout.NORTH);

		JLabel mainLabel = new JLabel("Dominoes", SwingConstants.CENTER);
		mainLabel.setFont(new Font("SansSerif", Font.PLAIN, 72));
		panel.add(mainLabel, BorderLayout.CENTER);		
		
		statusLabel = new JLabel();
		statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		panel.add(statusLabel, BorderLayout.SOUTH);

		this.setUndecorated(true);
		this.setContentPane(panel);
		this.setSize(400, 200);
		this.setLocationRelativeTo(null);
		this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		
//		ConnectorManager.getInstance().addConnectionListener(this);
	}

	/**
	 * Write a new status message
	 */
	public void setFormatStatus(String message) {
		statusLabel.setText(message);
//		System.out.println("Status: " + statusLabel);
		updateMemory();
	}
	
	/**
	 * Update the memory message
	 */
	public void updateMemory() {
		long memory = Runtime.getRuntime().totalMemory();
		memoryLabel.setText("Memory used: " + Long.toString(Math.round(memory / Math.pow(2, 20))) + " MB");
	}

	/**
	 * @see edu.uci.ics.archtrace.connectors.ConnectionListener#connectionStarted(java.lang.String)
	 */
	public void connectionStarted(String message) {
		setFormatStatus(message);		
	}

	/**
	 * @see edu.uci.ics.archtrace.connectors.ConnectionListener#connectionProgress(java.lang.String)
	 */
	public void connectionProgress(String message) {
		setFormatStatus(message);		
	}

	/**
	 * @see edu.uci.ics.archtrace.connectors.ConnectionListener#connectionFinished(java.lang.String)
	 */
	public void connectionFinished(String message) {
		setFormatStatus(message);		
	}

//	/**
//	 * @see java.awt.Component#setVisible(boolean)
//	 */
	public void setVisible(boolean visible) {
		this.setLocationRelativeTo(this.getOwner());
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		super.setVisible(visible);
	}
}