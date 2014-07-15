/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import arch.MatrixDescriptor;
import domain.Dominoes;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author Daniel
 */
public class MatrixPane extends Pane {

    private double maxZoom = 20;
    private double minZoom = 0.05;

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;

    public MatrixPane(Dominoes domino) {
    	
        System.out.println("Rows: " + domino.getMat().getMatrixDescriptor().getNumRows() +
        		" Cols: " + domino.getMat().getMatrixDescriptor().getNumCols());
        
        Group group = new Group();

        float max = domino.getMat().findMaxValue();
        float min = domino.getMat().findMinValue();

        double padding = 0;
        double cellSpace = 20;

        MatrixDescriptor _descriptor = domino.getMat().getMatrixDescriptor();

        for (int i = 0; i < _descriptor.getNumRows(); i++) {
        	float[] _row = domino.getMat().getRow(_descriptor.getRowAt(i));
        	
            for (int j = 0; j < _descriptor.getNumCols(); j++) {
                Rectangle back = new Rectangle(i * (cellSpace + padding) + padding, j * (cellSpace + padding) + padding, cellSpace, cellSpace);
                back.setFill(new Color(0, 0, 0, 0.1));
                Rectangle front = new Rectangle(i * (cellSpace + padding) + padding, j * (cellSpace + padding) + padding, cellSpace, cellSpace);
                front.setFill(new Color(0, 0, 0, (_row[j] - min) / (max - min)));
                front.toFront();

                Group cell = new Group(back, front);
                
                Text text = new Text(i * (cellSpace + padding) + padding, j * (cellSpace + padding) + padding + 20, String.valueOf(_row[j]));
//                text.setFont(new Font("Arial", 20));
                text.setFill(Color.WHITE);
                text.toFront();

                group.getChildren().add(cell);
                group.getChildren().add(text);
            }
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

        this.getChildren().add(group);
    }

   /* private byte findMaxValue(Dominoes domino) {

        byte[][] mat = domino.getMat();
        byte result = mat[0][0];

        for (int i = 0; i < domino.getHeight(); i++) {
            for (int j = 0; j < domino.getWidth(); j++) {
                if (mat[i][j] > result) {
                    result = mat[i][j];
                }

            }
        }
        return result;
    }

    private byte findMinValue(Dominoes domino) {
        byte[][] mat = domino.getMat();
        byte result = mat[0][0];

        for (int i = 0; i < domino.getHeight(); i++) {
            for (int j = 0; j < domino.getWidth(); j++) {
                if (mat[i][j] < result) {
                    result = mat[i][j];
                }

            }
        }
        return result;
    }*/

}
