package boundary;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.josericardojunior.RepositoryImporter.CommitNode;
import com.josericardojunior.RepositoryImporter.RepositoryNode;
import com.josericardojunior.dao.DominoesSQLDao;
import domain.Configuration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ProjectInfoPane extends Pane {

	private Label projectName = new Label("Project Name");
	private ComboBox<String> projects = new ComboBox<>();
	private Button newProjectImporter = new Button();
	private Button setTime = new Button();
	private Date repoBeginDate = null;
	private Date repoEndDate = null;
	
	private DatePicker beginDate;
	private DatePicker endDate;
	Map<String, RepositoryNode> repositories = new HashMap<>();
	
	public ProjectInfoPane() {
		BorderPane bp = new BorderPane();
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(10, 10, 15, 10));
		hbox.setSpacing(20);
		hbox.setStyle("-fx-background-color: #C7C8BE;");
		bp.setTop(hbox);
		    		    
		setTime.setText("Set");;
		//setTime.setPrefSize(50, 20);
		setTime.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				Configuration.beginDate = toDate(beginDate.getValue());
				Configuration.endDate = toDate(endDate.getValue());
				App.time.Configure(toDate(beginDate.getValue()), toDate(endDate.getValue()), Configuration.database,
						projects.getSelectionModel().getSelectedItem() );
				App.LoadDominoesPieces();
			}
		});
		
		newProjectImporter.setText("Projects...");
		//newProjectImporter.setPrefSize(80, 20);
		//projectName.setPrefSize(80, 20);
		//projects.setPrefSize(150, 20);
		
		beginDate = new DatePicker();
		beginDate.setDisable(true);
		
		endDate = new DatePicker();
		endDate.setDisable(true);
		
		
		
		hbox.getChildren().addAll(projectName, projects, 
				new Label("Start: "), beginDate, 
				new Label("End: "), endDate, 
				setTime,
				newProjectImporter);
		
		getChildren().add(bp);

		projects.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				if (newValue == null) return;
				
				if (!newValue.equals(Configuration.projName)){
					App.clear();
				}
				
				// TODO Auto-generated method stub
				Configuration.projName = newValue;
				
				if (!Configuration.projName.isEmpty()){
					RepositoryNode currentRepo = repositories.get(projects.getSelectionModel().getSelectedItem());
					
					repoBeginDate = DominoesSQLDao.getFirstCommit(currentRepo).getDate();
	                repoEndDate = DominoesSQLDao.getLastCommit(currentRepo).getDate();
	                
					beginDate.setDisable(false);
					endDate.setDisable(false);
					beginDate.setValue(toLocalDate(repoBeginDate));
					endDate.setValue(toLocalDate(repoEndDate));
					UpdateDatePicker();
				}
			}
			
		});
		
		
		
		newProjectImporter.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				FXMLLoader loader = new FXMLLoader();
				// Works on JWS Version
				//URL url = getClass().getResource("/resources/ImporterView.fxml");
				
				// Works on Stand alone app
				URL url = getClass().getResource("/ImporterView.fxml");
				System.out.println(url);
				loader.setLocation(url);
	
				Stage importerStage = new Stage();
				try {
					importerStage.setScene(new Scene((Pane) loader.load()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Importer importer = loader.<Importer>getController();
				importer.initParameters(Configuration.database);
				
				importerStage.setTitle("Importer");
				importerStage.initModality(Modality.APPLICATION_MODAL);
				importerStage.initOwner(App.getStage());;
				importerStage.showAndWait();
				
				FillAvailableRepositories();
			}
		});
		
		FillAvailableRepositories();
	}

	private void FillAvailableRepositories() {
		
		projects.getItems().clear();
		
		try {
			repositories = DominoesSQLDao.retrieveRepositores();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
					
					
		for (RepositoryNode node : repositories.values()){
			projects.getItems().add(node.getName());	
		}
		
	}
	
	private void UpdateDatePicker(){
		Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell()
        {
            @Override
            public void updateItem(LocalDate item, boolean empty)
            {
                super.updateItem(item, empty);

                if(item.isBefore(Instant.ofEpochMilli(repoBeginDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate()) 
                		|| item.isAfter(Instant.ofEpochMilli(repoEndDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate()))
                {
                    setStyle("-fx-background-color: #ffc0cb;");
                    setDisable(true);

                    /* When Hijri Dates are shown, setDisable() doesn't work. Here is a workaround */
                  //  addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());
                }
                
     
            }
        };

        beginDate.setDayCellFactory(dayCellFactory);
        endDate.setDayCellFactory(dayCellFactory);
	}
	
	private static Date toDate(LocalDate localDate){
		Calendar c =  Calendar.getInstance();
		c.set(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
		Date date = c.getTime();
		
		return date;
	}
	
	private static LocalDate toLocalDate(Date date){
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
