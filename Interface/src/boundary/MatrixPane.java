/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import arch.Cell;
import arch.MatrixDescriptor;
import domain.Dominoes;

/**
 *
 * @author Daniel
 */
@SuppressWarnings("restriction")
public class MatrixPane extends Pane {
	
    private double maxZoom = 2;
    private double minZoom = 0.05;

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;
    
    private List<Rectangle> recHeaders;
    private List<Rectangle> recCells;
    private List<Float> cells;
    private Color cellColor = new Color(0, 0, 1.0f, 1.0f);
    
    private float max, min;
    
	public MatrixPane(Dominoes domino) {
		
		this.setStyle("-fx-background-color: #E6E6E6");
		
		this.recHeaders = new ArrayList<>();
		this.recCells = new ArrayList<>();
		this.cells = new ArrayList<>();
		
        System.out.println("Rows: " + domino.getMat().getMatrixDescriptor().getNumRows() +
        		" Cols: " + domino.getMat().getMatrixDescriptor().getNumCols());
        
        Group group = new Group();
        MatrixDescriptor _descriptor = domino.getMat().getMatrixDescriptor();
        
        this.min = domino.getMat().findMinValue();
        this.max = domino.getMat().findMaxValue();
        
        double beginRowHead;
        double endRowHead;
        double beginColumnHead;
        double endColumnHead;
        
        double width;
    	double height;
        
        double padding = 0;
        double cellSpace = 20;
        double charSpace = 7;
        double largerSize = 0;
        
        
        int _nRows = _descriptor.getNumRows();
        int _nCols = _descriptor.getNumCols();
        
        for(int i = 0; i < _nRows; i++){
        	if(domino.getMat().getMatrixDescriptor().getRowAt(i).length() > largerSize){
        		largerSize = domino.getMat().getMatrixDescriptor().getRowAt(i).length();
        	}
        }
        
        beginRowHead = -1 * largerSize * charSpace;
        endRowHead = 0;
        
        for(int i = 0; i < _nCols; i++){
        	if(domino.getMat().getMatrixDescriptor().getColumnAt(i).length() > largerSize){
        		largerSize = domino.getMat().getMatrixDescriptor().getColumnAt(i).length();
        	}
        }
        
        beginColumnHead = -1 * largerSize * charSpace;
        endColumnHead = 0;
        
        width = Math.abs(endRowHead - beginRowHead);
    	height = cellSpace;
    	
        // draw the label of the matrix row/columns
        for (int i = 0; i < _nRows; i++) {
        	largerSize = domino.getMat().getMatrixDescriptor().getRowAt(i).length();
        	Rectangle back = new Rectangle(width, height);
            back.setFill(new Color(1, 1, 1, 1));
            back.setTranslateX(0);
            back.setTranslateY(0);
            back.toBack();
            
            Rectangle front = new Rectangle(width, height);
            front.setFill(new Color(0, 0, 1, 0.5 + (0.2 * ((-1) * i%2))));
            front.setTranslateX(0);
            front.setTranslateY(0);
            front.toFront();

            this.recHeaders.add(front);
            
            Group cell = new Group(back, front);
            cell.setTranslateX(beginRowHead);
            cell.setTranslateY(i * (cellSpace + padding) + padding);
            
            Text text = new Text(domino.getMat().getMatrixDescriptor().getRowAt(i));
            text.setTranslateX(beginRowHead);
            text.setTranslateY(i * (cellSpace + padding) + padding + height);
            if(i%2 == 0){
            	text.setFill(Color.WHITE);
            }else{
            	text.setFill(Color.BLACK);
            }
            text.toFront();

            group.getChildren().add(new Group(cell, text));
        	
        }
        
        width = Math.abs(endColumnHead - beginColumnHead);
    	height = cellSpace;
        
        for (int i = 0; i < _nCols; i++) {
        	Rectangle back = new Rectangle(width, height);
        	back.setTranslateX(0);
        	back.setTranslateY(0);
            back.setFill(new Color(1, 1, 1,1));

            Rectangle front = new Rectangle(width, height);
            front.setTranslateX(0);
        	front.setTranslateY(0);
            front.setFill(new Color(0, 0, 1, 0.5 + (0.2 * ((-1) * i%2))));
            
            front.toFront();
            
            this.recHeaders.add(front);
            
            Group cell = new Group(back, front);
            
            Text text = new Text(domino.getMat().getMatrixDescriptor().getColumnAt(i));
            text.setTranslateX(endColumnHead);
            text.setTranslateY(height - 5.0);

            if(i%2 == 0){
            	text.setFill(Color.WHITE);
            }else{
            	text.setFill(Color.BLACK);
            }
            text.toFront();
            
            Group g = new Group(cell, text);
            g.setTranslateX(1 + (i * (cellSpace + padding) + padding) + (height/2 - width/2));
            g.setTranslateY(-1 + ((-1) * (cellSpace + padding)) - (width/2 - height/2));
            g.getTransforms().add(new Rotate(-90, width / 2.0f, height / 2.0f, 1.0f, Rotate.Z_AXIS));
            
            group.getChildren().add(g);
        }
        
        // draw the matrix information
        ArrayList<Cell> cells = domino.getMat().getNonZeroData();
        
        for (Cell _matCell : cells){
        	Rectangle back = new Rectangle(cellSpace, cellSpace);
            back.setFill(new Color(1, 1, 1, 1));
            Rectangle front = new Rectangle(cellSpace, cellSpace);
        
            front.setFill(new Color(cellColor.getRed(),
            		cellColor.getGreen(),
            		cellColor.getBlue(),
            		(_matCell.value - min) / (max - min)));           
            front.toFront();
            
            this.cells.add(_matCell.value);
            this.recCells.add(front);
            
            Group cell = new Group(back, front);
            cell.setTranslateX(_matCell.col * (cellSpace + padding) + padding);
            cell.setTranslateY(_matCell.row * (cellSpace + padding) + padding);
            
            Tooltip.install(cell, new Tooltip(String.valueOf(_matCell.value)));
                
            group.getChildren().add(cell);
        }

        this.setOnScroll(new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                double srcX = event.getX() - group.getTranslateX() - group.prefWidth(-1) / 2;
                double srcY = event.getY() - group.getTranslateY() - group.prefHeight(-1) / 2;
                double trgX = srcX;
                double trgY = srcY;

                double factor = 0.05;

                if (event.getDeltaY() < 0 && group.getScaleX() > minZoom) {
                    group.setScaleX(group.getScaleX() * (1 - factor));
                    group.setScaleY(group.getScaleY() * (1 - factor));
                    trgX = srcX * (1 - factor);
                    trgY = srcY * (1 - factor);
                } else if (event.getDeltaY() > 0 && group.getScaleX() < maxZoom) {
                    group.setScaleX(group.getScaleX() * (1 + factor));
                    group.setScaleY(group.getScaleY() * (1 + factor));
                    trgX = srcX * (1 + factor);
                    trgY = srcY * (1 + factor);
                }
                group.setTranslateX(group.getTranslateX() - (trgX - srcX));
                group.setTranslateY(group.getTranslateY() - (trgY - srcY));

            }
        });
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - srcSceneX;
                double offsetY = event.getSceneY() - srcSceneY;
                double newTranslateX = srcTranslateX + offsetX;
                double newTranslateY = srcTranslateY + offsetY;

                group.setTranslateX(newTranslateX);
                group.setTranslateY(newTranslateY);

            }
        });
        this.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                srcSceneX = event.getSceneX();
                srcSceneY = event.getSceneY();
                srcTranslateX = group.getTranslateX();
                srcTranslateY = group.getTranslateY();

                cursorProperty().set(Cursor.CLOSED_HAND);
            }
        });
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                cursorProperty().set(Cursor.OPEN_HAND);
            }
        });
        
        this.getChildren().add(new FlowPane(group));
        
    }


}
