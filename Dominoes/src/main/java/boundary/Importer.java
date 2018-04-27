package boundary;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


import com.josericardojunior.RepositoryImporter.RepositoryNode;
import com.josericardojunior.dao.DominoesSQLDao;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Importer {

	@FXML
	Button btnClose;
	
	@FXML
	Button btnUpdate;
	
	@FXML
	ComboBox<String> cbRepositories;
	
	@FXML
	Button btnNew;
	
	@FXML
	TextField txDatabase;
	
	@FXML
	TextArea txLog;
	
	private String databaseName;
	Map<String, RepositoryNode> repositories = new HashMap<>();
	
	
	public void initParameters(String databaseName){
		this.databaseName = databaseName;
		txDatabase.setText(databaseName);
		
		txLog.textProperty().addListener(new ChangeListener<Object>() {

			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				txLog.setScrollTop(Double.MAX_VALUE);
				
			}
		});
		
		fillComboBox();
	}
	
	public void importNewRepo(){
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Git Repository");
		
		File dirProject = dc.showDialog(App.getStage());
		
		if (dirProject != null){
			File gitPath = new File(dirProject.getAbsolutePath() + File.separatorChar + ".git");
			
			
			if (gitPath.exists()){
				// Check if this is already imported
				if (!isAlreadyImported(dirProject)){
					
					TextInputDialog bugIdDialog = new TextInputDialog();
					bugIdDialog.setTitle("Bug ID");
					bugIdDialog.setContentText("Please type the bug id prefix:");
					
					Optional<String> bugId = bugIdDialog.showAndWait();
					
					// Start importing
					if (bugId != null)
						importNewRepository(dirProject, bugId.get());
					
					
				} else {
					Alert error = new Alert(AlertType.ERROR);
					error.setTitle("Import Error");
					error.setContentText("Repository already imported!");
					error.showAndWait();
				}
			} else {
				Alert error = new Alert(AlertType.ERROR);
				error.setTitle("Error");
				error.setContentText("Not a Git directory");
				error.showAndWait();
			}
		}
		
	}
	
	public void updateRepository(){
		String curRepo = cbRepositories.getSelectionModel().getSelectedItem();
		
		if (curRepo == null || curRepo.length() == 0) return;
		
		RepositoryNode toUpdate = repositories.get(curRepo);
		
		txLog.setText("Updating repository " + toUpdate.getName() + "...");
		
		ImporterWorker iw = new ImporterWorker(txLog, toUpdate);
		iw.execute();
	}
	
	public void closeWindow(){
		Stage stage = (Stage) btnClose.getScene().getWindow();
		stage.close();
	}
	
	private void fillComboBox(){
		// Add information about the projects in the database
			
		try {
			repositories = DominoesSQLDao.retrieveRepositores();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
					
					
		for (RepositoryNode node : repositories.values()){
			cbRepositories.getItems().add(node.getName());	
		}
		
		cbRepositories.getSelectionModel().select(0);
	}
	
	private boolean isAlreadyImported(File file){
		String absolutePathName = file.getAbsolutePath();
		String projName = absolutePathName.substring(
				absolutePathName.lastIndexOf(File.separatorChar) + 1,
				absolutePathName.length());
		
		return repositories.containsKey(projName);
	}
	
	private void importNewRepository(File file, String mineBugId){		
		RepositoryNode repoNode = new RepositoryNode(file, mineBugId);
		repositories.put(repoNode.getName(), repoNode);
		cbRepositories.getItems().add(repoNode.getName());
		
		txLog.setText("Saving repository " + repoNode.getName() + " to database...");

		try {
			DominoesSQLDao.addRepository(repoNode);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ImporterWorker iw = new ImporterWorker(txLog, repoNode);
		iw.execute();		
	}
}
