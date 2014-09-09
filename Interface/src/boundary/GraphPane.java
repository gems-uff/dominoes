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
import edu.uci.ics.jung.io.graphml.parser.NodeElementParser;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.PluggableRenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import arch.Cell;
import arch.MatrixDescriptor;

/**
 *
 * @author Daniel
 */
public class GraphPane extends Pane {

    /**
     * the graph
     */
	Map<String, NodeInfo> nodes = new HashMap<>();
	Map<String, NodeLink> edges = new HashMap<>();
	ArrayList<NodeInfo> nodesHighlighted = new ArrayList<>();
    Forest<String, String> graph;
    
    UndirectedGraph<String, String> graphFactory= new UndirectedSparseMultigraph<>();

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String, String> vv;

    FRLayout<String, String> treeLayout;
   // CircleLayout<String, Integer> treeLayout;

    public GraphPane(Dominoes domino) {
    	
        // create a simple graph for the demo
        graph = new DelegateForest<String, String>();

        createTree(domino);

//        treeLayout = new TreeLayout<String, Integer>(graph);
        treeLayout = new FRLayout<>(graph);
        
        vv = new VisualizationViewer<String, String>(treeLayout, new Dimension(600, 600));
       
        
        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<String, Paint>(){

			@Override
			public Paint transform(String i) {
				return (Paint) nodes.get(i).getColor();
			}
     
        });
        
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<String, String>() {
			
			@Override
			public String transform(String arg0) {
				return "";
			}
		});
        
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<String, String>() {

			@Override
			public String transform(String arg0) {
				return "";
			}
		});
        
        vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<String, Paint>() {
			
			@Override
			public Paint transform(String arg0) {
				return edges.get(arg0).getColor();
			}
		});
        
        final PickedState<String> pickedState = vv.getPickedVertexState();
        pickedState.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object obj = e.getItem();
				
				if (obj instanceof String){
					String vertexId = (String) obj;
					
					nodes.get(vertexId).setHighlighted(pickedState.isPicked(vertexId));
				}
				
			}
		});

        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new Transformer<String, String>() {
			
			@Override
			public String transform(String arg0) {
				return nodes.get(arg0).getUserData();
			}
		});
        //vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));

       final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);

        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());;



        SwingNode s = new SwingNode();
        s.setContent(vv);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(addTransformingModeOptions());
        borderPane.setBottom(addThresholdSlider());
        borderPane.setCenter(s);
        this.getChildren().add(borderPane);  
    }

    private Node addThresholdSlider() {
    	HBox hBox = new HBox();
    	
    	hBox.setStyle("-fx-background-color: #66FFFF;");
    	
    	return hBox;
    }

	private HBox addTransformingModeOptions() {
    	HBox hBox = new HBox();
    	
    	hBox.setPadding(new Insets(15, 12, 15, 12));
    	hBox.setSpacing(10);
    	hBox.setStyle("-fx-background-color: #66FFFF;");
    	
    	final ToggleGroup optionGroup = new ToggleGroup();
    	
    	Label lblMouseMode = new Label("Mouse Mode: ");
    	lblMouseMode.setPrefSize(100,  20);
    	
    	RadioButton rbTransform = new RadioButton("Translate");
    	rbTransform.setPrefSize(100, 20);
    	rbTransform.setToggleGroup(optionGroup);
    	rbTransform.setUserData("T");
    	rbTransform.setSelected(true);
    	
    	RadioButton rbPick = new RadioButton("Picking");
    	rbPick.setPrefSize(100, 20);
    	rbPick.setUserData("P");
    	rbPick.setToggleGroup(optionGroup);
    	
    	
    	Label lblSearch = new Label("Search: ");
    	lblSearch.setPrefSize(70, 20);
    	
    	TextField tf = new TextField();
    	
    	tf.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				String toFind = tf.getText().toLowerCase();
				
				
				for (NodeInfo nodeInfo : nodesHighlighted)
					nodeInfo.setHighlighted(false);
				
				if (nodesHighlighted.size() > 0){
					nodesHighlighted.clear();
					vv.repaint();
				}
				
				
				if (toFind.length() > 2){
					for (NodeInfo nodeInfo : nodes.values()){						
						if (nodeInfo.getUserData().toLowerCase().contains((toFind))){
							nodeInfo.setHighlighted(true);							
							nodesHighlighted.add(nodeInfo);
						}
					}
					
					vv.repaint();
				}
			}
		});
    	
    	hBox.getChildren().addAll(lblMouseMode, rbTransform, rbPick, lblSearch, tf);
    	
    	
    	optionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable,
					Toggle oldValue, Toggle newValue) {
				if (optionGroup.getSelectedToggle() != null){
					DefaultModalGraphMouse dmg = (DefaultModalGraphMouse) vv.getGraphMouse();
					
					if (optionGroup.getSelectedToggle().getUserData().equals("T")){
						dmg.setMode(Mode.TRANSFORMING);
					} else 	if (optionGroup.getSelectedToggle().getUserData().equals("P")){
						dmg.setMode(Mode.PICKING);
					}
				}
				
			}
		});
    	
    	return hBox;

    }

	private void createTree(Dominoes domino) {
    	
    	// Matrix represents a relationship among elements
    	ArrayList<Cell> nz = domino.getMat().getNonZeroData();
    	MatrixDescriptor desc = domino.getMat().getMatrixDescriptor();
    	float min = domino.getMat().findMinValue();
    	float max = domino.getMat().findMaxValue();
    	float dist = max - min;
    	
    	for (Cell cell : nz){
    		NodeInfo n1 = null;
    		NodeInfo n2 = null;
    		String id1 = null;
    		String id2 = null;
    		
    		if (domino.isSquare() && domino.getIdRow().equals(domino.getIdCol())){
    			
    			if (cell.row == cell.col){
    				continue;
    			}
    		}
			
    		id1 = "R" + cell.row;
    		id2 = "C" + cell.col;
    		
    		if (nodes.containsKey(id1)){  
				n1 = nodes.get(id1); 
			}
			else {
				n1 = new NodeInfo(id1);
				n1.setColor(Color.BLUE);
				n1.setUserData(desc.getRowAt(cell.row));
				nodes.put(n1.toString(), n1);
				graph.addVertex(n1.toString());
			}
				
			if (nodes.containsKey(id2)){
				n2 = nodes.get(id2); 
			}
			else {
				n2 = new NodeInfo(id2);
				n2.setColor(Color.GREEN);
				n2.setUserData(desc.getColumnAt(cell.col));
				nodes.put(n2.toString(), n2);
				graph.addVertex(n2.toString());
			}
			
			float perc = cell.value / dist;
			System.out.println(perc);
			
			NodeLink edge = new NodeLink("E" + n1.toString() + n2.toString(), cell.value);
			edge.setColor(new Color(1.0f - perc, 1.0f - perc, 1.0f - perc));
			edges.put(edge.getId(), edge);
			graph.addEdge(edge.getId(), n1.toString(), n2.toString());
    	}
    }

    private boolean isAValidDomino(Dominoes domino) {
        return ((domino.getMat().getMatrixDescriptor().getNumRows() == domino.getMat().getMatrixDescriptor().getNumCols())
                && (domino.getIdRow().equals(domino.getIdCol())));
    }

}
