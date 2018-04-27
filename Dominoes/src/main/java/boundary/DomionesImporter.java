package boundary;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;

import javax.swing.JCheckBox;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Insets;

import javax.swing.BoxLayout;

import java.awt.GridLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.josericardojunior.util.JGitDirectoryFilter;

import javax.swing.JComboBox;

import com.josericardojunior.dao.DominoesSQLDao;
import com.josericardojunior.RepositoryImporter.RepositoryNode;
import javax.swing.JFormattedTextField;


public class DomionesImporter extends JDialog {

	private JPanel contentPane;
	private JLabel lblTeste;
	private JTextField txtDatabaseName;
	private JLabel lblRepository;
	private JButton btnUpdate;
	private JButton btnNewButton_1;
	private final JFileChooser fc = new JFileChooser();
	private JScrollPane scrollPane;
	private JTextPane txtLog;
	private JComboBox cbRepos;
	private JButton btnNew;
	Map<String, RepositoryNode> repositories = new HashMap<>();

	/**
	 * Create the frame.
	 */
	public DomionesImporter(String databaseName) {
		setTitle("Dominoes Repository Importer");
		setBounds(100, 100, 514, 534);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{178, 178, 178, 178, 0, 178, 178, 178, 178, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		lblTeste = new JLabel("Dominoes Database");
		GridBagConstraints gbc_lblTeste = new GridBagConstraints();
		gbc_lblTeste.gridwidth = 9;
		gbc_lblTeste.insets = new Insets(0, 5, 5, 0);
		gbc_lblTeste.anchor = GridBagConstraints.WEST;
		gbc_lblTeste.gridx = 0;
		gbc_lblTeste.gridy = 0;
		contentPane.add(lblTeste, gbc_lblTeste);
		
		txtDatabaseName = new JTextField();
		txtDatabaseName.setEditable(false);
		GridBagConstraints gbc_txtDatabaseName = new GridBagConstraints();
		gbc_txtDatabaseName.gridwidth = 4;
		gbc_txtDatabaseName.insets = new Insets(0, 0, 5, 5);
		gbc_txtDatabaseName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDatabaseName.gridx = 0;
		gbc_txtDatabaseName.gridy = 1;
		contentPane.add(txtDatabaseName, gbc_txtDatabaseName);
		txtDatabaseName.setColumns(10);
		
		lblRepository = new JLabel("Repository");
		GridBagConstraints gbc_lblRepository = new GridBagConstraints();
		gbc_lblRepository.gridwidth = 2;
		gbc_lblRepository.anchor = GridBagConstraints.WEST;
		gbc_lblRepository.insets = new Insets(0, 5, 5, 5);
		gbc_lblRepository.gridx = 0;
		gbc_lblRepository.gridy = 2;
		contentPane.add(lblRepository, gbc_lblRepository);
		
		cbRepos = new JComboBox();
		GridBagConstraints gbc_cbRepos = new GridBagConstraints();
		gbc_cbRepos.gridwidth = 7;
		gbc_cbRepos.insets = new Insets(0, 0, 5, 5);
		gbc_cbRepos.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbRepos.gridx = 0;
		gbc_cbRepos.gridy = 3;
		contentPane.add(cbRepos, gbc_cbRepos);
		
		btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnUpdate){
					
					RepositoryNode toUpdate = repositories.get(cbRepos.getSelectedItem());
					
					txtLog.setText("Updating repository " + toUpdate.getName() + "...");
					
					//ImporterWorker iw = new ImporterWorker(txtLog, toUpdate);
					//iw.execute();
				}
			}
		});
		
		btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnNew){
					fc.setAcceptAllFileFilterUsed(false);
					fc.addChoosableFileFilter(new JGitDirectoryFilter());
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					int returnval = fc.showOpenDialog(DomionesImporter.this);
					
					if (returnval == JFileChooser.APPROVE_OPTION){
						File path = fc.getSelectedFile();
						File gitPath = new File(path.getAbsolutePath() + File.separatorChar + ".git");
						
						
						if (gitPath.exists()){
							// Check if this is already imported
							if (!isAlreadyImported(path)){
								
								String bugId = JOptionPane.showInputDialog("Please type the bug id prefix");
								
								// Start importing
								if (bugId != null)
									importNewRepository(path, bugId);
								
								
							} else {
								JOptionPane.showMessageDialog(DomionesImporter.this, "Repository already imported!",
										"Import Erro", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(DomionesImporter.this, 
									"Not a Git directory", "error", JOptionPane.ERROR_MESSAGE);
						}
					}
					
				}
			}
		});
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.gridwidth = 2;
		gbc_btnNew.insets = new Insets(0, 0, 5, 5);
		gbc_btnNew.gridx = 1;
		gbc_btnNew.gridy = 4;
		contentPane.add(btnNew, gbc_btnNew);
		GridBagConstraints gbc_btnUpdate = new GridBagConstraints();
		gbc_btnUpdate.gridwidth = 2;
		gbc_btnUpdate.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdate.gridx = 5;
		gbc_btnUpdate.gridy = 4;
		contentPane.add(btnUpdate, gbc_btnUpdate);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 8;
		gbc_scrollPane.gridwidth = 9;
		gbc_scrollPane.insets = new Insets(10, 10, 10, 10);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 7;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		txtLog = new JTextPane();
		txtLog.setEditable(false);
		scrollPane.setViewportView(txtLog);
		
		txtDatabaseName.setText(databaseName);
		
		btnNewButton_1 = new JButton("Close");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.gridwidth = 2;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 4;
		gbc_btnNewButton_1.gridy = 15;
		contentPane.add(btnNewButton_1, gbc_btnNewButton_1);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		fillComboBox();
	}
	
	private void fillComboBox(){
		// Add information about the projects in the database
			
		try {
			repositories = DominoesSQLDao.retrieveRepositores();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
					
					
		for (RepositoryNode node : repositories.values()){
			cbRepos.addItem(node.getName());	
		}
	}
	
	private boolean isAlreadyImported(File file){
		String absolutePathName = file.getAbsolutePath();
		String projName = absolutePathName.substring(
				absolutePathName.lastIndexOf(File.separatorChar) + 1,
				absolutePathName.length());
		
		return repositories.containsKey(projName);
	}
	
	private void loadDatabaseInfo(File file){
		txtDatabaseName.setText(file.getAbsolutePath());
			
		/*// Add information about the projects in the database

		List<String> repos = new ArrayList<String>();
		try {
			repos = RepositoryImporter.Database.getRepoInfo();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
			
		for (String repo : repos){
			String repoInfo = "";
			int repoNameIdx = repo.indexOf(";", 0);
			int repoLastUpdate = repo.indexOf(";", repoNameIdx + 1);
			int dtIdx = repo.indexOf(";", repoLastUpdate + 1);
				
				
			repoInfo = repoInfo.concat("Repository: " + repo.substring(0, repoNameIdx) + " -- " + 
					"Last CommitId: " + repo.substring(repoNameIdx + 1, repoLastUpdate) + " -- " + 
					"Commit Count: " + repo.substring(repoLastUpdate + 1, dtIdx) + " -- " + 
					"Commit Date: " + repo.substring(dtIdx + 1, repo.length()));

			txtLog.setText(txtLog.getText().concat(repoInfo));
		}*/
		
	}
	
	private void importNewRepository(File file, String mineBugId){		
		RepositoryNode repoNode = new RepositoryNode(file, mineBugId);
		repositories.put(repoNode.getName(), repoNode);
		cbRepos.addItem(repoNode.getName());
		
		txtLog.setText("Saving repository " + repoNode.getName() + "to database...");

		try {
			DominoesSQLDao.addRepository(repoNode);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ImporterWorker iw = new ImporterWorker(txtLog, repoNode);
		//iw.execute();		
	}

}
