/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import com.josericardojunior.arch.Cell;
import domain.Configuration;
import com.josericardojunior.domain.Dominoes;


/**
 *
 * @author Daniel
 */
@SuppressWarnings("restriction")
public class ChartPane extends Pane {

	private static final String TYPE_ORDER_BY_NORMAL = "Normal";
	private static final String TYPE_ORDER_BY_INCREASING = "Increasing";
	private static final String TYPE_ORDER_BY_DECREASING = "Decreasing";
	
    private ComboBox<String> cbSelectedRow;
    private ComboBox<String> cbOrderBy;
    private HashMap<String, Integer> unsortedList = new HashMap<>();

    private BarChart<String, Number> bc;

    private double maxZoom = 20;
    private double minZoom = 0.05;

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;

    private Dominoes domino;
    
    public ChartPane(Dominoes domino) {
    	this.domino = domino;
    	this.init();
        this.drawChart();
        this.setActions();
        this.update();
        

    }

    private void init(){
    	CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel(domino.getMat().getMatrixDescriptor().getRowType());
        yAxis.setLabel(domino.getMat().getMatrixDescriptor().getColType());

        bc = new BarChart<String, Number>(xAxis, yAxis);
//        bc.setTitle(domino.getHistoric().toString());
        bc.setAnimated(false);
        bc.getXAxis().setAutoRanging(true);
        bc.getYAxis().setAutoRanging(true);
//        bc.set
        
	    // Draw Chart
        bc.getXAxis().setTickLabelFont(new Font("Lucida Console", (int)bc.getXAxis().getTickLabelFont().getSize()));
	    float maxValue = domino.getMat().findMaxValue();
	    float minValue = domino.getMat().findMinValue();
	    
	    double fontSize = bc.getXAxis().getTickLabelFont().getSize();
		double textLarger = 0;
	    	
		int select = 0;
		for(int i = 0; i < domino.getMat().getMatrixDescriptor().getNumCols(); i++){
			if(domino.getMat().getMatrixDescriptor().getColumnAt(i).length() > textLarger){
				textLarger = domino.getMat().getMatrixDescriptor().getColumnAt(i).length();
				select = i;
			}
		}
		bc.setPrefWidth(domino.getMat().getMatrixDescriptor().getNumCols() * fontSize * 4);
		bc.setPrefHeight(2.5 * textLarger * fontSize);
        
        // change color BarChart

        cbSelectedRow = new ComboBox<>();
        ObservableList<String> itemsCBMatrixRows = FXCollections.observableArrayList();
        for (int i = 0; i < domino.getMat().getMatrixDescriptor().getNumRows(); i++) {
            itemsCBMatrixRows.add(domino.getMat().getMatrixDescriptor().getRowAt(i));
            unsortedList.put(domino.getMat().getMatrixDescriptor().getRowAt(i), i);

        }
        FXCollections.sort(itemsCBMatrixRows);
        cbSelectedRow.setItems(itemsCBMatrixRows);
        cbSelectedRow.getSelectionModel().select(0);
        cbSelectedRow.toFront();
        
        cbOrderBy = new ComboBox<>();
        ObservableList<String> itemsBCOrderBy = FXCollections.observableArrayList();
        
        itemsBCOrderBy.add(TYPE_ORDER_BY_NORMAL);
        itemsBCOrderBy.add(TYPE_ORDER_BY_INCREASING);
        itemsBCOrderBy.add(TYPE_ORDER_BY_DECREASING);
        
        cbOrderBy.setItems(itemsBCOrderBy);
        cbOrderBy.getSelectionModel().select(TYPE_ORDER_BY_NORMAL);
        cbOrderBy.toFront();
        
    }
    
