package boundary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.josericardojunior.dao.DominoesSQLDao;
import domain.Configuration;


@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class TimePane extends Pane{
	private Group timePaneGroup; 
	
	private LineChart<String, Number> lineChart;
	private double historicBeginPosition = 0, historicEndPosition = 0;
	private double chartZeroX = -1;
	private double chartZeroY = -1;
	private double paddingX = -1;
	

	
	public void Configure(Date _beginDate, Date _endDate, String _database, String _project){
		
		ObservableList<String> xItems = FXCollections.<String>observableArrayList();
				
        BorderPane gridPaneSet = new BorderPane();        
        
		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.setTickLabelRotation(90);
        final NumberAxis yAxis = new NumberAxis();
        
        this.lineChart = new LineChart<String, Number>(xAxis, yAxis);
        this.lineChart.setPrefHeight(200);
        this.lineChart.setCreateSymbols(false);

		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		// Add commits
		try {
			Map<String, Integer> commitsByPeriod = DominoesSQLDao.getNumCommits(DominoesSQLDao.Group.Month,
					_database, _beginDate, _endDate, _project);			
			XYChart.Series<String, Number> serie1 = new XYChart.Series<>();
			serie1.setName("Commits");
			for (Map.Entry<String, Integer> value : commitsByPeriod.entrySet()){
				serie1.getData().add(new XYChart.Data<String, Number>(value.getKey(), value.getValue()));
				xItems.add(value.getKey());
				ds.addValue(value.getValue(), value.getKey(), "Commits");
			}
			lineChart.getData().add(serie1);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Add Bugs
		try {
			Map<String, Integer> bugsByPeriod = DominoesSQLDao.getNumBugs(DominoesSQLDao.Group.Month,
					_beginDate, _endDate, _database, _project);			
			XYChart.Series serie2 = new XYChart.Series<>();
			serie2.setName("Issues");
			
			for (Map.Entry<String, Integer> value : bugsByPeriod.entrySet()){
				serie2.getData().add(new XYChart.Data<>(value.getKey(), value.getValue()));
				ds.addValue(value.getValue(), value.getKey(), "Issues");
			}
			lineChart.getData().add(serie2);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		xAxis.setCategories(xItems);
		
		//slider = new IntervalSlider(min, max, selectMin, selectMax, max-min);
		
		timePaneGroup = new Group();
		timePaneGroup.getChildren().add(lineChart);
	//	timePaneGroup.getChildren().add(slider);
		
		this.lineChart.setPrefWidth(Configuration.width);
		
		GridPane pane = new GridPane();
		pane.getChildren().clear();
		pane.add(timePaneGroup, 0, 0);
		pane.add(gridPaneSet, 0, 1);
		this.getChildren().clear();
		
		this.getChildren().add(pane);
	}
	
	public String getValueToolTipMin(){
    	return "0";//slider.getValueToolTipMin();
    }
    
    public String getValueToolTipMax(){
    	return "0";// slider.getValueToolTipMax();
    }
	
	public void setButtomOnAction(EventHandler<ActionEvent> event){
	}
}
