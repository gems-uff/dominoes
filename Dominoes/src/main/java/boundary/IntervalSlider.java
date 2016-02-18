/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Daniel
 */
@SuppressWarnings("restriction")
public class IntervalSlider extends Control {
	
    private Group slider;
    
    private Rectangle back;
    private Rectangle line;
    private Rectangle selectedArea;
    private Polygon pointMin;
    private Polygon pointMax;

    private Double sizePointer = new Double(8);
    private Double[] centerShape = new Double[]{
    		0.4 * sizePointer, 0.5 * sizePointer
    };
    
    private Double[] pointerShape = new Double[]{
    		0.2 * sizePointer, -0.3 * sizePointer,
    		0.0 * sizePointer, -0.5 * sizePointer,
    		-0.2 * sizePointer, -0.3 * sizePointer,
        
    		-0.4 * sizePointer, 0.2 * sizePointer,
    		-0.4 * sizePointer, 0.5 * sizePointer,
    		-0.2 * sizePointer, 0.6 * sizePointer,
    		
    		0.2 * sizePointer, 0.6 * sizePointer,
    		0.4 * sizePointer, 0.5 * sizePointer,
    		0.4 * sizePointer, 0.2 * sizePointer
    		
//    		center = x = 0.4 , y = 0.5
    };
    
    private double min = 0;
    private double max = 1;
    private double valueMin = 0;
    private double valueMax = 1;
    
    private double srcSceneX;
    private double srcTranslateX;
    
    private boolean enableLinkPoint = false;
    
    private String valueTooltipMin;
    private String valueTooltipMax;
    
    private ArrayList<String> tooltip;
    
    public IntervalSlider() throws IllegalArgumentException{
    	this(0, 1, 0, 1);
    }
    
    public IntervalSlider(double min, double max, double valueMin, double valueMax) throws IllegalArgumentException{
    	this(min, max, valueMin, valueMax, 100);
    }
    
    public IntervalSlider(double min, double max, double valueMin, double valueMax, double width) throws IllegalArgumentException{
    	this(min, max, valueMin, valueMax, width, null);
    }
    
//    public IntervalSlider(double min, double max, double valueMin, double valueMax, double width, ArrayList<String> tooltip, Double[] linkPoint) throws IllegalArgumentException{
    public IntervalSlider(double min, double max, double valueMin, double valueMax, double width, ArrayList<String> tooltip) throws IllegalArgumentException{
    	
        this.min = min;
        this.max = max;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        
        this.setWidth(width);
        this.setMin(min);
        this.setMax(max);
        this.setValueMin(valueMin);
        this.setValueMax(valueMax);
//        this.setLinkPoint(linkPoint);
        this.setTooltip(tooltip);
        
        this.initialize();
        
    }

	public double getMin() {
        return min;
    }

