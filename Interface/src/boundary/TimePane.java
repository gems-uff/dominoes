package boundary;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import arch.Cell;
import dao.DaoFactory;
import dao.DominoesSQLDao;
import dao.DominoesSQLDao.Group;
import domain.Configuration;
import domain.Dominoes;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.embed.swing.SwingNode;
import javafx.geometry.*;


public class TimePane extends Pane{

	private Label labelTime;
	private Label labelDatabase;
	
	private ComboBox comboBoxTime;
	private ComboBox comboBoxDatabase;
	
	private LineChart<String, Number> lineChart;
	private IntervalSlider slider;
	
	
	JFreeChart jLineChart;
	
	public TimePane(){
		
		this.setHeight(Configuration.height/2);
		this.setWidth(Configuration.width);
		
		labelTime = new Label("Period");
		labelDatabase = new Label("Repository");
		
		comboBoxTime = new ComboBox();
		ObservableList<String> itemsTime = FXCollections.observableArrayList();
		
        itemsTime.add("1 Month");
        itemsTime.add("3 Month");
        itemsTime.add("6 Month");
        
        comboBoxTime.setItems(itemsTime);
        comboBoxTime.setValue(itemsTime.get(0));
        Tooltip.install(comboBoxTime, new Tooltip("specify the period to work in the repository"));
        
        comboBoxDatabase = new ComboBox();
        comboBoxDatabase.setMaxWidth(130);
        ObservableList<String> itemsDatabase = FXCollections.observableArrayList();
        
        //receber do banco de dados
        itemsDatabase.add("database 0");
        itemsDatabase.add("database 1");
        itemsDatabase.add("database 2");
		
        comboBoxDatabase.setItems(itemsDatabase);
        comboBoxDatabase.setValue(itemsDatabase.get(0));
        Tooltip.install(comboBoxDatabase, new Tooltip("select the repository to work with in the database"));
        
        GridPane gridPaneConfigurations = new GridPane();
        gridPaneConfigurations.setHgap(20);
        gridPaneConfigurations.setVgap(10);
        gridPaneConfigurations.setPrefWidth(200);
        gridPaneConfigurations.add(labelTime, 0, 0);
        gridPaneConfigurations.add(comboBoxTime, 1, 0);
        gridPaneConfigurations.add(labelDatabase, 0, 1);
        gridPaneConfigurations.add(comboBoxDatabase, 1, 1);
        
		final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("label X");
        xAxis.setPrefHeight(30);
        yAxis.setLabel("label Y");
        yAxis.setPrefWidth(50 + 10 * yAxis.getLabel().split("\\n").length);

        this.lineChart = new LineChart<String, Number>(xAxis, yAxis);
        this.lineChart.setAnimated(false);        
        this.lineChart.setPrefHeight(this.getHeight()/4);
        this.lineChart.setPrefWidth(this.getWidth() - gridPaneConfigurations.getPrefWidth());
        
        double x = yAxis.getPrefWidth() + yAxis.getTickLength() + yAxis.getTickLabelGap() + yAxis.getTickUnit();
        double y = xAxis.getPrefHeight() + xAxis.getTickLength() + xAxis.getTickLabelGap() + xAxis.getTickLabelFont().getSize();
		
		slider = new IntervalSlider(0, 1, 0.2, 0.8, this.lineChart.getPrefWidth() - x - 14);
		slider.setTranslateX(x);
		slider.setTranslateY(-y + slider.getHeight());
		

		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		// Add commits
		try {
			Map<String, Integer> commitsByPeriod = DominoesSQLDao.getNumCommits(Group.Month);			
			XYChart.Series serie1 = new XYChart.Series<>();
			serie1.setName("Commits");
			
			for (Map.Entry<String, Integer> value : commitsByPeriod.entrySet()){
				serie1.getData().add(new XYChart.Data<>(value.getKey(), value.getValue()));
				ds.addValue(value.getValue(), value.getKey(), "Commits");
			}
			lineChart.getData().add(serie1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		// Add Bugs
		try {
			Map<String, Integer> bugsByPeriod = DominoesSQLDao.getNumBugs(Group.Month);			
			XYChart.Series serie2 = new XYChart.Series<>();
			serie2.setName("Bugs");
			
			for (Map.Entry<String, Integer> value : bugsByPeriod.entrySet()){
				serie2.getData().add(new XYChart.Data<>(value.getKey(), value.getValue()));
				ds.addValue(value.getValue(), value.getKey(), "Bugs");
			}
			lineChart.getData().add(serie2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//lineChart.setBarGap(3);
		//lineChart.setCategoryGap(20);
		
		/*barChart.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
				if (event.getDeltaY() == 0) {
		            return;
		        }

		        double scaleFactor = (event.getDeltaY() > 0) ? 1.1 : 1 / 1.1;

		        barChart.setScaleX(barChart.getScaleX() * scaleFactor);
		        barChart.setScaleY(barChart.getScaleY() * scaleFactor);
		    }
				
		});*/
		
		
		
		//jLineChart = ChartFactory.createLineChart(
			//"Tittle", "", "", ds, PlotOrientation.HORIZONTAL, true, false, false);
		
		GridPane pane = new GridPane();
		pane.add(gridPaneConfigurations, 0, 0);		
		pane.add(new FlowPane(lineChart,slider), 1, 0);
		pane.setGridLinesVisible(true);
		
		pane.setPrefHeight(Configuration.width/2);
		this.getChildren().add(pane);
		
		//SwingNode swingNode = new SwingNode();
		//swingNode.setContent(new org.jfree.chart.ChartPanel(jLineChart));

		//this.getChildren().add(swingNode);
		
	}
}
