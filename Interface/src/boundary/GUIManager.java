package boundary;
//package edu.uci.ics.archtrace.gui.utils;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

import control.Controller;

//import org.jfree.data.statistics.MeanAndStandardDeviation;
//import cern.colt.matrix.impl.SparseDoubleMatrix1D;


/**
 * Manages the ArchTrace Windows
 *
 * @author Leo Murta (murta@ics.uci.edu, murta@cos.ufrj.br) - Aug 11, 2004
 */
public class GUIManager {
	
	/**
	 * Informs if the system is in debug mode
	 */
	private static final boolean DEBUG = true;
	
	/**
	 * Singleton instance
	 */	
	private static GUIManager instance;
	
	/**
	 * The main window
	 */
	private JFrame mainWindow;
	
	/**
	 * The configuration dialog
	 */
//	private JDTeste configDialog;
	
	/**
	 * The splash screen
	 */
	private SplashScreen splashScreen;
	
	/**
	 * The suggestion dialog
	 */
//	private SuggestionDialog suggestionDialog;

	/**
	 * Singleton constructor
	 */
	private GUIManager() {
		// Private singleton constructor
	}
	
	/**
	 * Provides the singleton instance
	 */
	public static GUIManager getInstance() {
//		if (instance == null)
			instance = new GUIManager();
		return instance;
	}
	
	/**
	 * Resets the singleton instance
	 */
	public static synchronized void resetInstance() {
		if(instance != null)
			instance.splashScreen = null;
		instance = null;
		
	}
	
	/**
	 * Sets the configuration dialog
	 * Should be called once, by ArchTrace at boot time
	 */
	public void setMainWindow(JFrame mainWindow) {
		this.mainWindow = mainWindow;
		this.splashScreen = new SplashScreen(mainWindow);
//		this.splashScreen = new SplashScreen();
	}
	
	/**
	 * Dispose the main window and all other windows 
	 */
	public void disposeMainWindow() {
		mainWindow.dispose();
	}
	
	/**
	 * @param string
	 */
//	public void addPolicyMessage(String message) {
//		mainWindow.addPolicyMessage(message);
//	}
	
	/**
	 * Sets an specified cursor to the mouse over the main window
	 */
	public void setMainWindowCursor(Cursor cursor) {
		mainWindow.setCursor(cursor);		
	}
	
	/**
	 * Sets the main window
	 * Should be called once, by ArchTrace at boot time
	 */
//	public void setConfigDialog(ConfigDialog configDialog) {
////		this.configDialog = configDialog;
//	}
	
	/**
	 * Shows the config dialog 
	 */
//	public void showConfigDialog() {
////		configDialog.setVisible(true);
//	}
	
	/**
	 * Show the ABOUT dialog
	 */
	public void showAboutDialog() {
		URL url = getClass().getResource("www.google.com.br");
		try {
			JPanel aboutPanel = new JPanel(new BorderLayout());
			
			JEditorPane editorPane = new JEditorPane(url);
//			JEditorPane editorPane = new JEditorPane();
			editorPane.setEditable(false);
			editorPane.setOpaque(false);
			aboutPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);

			JPanel memoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			memoryPanel.setOpaque(false);
			long memory = Runtime.getRuntime().totalMemory();
			memoryPanel.add(new JLabel("Memory used: " + Long.toString(Math.round(memory / Math.pow(2, 20))) + " MB"));		
			aboutPanel.add(memoryPanel, BorderLayout.NORTH);
			
			aboutPanel.setPreferredSize(new Dimension(400, 300));			
			JOptionPane.showMessageDialog(mainWindow, aboutPanel, "About", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			showErrorDialog(e);
		}
	}
	
	/**
	 * Show any error message
	 */
	public void showErrorDialog(Exception e) {
		if (DEBUG)
			e.printStackTrace();
		
		StringWriter buffer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(buffer);
		e.printStackTrace(printWriter);
						
		showMessageDialog("Error", e.getMessage(), "Error trace", buffer.toString(), false, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Show a message dialog with a specific long text inside
	 */
	public void showMessageDialog(String windowTitle, String briefMessage, String longMessageTitle, String longMessage, boolean wrapLines, int MessageType) {
		JTextArea textArea = new JTextArea(longMessage);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		if (wrapLines) {
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
		}		
		
		JPanel messagePanel = new JPanel(new BorderLayout(15, 15));
		JScrollPane scrollPanel = new JScrollPane(textArea);
		scrollPanel.setBorder(BorderFactory.createTitledBorder(longMessageTitle));
		messagePanel.add(scrollPanel, BorderLayout.CENTER);
		messagePanel.add(new JLabel("<html>" + briefMessage + "</html>"), BorderLayout.NORTH);
		messagePanel.setPreferredSize(new Dimension(400, 200));
		
		JOptionPane.showMessageDialog(mainWindow, messagePanel, windowTitle, MessageType);		
	}
	
	/**
	 * Show a dialog with a set of elements and a message
	 * @param message Message to be shown in the dialog
	 * @param sugestions Elements that may be selected by the user
	 * @return Collection of selected elements
	 */
//	public synchronized <T> Collection<T> showSuggestionDialog(String message, List<T> suggestions) {
//		return suggestionDialog.show(message, suggestions);
//	}

	/**
	 * Shows the splash screen and execute a given code in a separate thread.
	 * At the end of the code, hide the splash screen
	 */
	public void run(final Runnable code) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				splashScreen.setFormatStatus(Controller.MESSAGE_STARTED);
						code.run();
						
						int count = 1;
						String text = ".";
						// Wait the splash screen to be visible before trying to hide it
						while (splashScreen.isVisible() && Controller.indexTileSelected != Controller.amout_tiles) {
							
							splashScreen.setFormatStatus(setStatus(count, text));
							
							count = (++count % 4);
							try{
								Thread.sleep(200);
							}catch(Exception ex){}
						}
						Controller.indexTileSelected = 0;
						splashScreen.setFormatStatus(Controller.MESSAGE_FINISHED);
						splashScreen.setVisible(false);
			}

			private String setStatus(int count, String type) {
				
				String status = "";
				// TODO Auto-generated method stub
				status = Controller.message;
				for(int i = 1; i <= count; i++){
					status = status + type;
				}
				
				return status;
			}
		});
		
		splashScreen.setVisible(true);
		thread.start();
		
		
	}
	
//	/**
//	 * Set a new status message in the splash screen
//	 */
//	public void setSplashScreenStatus(String message) {
//		splashScreen.setStatus(message);
//	}
}