    private void setMin(double min) throws IllegalArgumentException {
        if(min > max){
        	throw new IllegalArgumentException("Invalid argument.\n"
            		+ this.getClass().toString() + ".min attribute not is valid");
        }
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    private void setMax(double max) throws IllegalArgumentException {
        if(max < min){
            throw new IllegalArgumentException("Invalid argument.\n"
            		+ this.getClass().toString() + ".max attribute not is valid");
        }
        this.max = max;
    }

    public double getValueMin() {
        return valueMin;
    }

    private void setValueMin(double value) {
        if(value < this.min || value > this.valueMax){
            value = this.min;
        }
        this.valueMin = value;
    }


	public double getValueMax() {
        return valueMax;
    }

    private void setValueMax(double value) {
        if(value > this.max || value < this.valueMin){
            value = this.max;
        }
        this.valueMax = value;
    }

    private void initialize() {
    	Color stroke = new Color(0.3,0.3, 0.3, 1);
        Color selectedPointer = new Color(88.0/255.0,188.0/255.0, 222.0/255.0, 1);
        Color selectedAreaColor = new Color(0,150.0/255.0, 201.0/255.0, 0.5);
        
        this.setHeight(sizePointer);
        
        this.setMinHeight(this.getHeight());
        
        this.line = new Rectangle(this.getWidth(), this.getHeight()/2);
        this.line.setArcHeight(5);
        this.line.setArcWidth(5);
        this.line.setFill(Color.WHITE);
        this.line.setStroke(stroke);
        this.line.setTranslateX(0);
        this.line.setTranslateY(0);
        
        this.pointMin = new Polygon();
        this.pointMin.getPoints().addAll(pointerShape);
        this.pointMin.setFill(Color.WHITESMOKE);
        this.pointMin.setStroke(stroke);
        this.pointMin.setTranslateX((this.getValueMin())/(this.max - this.min) * (this.line.getWidth()) + this.line.getTranslateX() - sizePointer/2);
        this.pointMin.setTranslateY(this.line.getHeight()/2);
        if(this.tooltip != null){
        	int indexMin = (int) (this.valueMin - min);
        	valueTooltipMin = this.tooltip.get(indexMin);
        	
        }else{
        	valueTooltipMin = String.valueOf(valueMin);
        }
        Tooltip.install(this.pointMin, new Tooltip(valueTooltipMin));
        
        this.pointMax = new Polygon();
        this.pointMax.getPoints().addAll(pointerShape);
        this.pointMax.setFill(Color.WHITESMOKE);
        this.pointMax.setStroke(stroke);
        this.pointMax.setTranslateX((this.getValueMax())/(this.max - this.min) * (this.line.getWidth()) + this.line.getTranslateX() - sizePointer/2);
        this.pointMax.setTranslateY(this.line.getHeight()/2);
        if(this.tooltip != null){
        	int indexMax = (int) (this.valueMax - min);
        	valueTooltipMax = this.tooltip.get(indexMax);
        }else{
        	valueTooltipMax = String.valueOf(valueMax);
        }
        Tooltip.install(this.pointMin, new Tooltip(valueTooltipMax));
        
        if(this.pointMin.getTranslateX() < this.line.getTranslateX())
        	this.pointMin.setTranslateX(this.line.getTranslateX());
        if(this.pointMax.getTranslateX() > this.line.getTranslateX() + this.line.getWidth())
        	this.pointMax.setTranslateX(this.line.getTranslateX() + this.line.getWidth());        
        
        if(this.pointMin.getTranslateX() >= this.line.getTranslateX() + this.line.getWidth()){
	        if(this.pointMin.getTranslateX() > this.pointMax.getTranslateX())
	        	this.pointMin.setTranslateX(this.pointMax.getTranslateX());
	        if(this.pointMax.getTranslateX() < this.pointMin.getTranslateX())
	        	this.pointMax.setTranslateX(this.pointMin.getTranslateX());
        }else if(this.pointMax.getTranslateX() <= this.line.getTranslateX()){
        	if(this.pointMax.getTranslateX() < this.pointMin.getTranslateX())
	        	this.pointMax.setTranslateX(this.pointMin.getTranslateX());
        	if(this.pointMin.getTranslateX() > this.pointMax.getTranslateX())
	        	this.pointMin.setTranslateX(this.pointMax.getTranslateX());
	        
        }
        
        this.selectedArea = new Rectangle(this.pointMax.getTranslateX() - this.pointMin.getTranslateX(), this.line.getHeight());
        this.selectedArea.setFill(selectedAreaColor);
        this.selectedArea.setTranslateX(this.pointMin.getTranslateX());
        this.selectedArea.setTranslateY(this.line.getTranslateY());
        
        this.back = new Rectangle(this.getWidth() + 4 * centerShape[0], 10);
        this.back.setTranslateX(-2 * centerShape[0]);
        this.back.setFill(new Color(0,0,0,0));
//        this.back.setFill(Color.WHITE);
        
        this.slider = new Group();
        this.slider.getChildren().addAll(
        		this.back,
                this.line,
                this.selectedArea,
                this.pointMin,
                this.pointMax);
        this.slider.setTranslateX(this.back.getTranslateX());
        this.slider.setTranslateY(0);
        
        this.pointMin.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setFill(Color.WHITE);
            }
        });
        this.pointMin.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setFill(Color.WHITESMOKE);
            }
        });
        this.pointMin.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                
                srcSceneX = event.getSceneX();
                srcTranslateX = pointMin.getTranslateX();
                
                pointMin.toFront();
                
                pointMin.setStroke(selectedPointer);
