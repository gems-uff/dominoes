/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import domain.Dominoes;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.DelegateForest;
//import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.PluggableRenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import arch.Cell;

/**
 *
 * @author Daniel
 */
public class GraphPane extends Pane {

    /**
     * the graph
     */
    Forest<String, Integer> graph;
    Factory<UndirectedGraph<String, Integer>> graphFactory
            = new Factory<UndirectedGraph<String, Integer>>() {

                public UndirectedGraph<String, Integer> create() {
                    return new UndirectedSparseMultigraph<String, Integer>();
                }
            };

    Factory<Integer> edgeFactory = new Factory<Integer>() {
        int i = 0;

        public Integer create() {
            return i++;
        }
    };

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String, Integer> vv;

    FRLayout<String, Integer> treeLayout;
   // CircleLayout<String, Integer> treeLayout;

    public GraphPane(Dominoes domino) {

        //if (!isAValidDomino(domino)) {
          //  throw new IllegalArgumentException("Invalid argument.\nThe Domino parameter not is valid");
        //}
    	
        // create a simple graph for the demo
        graph = new DelegateForest<String, Integer>();

        createTree(domino);

//        treeLayout = new TreeLayout<String, Integer>(graph);
        treeLayout = new FRLayout<>(graph);
        
        vv = new VisualizationViewer<String, Integer>(treeLayout, new Dimension(600, 600));
        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<String, Paint>(){

			@Override
			public Paint transform(String i) {
				return (Paint) Color.GREEN;
			}
     
        });

        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        // add a listener for ToolTips
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));

        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);

        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

        vv.setGraphMouse(graphMouse);

        SwingNode s = new SwingNode();
        s.setContent(panel);
        this.getChildren().add(s);

    }

    private void createTree(Dominoes domino) {
    	
    	// Matrix represents a relationship among elements
    	if (domino.isSquare() && domino.getIdRow().equals(domino.getIdCol())){
    		for (int i = 0; i < domino.getMat().getMatrixDescriptor().getNumRows(); i++) {
                graph.addVertex(domino.getIdCol() + " " + i);
                for (int j = 0; j < i; j++) {
                    if (graph.addEdge(edgeFactory.create(), domino.getIdCol() + " " + i, domino.getIdCol() + " " + j)) {
                        graph.addEdge(edgeFactory.create(), domino.getIdCol() + " " + j, domino.getIdCol() + " " + i);
                    }
                }

            }
    	} else {        	
        	ArrayList<Cell> nz = domino.getMat().getNonZeroData();
        	
        	for (Cell cell : nz){
        		
        		if (cell.value > 1.0){
        			String v_i = domino.getIdRow() + " " + cell.row;
        			String v_j = domino.getIdCol() + " " + cell.col;
        		
        			if (!graph.containsVertex(v_i))
        				graph.addVertex(v_i);
        		
        			if (!graph.containsVertex(v_j))
        				graph.addVertex(v_j);
        			
        			graph.addEdge(edgeFactory.create(), v_i, v_j);
        		}
        	}    
    	}
        

    }

    private boolean isAValidDomino(Dominoes domino) {
        return ((domino.getMat().getMatrixDescriptor().getNumRows() == domino.getMat().getMatrixDescriptor().getNumCols())
                && (domino.getIdRow().equals(domino.getIdCol())));
    }

}
