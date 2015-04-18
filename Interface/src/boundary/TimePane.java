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

import dao.DominoesSQLDao;
import domain.Configuration;


@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class TimePane extends Pane{
	
	private Button bSet;
	
	private Group timePaneGroup; 
	
	private LineChart<String, Number> lineChart;
	private IntervalSlider slider;
	private double sliderWidth;
	private double historicBeginPosition = 0, historicEndPosition = 0;
	private double chartZeroX = -1;
	private double chartZeroY = -1;
	private double paddingX = -1;
	JFreeChart jLineChart;
	Date beginDate = null;
	Date endDate = null;
	
	public TimePane(Date _begin, Date _end, String _database, String _project){
		this(0,1,0,1, _begin, _end, _database, _project);
	}
	
	public TimePane(double min, double max, Date _begin, Date _end, 
			String _database, String _project){
		this(min, max, 0, 1, _begin, _end, _database, _project);
	}
	
	public TimePane(double min, double max, double selectMin, double selectMax, 
			Date _begin, Date _end, String _database, String _project){
		beginDate = _begin;
		endDate = _end;
		
		ObservableList<String> xItems = FXCollections.<String>observableArrayList();
		
		this.bSet = new Button("Set");
		Tooltip.install(bSet, new Tooltip("Click to set begin date and end date to work"));
		
        BorderPane gridPaneSet = new BorderPane();        
        gridPaneSet.setCenter(bSet);
        
		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.setTickLabelRotation(90);
        final NumberAxis yAxis = new NumberAxis();
        
        this.lineChart = new LineChart<String, Number>(xAxis, yAxis);
        this.lineChart.setPrefHeight(200);
        this.lineChart.setCreateSymbols(false);

		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		// Add commits
		try {
			Map<String, Integer> commitsByPeriod = DominoesSQLDao.getNumCommits(dao.DominoesSQLDao.Group.Month,
					_database, beginDate, endDate, _project);			
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
			Map<String, Integer> bugsByPeriod = DominoesSQLDao.getNumBugs(dao.DominoesSQLDao.Group.Month,
					beginDate, endDate, _database, _project);			
			XYChart.Series serie2 = new XYChart.Series<>();
			serie2.setName("Bugs");
			
			for (Map.Entry<String, Integer> value : bugsByPeriod.entrySet()){
				serie2.getData().add(new XYChart.Data<>(value.getKey(), value.getValue()));
				ds.addValue(value.getValue(), value.getKey(), "Bugs");
			}
			lineChart.getData().add(serie2);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		xAxis.setCategories(xItems);
		
		slider = new IntervalSlider(min, max, selectMin, selectMax, max-min);
		
		timePaneGroup = new Group();
		timePaneGroup.getChildren().add(lineChart);
		timePaneGroup.getChildren().add(slider);
		
		this.lineChart.setPrefWidth(Configuration.width);
		
		GridPane pane = new GridPane();
		pane.add(timePaneGroup, 0, 0);
		pane.add(gridPaneSet, 0, 1);
		
		this.getChildren().add(pane);

		this.bSet.setOnAction(null);
	}
	
	public void definitionSlider(Stage stage){
		int size = this.lineChart.getData().get(0).getData().size();
		String begin = this.lineChart.getData().get(0).getData().get(0).getXValue();
		String end = this.lineChart.getData().get(0).getData().get(size - 1).getXValue();
		
		if(Configuration.resizableTimeOnFullScreen){
			this.lineChart.setPrefWidth(stage.getWidth());
		}
		stage.show();
		
		Axis<String> xAxis = lineChart.getXAxis();
		Axis<Number> yAxis = lineChart.getYAxis();
		
		ArrayList<String> tooltip = new ArrayList<>();
		
		for(int i = 0; i < size; i++){
			tooltip.add(this.lineChart.getData().get(0).getData().get(i).getXValue());
		}
		
		if(chartZeroX == -1
        		|| chartZeroY == -1
        		|| paddingX == -1){
        Node chartPlotArea = stage.getScene().lookup(".chart-plot-background");
	    	chartZeroX = chartPlotArea.getLayoutX();
        	chartZeroY = chartPlotArea.getLayoutY();
        
        	paddingX = xAxis.getTickLength() - xAxis.getTickLabelGap();
        }

        
        if(Configuration.resizableTimeOnFullScreen){
        	if(historicBeginPosition == 0 && historicEndPosition == 0){
        		historicBeginPosition = xAxis.getDisplayPosition(begin);
        		historicEndPosition = xAxis.getDisplayPosition(end);
        	}
        	sliderWidth = historicEndPosition;
        	sliderWidth -= historicBeginPosition;
        }else{
        	sliderWidth = xAxis.getDisplayPosition(end);
        	sliderWidth -= xAxis.getDisplayPosition(begin);
        }
        
        IntervalSlider slider = new IntervalSlider(this.slider.getMin(), this.slider.getMax(), this.slider.getValueMin(), this.slider.getValueMax(), this.sliderWidth, tooltip);
        if(Configuration.resizableTimeOnFullScreen){
        	slider.setTranslateX(historicBeginPosition + chartZeroX + paddingX);
        }else{
        	slider.setTranslateX(xAxis.getDisplayPosition(begin) + chartZeroX + paddingX);
        }
        slider.setTranslateY(yAxis.getDisplayPosition(0) + chartZeroY);
        slider.setLinkPointers(true);
        
        int index = this.timePaneGroup.getChildren().indexOf(this.slider);
        this.slider = slider;
        this.timePaneGroup.getChildren().set(index, slider);
        
        if(Configuration.resizableTimeOnFullScreen){
        	historicBeginPosition = xAxis.getDisplayPosition(begin);
        	historicEndPosition = xAxis.getDisplayPosition(end);
        }
        stage.show();
	}
	
	public String getValueToolTipMin(){
    	return slider.getValueToolTipMin();
    }
    
    public String getValueToolTipMax(){
    	return slider.getValueToolTipMax();
    }
	
	public void setButtomOnAction(EventHandler<ActionEvent> event){
		this.bSet.setOnAction(event);
	}
}
