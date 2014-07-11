/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import domain.Dominoes;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.layout.CachingLayout;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Random;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

/**
 *
 * @author Daniel
 */
public class GraphPane extends Pane {

    private int CIRCLE_SIZE = 15;
    private double maxZoom = 5;
    private double minZoom = 0.1;

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;

    public GraphPane(Dominoes domino) {

        if (!isAValidDomino(domino)) {
            throw new IllegalArgumentException("Invalid argument.\nThe Domino parameter not is valid");
        }
        Group viz1 = new Group();

        Graph<String, Number> graph1 = TestGraphs.createChainPlusIsolates(0, domino.getMat().length);

//        Layout<String, Number> circleLayout = new CircleLayout<>(graph1);
        Layout<String, Number> circleLayout = new CircleLayout<>(graph1);
//        Layout<String, Number> circleLayout = new ISOMLayout<>(graph1);

        VisualizationModel<String, Number> vm1 = new DefaultVisualizationModel<>(circleLayout, new Dimension(400, 400));

        renderGraph(domino, graph1, circleLayout, viz1);

        maxZoom *= viz1.getScaleX();
        minZoom *= viz1.getScaleX();

        this.getChildren().add(viz1);

        this.setOnScroll(new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                double srcX = event.getX() - viz1.getTranslateX() - viz1.prefWidth(-1) / 2;
                double srcY = event.getY() - viz1.getTranslateY() - viz1.prefHeight(-1) / 2;
                double trgX = srcX;
                double trgY = srcY;

                double factor = 0.05;

                if (event.getDeltaY() < 0 && viz1.getScaleX() > minZoom) {
                    viz1.setScaleX(viz1.getScaleX() * (1 - factor));
                    viz1.setScaleY(viz1.getScaleY() * (1 - factor));
                    trgX = srcX * (1 - factor);
                    trgY = srcY * (1 - factor);
                } else if (event.getDeltaY() > 0 && viz1.getScaleX() < maxZoom) {
                    viz1.setScaleX(viz1.getScaleX() * (1 + factor));
                    viz1.setScaleY(viz1.getScaleY() * (1 + factor));
                    trgX = srcX * (1 + factor);
                    trgY = srcY * (1 + factor);
                }
                viz1.setTranslateX(viz1.getTranslateX() - (trgX - srcX));
                viz1.setTranslateY(viz1.getTranslateY() - (trgY - srcY));

            }
        });
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - srcSceneX;
                double offsetY = event.getSceneY() - srcSceneY;
                double newTranslateX = srcTranslateX + offsetX;
                double newTranslateY = srcTranslateY + offsetY;

                viz1.setTranslateX(newTranslateX);
                viz1.setTranslateY(newTranslateY);

            }
        });
        this.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                srcSceneX = event.getSceneX();
                srcSceneY = event.getSceneY();
                srcTranslateX = viz1.getTranslateX();
                srcTranslateY = viz1.getTranslateY();

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

    /**
     * Render a graph to a particular <code>Group</code>
     *
     * @param graph
     * @param layout
     * @param viz
     */
    private void renderGraph(Dominoes domino, Graph<String, Number> graph, Layout<String, Number> layout, Group viz) {
        
        byte max = findMaxValue(domino);
        byte min = findMinValue(domino);
        
        int i = 0, j = 1;
        for (String v : graph.getVertices()) {
            String id = "[" + domino.getIdRow() + " " + i++ + "]";

            Point2D p = layout.transform(v);
            Circle circle = CircleBuilder.create()
                    .centerX(p.getX())
                    .centerY(p.getY())
                    .radius(CIRCLE_SIZE)
                    .build();

            Text text = new Text(id);
            text.setFill(Dominoes.COLOR_TYPE);
            text.setX(circle.getCenterX());
            text.setY(circle.getCenterY());
            text.toFront();
            
            viz.getChildren().add(circle);
            viz.getChildren().add(text);
        }
        i = 0;
        j = 0;
        for (String v : graph.getVertices()) {
            j = 0;
            for (String v2 : graph.getVertices()) {
                if(i == j){
                    i++;
                    j = 0;
                    break;
                }
                if (v.equals(v2)) {
                    j++;
                    continue;
                }
                String id = "[" + domino.getMat()[i][j] + "]";


                Point2D pStart = layout.transform(v);
                Point2D pEnd = layout.transform(v2);
                
                Path line = new Path();
                line.getElements().add(new MoveTo(pStart.getX(), pStart.getY()));

                line.getElements().add(new LineTo(pEnd.getX(), pEnd.getY()));
                
                line.setStrokeWidth(1 + 10*(domino.getMat()[i][j] - min)/(max - min));
                Tooltip.install(line, new Tooltip(id));
                line.toBack();

                Text text = new Text(id);
                text.setFill(Dominoes.COLOR_TYPE);
                text.setTranslateX((pStart.getX() + pEnd.getX()) / 2);
                text.setTranslateY((pStart.getY() + pEnd.getY()) / 2);
                text.toFront();
                
                j++;

                viz.getChildren().add(line);
                viz.getChildren().add(text);
            }
        }
    }

    private boolean isAValidDomino(Dominoes domino) {
        return ((domino.getWidth() == domino.getWidth())
                && (domino.getIdRow().equals(domino.getIdCol()))
                && (domino.getMat().length == domino.getMat()[0].length));
    }
    
    private byte findMaxValue(Dominoes domino) {

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
    }
}