    private void setActions(){
        cbSelectedRow.valueProperty().addListener(new ChangeListener<String>() {
            @SuppressWarnings("rawtypes")
			@Override
            public void changed(ObservableValue ov, String t, String t1) {
                // draw chart
            	cbOrderBy.getSelectionModel().select(TYPE_ORDER_BY_NORMAL);
                drawChart();
            }
        });
        
        cbOrderBy.valueProperty().addListener(new ChangeListener<String>() {
            @SuppressWarnings("rawtypes")
			@Override
            public void changed(ObservableValue ov, String t, String t1) {
                // draw chart
                drawChart();
                
            }
        });
    	
    	this.setOnScroll(new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                double srcX = event.getX() - bc.getTranslateX() - bc.prefWidth(-1) / 2;
                double srcY = event.getY() - bc.getTranslateY() - bc.prefHeight(-1) / 2;
                double trgX = srcX;
                double trgY = srcY;

                double factor = 0.05;

                if (event.getDeltaY() < 0 && bc.getScaleX() > minZoom) {
                    bc.setScaleX(bc.getScaleX() * (1 - factor));
                    bc.setScaleY(bc.getScaleY() * (1 - factor));
                    trgX = srcX * (1 - factor);
                    trgY = srcY * (1 - factor);
                } else if (event.getDeltaY() > 0 && bc.getScaleX() < maxZoom) {
                    bc.setScaleX(bc.getScaleX() * (1 + factor));
                    bc.setScaleY(bc.getScaleY() * (1 + factor));
                    trgX = srcX * (1 + factor);
                    trgY = srcY * (1 + factor);
                }
                bc.setTranslateX(bc.getTranslateX() - (trgX - srcX));
                bc.setTranslateY(bc.getTranslateY() - (trgY - srcY));

            }
        });
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - srcSceneX;
                double offsetY = event.getSceneY() - srcSceneY;
                double newTranslateX = srcTranslateX + offsetX;
                double newTranslateY = srcTranslateY + offsetY;

                bc.setTranslateX(newTranslateX);
                bc.setTranslateY(newTranslateY);

            }
        });
        this.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                srcSceneX = event.getSceneX();
                srcSceneY = event.getSceneY();
                srcTranslateX = bc.getTranslateX();
                srcTranslateY = bc.getTranslateY();

                cursorProperty().set(Cursor.CLOSED_HAND);
            }
        });
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                cursorProperty().set(Cursor.OPEN_HAND);
            }
        });
    }

    private void update(){
// building interface
        
        HBox hBox = new HBox();
    	
    	hBox.setPadding(new Insets(15, 12, 15, 12));
    	hBox.setSpacing(10);
    	hBox.setStyle("-fx-background-color: #66FFFF;");
    	
    	final ToggleGroup optionGroup = new ToggleGroup();
    	
    	Label lSelectedRow = new Label("Selected Row: ");
    	lSelectedRow.setPrefWidth(100);
    	lSelectedRow.setPrefHeight(20);
    	
    	Label lOrderBy = new Label("Order By: ");
    	lOrderBy.setPrefWidth(100);
    	lOrderBy.setPrefHeight(20);
    	
        hBox.setPrefWidth(Configuration.width);
        hBox.getChildren().addAll(lSelectedRow, cbSelectedRow, lOrderBy, cbOrderBy);
        
        BorderPane border = new BorderPane();
        border.setTop(hBox);
        border.toFront();

        this.getChildren().addAll(bc, border);
    }

    
    @SuppressWarnings("rawtypes")
	private void drawChart() {
		int _nCols = domino.getMat().getMatrixDescriptor().getNumCols();
		
	    bc.getData().removeAll(bc.getData());
	    XYChart.Series series = new XYChart.Series();
	    
	    String itemSelected = cbSelectedRow.getSelectionModel().getSelectedItem();
	    int rowSelected = unsortedList.get(itemSelected);
	    String typeOrderSelected = cbOrderBy.getSelectionModel().getSelectedItem();
	    
	    series.setName("row " + itemSelected); //row name
	    
	    ArrayList<Cell> cells = domino.getMat().getNonZeroData();
	    ArrayList<Cell> temp = new ArrayList<Cell>();
	    
	    // 1 - selecting the cells where its row is equals selected row
	    for(Cell cell: cells){
	    	if(cell.row == rowSelected)
	    		temp.add(cell);
	    }
	    cells = temp;
	    // 1 - end
	    
	    // 2 - Adding the columns where value is 0 (zero)
	    int j = 0;
	    temp = new ArrayList<Cell>(_nCols);
	    for(int column = 0; column < _nCols; column++){
	    	if(j < cells.size() && cells.get(j).col == column){
	    		temp.add(cells.get(j++));
	    	}else{
	    		temp.add(new Cell(rowSelected, column, 0));
	    	}
	    }
	    cells = temp;
	    temp = null;
	    // 2 - end
	    
	    // 3 - the list, with chart columns (name,data), is created and filled
	    ArrayList<Data<String, Number>> chartColumns = new ArrayList<>();
	    for(int i = 0; i < cells.size(); i++){
	    	
	    	String name = domino.getMat().getMatrixDescriptor().getColumnAt(i);
	    	Data<String, Number> data = new XYChart.Data(name, cells.get(i).value);
	    	chartColumns.add(i, data);
	    }
	    // 3 - end
	    
	    // [Optional] 4 - the list is ordered by value
	    if(typeOrderSelected.equals(TYPE_ORDER_BY_INCREASING)){
	    	
	    	/*
	    	 * NOTE
	    	 * This loop is adding, in begin the word, the prefix "[Column: {id}]:".
	    	 * It was necessary because the Javafx's Chart not update when the column name not update.
	    	 * Maybe, there are other way to do it
	    	 */
	    	int numDigits = String.valueOf(chartColumns.size()).length();
	    	for(int i = 0; i < chartColumns.size(); i++){	    		
	    		chartColumns.get(i).setXValue("[Column: " + String.format("%0" + numDigits + "d", i)  + "]: " + chartColumns.get(i).getXValue());
	    		//chartColumns.get(i).setXValue(chartColumns.get(i).getXValue());
	    	}
	    	
	    	chartColumns.sort(new Comparator<Data<String,Number>>(){

				@Override
				public int compare(Data<String, Number> arg0,
						Data<String, Number> arg1) {
					if(arg0.getYValue().floatValue() < arg1.getYValue().floatValue()) return -1;
					if(arg0.getYValue().floatValue() > arg1.getYValue().floatValue()) return 1;
					if(arg0.getXValue().compareTo(arg1.getXValue()) < 0) return -1;
					if(arg0.getXValue().compareTo(arg1.getXValue()) > 0) return 1;
					return 0;
				}
	    	});
	    }else if(typeOrderSelected.equals(TYPE_ORDER_BY_DECREASING)){
	    	
	    	/*
	    	 * NOTE
	    	 * This loop is adding, in begin the word, the prefix "[Column: {id}]:".
	    	 * It was necessary because the Javafx's Chart not update when the column name not update.
	    	 * Maybe, there are other way to do it
	    	 */
	    	int numDigits = String.valueOf(chartColumns.size()).length();
	    	for(int i = 0; i < chartColumns.size(); i++){	    		
	    		chartColumns.get(i).setXValue("[Column: " + String.format("%0" + numDigits + "d", i)  + "]: " + chartColumns.get(i).getXValue());
	    		//chartColumns.get(i).setXValue(chartColumns.get(i).getXValue());
	    	}
	    	
	    	chartColumns.sort(new Comparator<Data<String,Number>>(){

				@Override
				public int compare(Data<String, Number> arg0,
						Data<String, Number> arg1) {
					if(arg0.getYValue().floatValue() > arg1.getYValue().floatValue()) return -1;
					if(arg0.getYValue().floatValue() < arg1.getYValue().floatValue()) return 1;
					if(arg0.getXValue().compareTo(arg1.getXValue()) > 0) return -1;
					if(arg0.getXValue().compareTo(arg1.getXValue()) < 0) return 1;
					return 0;
				}
	    	});
	    }
	    // 4 - end
	    
	    
	    // 5 - the list is added in Chart
	    for(int i = 0; i < chartColumns.size(); i++){
	    	series.getData().add(chartColumns.get(i));
	    }
	    bc.getData().add(series);
	    // 5 - end
	}

}