//                pointMin.setStrokeWidth(2);
            }
        });
        this.pointMin.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setStroke(stroke);
                pointMin.setStrokeWidth(1);
            }
        });
        this.pointMin.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
            	if(tooltip != null){
                	int indexMin = (int) (valueMin - min);
                	valueTooltipMin = tooltip.get(indexMin);
                }else{
                	valueTooltipMin = String.valueOf(valueMin);
                }
            	Tooltip.uninstall(pointMin, new Tooltip(valueTooltipMin));
            	
                double offsetX = event.getSceneX() - srcSceneX;
                double newTranslateX = srcTranslateX + offsetX;
                
                valueMin = min + ((max - min) * (newTranslateX - line.getTranslateX())/(line.getWidth()));
                
                if(enableLinkPoint){
                	valueMin = (int)Math.round(valueMin);
                	pointMin.setTranslateX(line.getTranslateX() + (line.getWidth() * ((valueMin - min) / (max - min))));
                }else{
                	pointMin.setTranslateX(newTranslateX);
                }
                
                if(newTranslateX < line.getTranslateX() || valueMin < min){
                    pointMin.setTranslateX(line.getTranslateX());
                    valueMin = min;
                }else if(newTranslateX > pointMax.getTranslateX() || valueMin > valueMax){
                    pointMin.setTranslateX(pointMax.getTranslateX());
                    valueMin = valueMax;
                }
                
                selectedArea.setTranslateX(pointMin.getTranslateX());
                selectedArea.setWidth(pointMax.getTranslateX() - pointMin.getTranslateX());

                if(tooltip != null){
                	int indexMin = (int) (valueMin - min);
                	valueTooltipMin = tooltip.get(indexMin);
                }else{
                	valueTooltipMin = String.valueOf(valueMin);
                }
                Tooltip.install(pointMin, new Tooltip(valueTooltipMin));
            }
        });
        
        this.pointMax.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMax.setFill(Color.WHITE);
            }
        });
        this.pointMax.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMax.setFill(Color.WHITESMOKE);
            }
        });
        this.pointMax.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                srcSceneX = event.getSceneX();
                srcTranslateX = pointMax.getTranslateX();
                
                pointMax.toFront();
                
                pointMax.setStroke(selectedPointer);
//                pointMax.setStrokeWidth(2);
            }
        });
        this.pointMax.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMax.setStroke(stroke);
                pointMax.setStrokeWidth(1);
            }
        });
        this.pointMax.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
            	if(tooltip != null){
                	int indexMax = (int) (valueMax - min);
                	valueTooltipMax = tooltip.get(indexMax);
                	
                }else{
                	valueTooltipMax = String.valueOf(valueMax);
                }
            	Tooltip.uninstall(pointMax, new Tooltip(valueTooltipMax));
            	
                double offsetX = event.getSceneX() - srcSceneX;
                double newTranslateX = srcTranslateX + offsetX;

                
                
                valueMax = min + ((max - min) * (newTranslateX - line.getTranslateX())/(line.getWidth()));
                if(enableLinkPoint){
                	valueMax = (int)Math.round(valueMax);
                	pointMax.setTranslateX(line.getTranslateX() + (line.getWidth() * ((valueMax - min) / (max - min))));
                }else{
                	pointMax.setTranslateX(newTranslateX);
                }
                if(newTranslateX > line.getTranslateX() + line.getWidth() || valueMax > max ){
                    pointMax.setTranslateX(line.getTranslateX() + line.getWidth());
                    valueMax = max ;
                }else if(newTranslateX < pointMin.getTranslateX() || valueMax < valueMin){
                    pointMax.setTranslateX(pointMin.getTranslateX());
                    valueMax = valueMin;
                }
                selectedArea.setWidth(pointMax.getTranslateX() - pointMin.getTranslateX());
                
                if(tooltip != null){
                	int indexMax = (int) (valueMax - min);
                	valueTooltipMax = tooltip.get(indexMax);
                }else{
                	valueTooltipMax = String.valueOf(valueMax);
                }
                Tooltip.install(pointMax, new Tooltip(valueTooltipMax));
                
            }
        });
        
        this.getChildren().addAll(new FlowPane(this.slider));
    }

    public void setTooltip(ArrayList<String> tooltip)throws IllegalArgumentException{
    	if(this.tooltip != null
    			&& tooltip != null
    			&& tooltip.size() != (max - min)){
    		throw new IllegalArgumentException("Invalid argument."
            		+ "\nThe IntervalSlider.Tooltip attribute is null"); 
    	}
    	
    	this.tooltip = tooltip;
    }
    
    public void setLinkPointers(boolean val){
    	this.enableLinkPoint = val;
    }

    public String getValueToolTipMin(){
    	return valueTooltipMin;
    }
    
    public String getValueToolTipMax(){
    	return valueTooltipMax;
    }
}
