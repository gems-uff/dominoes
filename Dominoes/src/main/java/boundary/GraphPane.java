/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import com.josericardojunior.domain.Dominoes;
import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
//import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.io.graphml.parser.NodeElementParser;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.PluggableRenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JSlider;
import javax.xml.ws.Action;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import com.josericardojunior.arch.Cell;
import com.josericardojunior.arch.MatrixDescriptor;
import com.josericardojunior.domain.Dominoes;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
//import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.event.GraphEvent.Edge;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
/**
 *
 * @author Daniel
 */
@SuppressWarnings("restriction")
public class GraphPane extends BorderPane {

    /**
     * the graph
     */
	private Map<String, NodeInfo> nodes = new HashMap<>();
	private Map<String, NodeLink> edges = new HashMap<>();
	private ArrayList<NodeInfo> nodesHighlighted = new ArrayList<>();
	private DirectionDisplayPredicate edgePredicate;
	private VertexDisplayPredicate vertexPredicate;
	//private Forest<String, String> graph;
	private Graph<String, String> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String, String> vv;

    FRLayout<String, String> treeLayout;
   // ISOMLayout<String, String> treeLayout;
    //CircleLayout<String, Integer> treeLayout;

    public GraphPane(Dominoes domino) {
    	
        // create a simple graph for the demo
        graph = new DelegateForest<String, String>();
    	
    	if (domino.getType() == Dominoes.TYPE_SUPPORT)
    		graph = new UndirectedSparseGraph<String, String>();
    	else
    		graph = new DirectedSparseGraph<String, String>();
        
        createTree(domino);

        treeLayout = new FRLayout<>(graph);
        //treeLayout = new CircleLayout(graph);
        
        vv = new VisualizationViewer<String, String>(treeLayout, new Dimension(400, 400));
       
        
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
        graphMouse.setZoomAtMouse(false);
        
        vv.getRenderContext().setEdgeIncludePredicate(edgePredicate);
        
        vv.getRenderContext().setVertexIncludePredicate(vertexPredicate);

        SwingNode s = new SwingNode();
        s.setContent(panel);
        

        this.setTop(addTransformingModeOptions());
        this.setBottom(addThresholdSlider(domino.getMat().findMinValue(), 
        		domino.getMat().findMaxValue()));
        this.setCenter(s);
        //this.getChildren().add(borderPane);  
    }

    private Node addThresholdSlider(float min, float max) {
    	HBox hBox = new HBox();
    	
    	hBox.setPadding(new Insets(15, 12, 15, 12));
    	hBox.setStyle("-fx-background-color: #66FFFF;");
    	
    	Label lblThreshold = new Label("Threshold: ");
    	lblThreshold.setPrefSize(100, 20);
    	
    	Label lblValue = new Label("Value: ");
    	lblValue.setPrefSize(50, 20);
    	TextField tfValue = new TextField(String.valueOf(min));
    	
    	Slider thresholdSlider = new Slider();
    	thresholdSlider.setMin(Math.floor(min));
    	thresholdSlider.setMax(Math.ceil(max));
    	thresholdSlider.setMajorTickUnit(Math.ceil((max - min) / 5));
    	thresholdSlider.setMinorTickCount(1);
    	thresholdSlider.setBlockIncrement(1);
    	thresholdSlider.setSnapToTicks(true);
    	thresholdSlider.setShowTickMarks(true);
    	
    	
    	thresholdSlider.valueProperty().addListener(new ChangeListener<Number>() {
    		

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				
				edgePredicate.setThreshold(newValue.floatValue());
				vertexPredicate.setThreshold(newValue.floatValue());
				
				vv.repaint();
				
				tfValue.setText(String.format(Locale.US, "%.2f", newValue.floatValue()));
				
			}
		});
    	
    	tfValue.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				float value;
				
				try{
					value = Float.parseFloat(tfValue.getText());
				}catch(Exception ex){
					value = 0;
				}
				edgePredicate.setThreshold(value);
				vertexPredicate.setThreshold(value);
				
				vv.repaint();
				
				thresholdSlider.setValue(value);
				
			}
		});
    	
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
					
					if (nodesHighlighted.size() == 1){
						Layout<String, String> layout = vv.getGraphLayout();
			            Point2D q = layout.transform(nodesHighlighted.get(0).id);
			            Point2D lvc = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter());
			            final double dx = (lvc.getX() - q.getX()) / 10;
			            final double dy = (lvc.getY() - q.getY()) / 10;
			            
			            Runnable animator = new Runnable() {

			                public void run() {
			                    for (int i = 0; i < 10; i++) {
			                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).translate(dx, dy);
			                        try {
			                            Thread.sleep(100);
			                        } catch (InterruptedException ex) {
			                        }
			                    }
			                }
			            };
			            
			            Thread thread = new Thread(animator);
			            thread.start();
					}
					vv.repaint();
				}
			}
		});
    	
    	hBox.getChildren().addAll(lblThreshold, thresholdSlider, lblValue, tfValue, lblSearch, tf);
    	
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
    	
    	RadioButton rbTransform = new RadioButton("Pan & Zoom");
    	rbTransform.setPrefSize(100, 20);
    	rbTransform.setToggleGroup(optionGroup);
    	rbTransform.setUserData("T");
    	rbTransform.setSelected(true);
    	
    	RadioButton rbPick = new RadioButton("Picking");
    	rbPick.setPrefSize(100, 20);
    	rbPick.setUserData("P");
    	rbPick.setToggleGroup(optionGroup);
    	
    	
    	
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
    	
    	
    	
    	hBox.getChildren().addAll(lblMouseMode, rbTransform, rbPick);
    	
    	return hBox;

    }

	private void createTree(Dominoes domino) {
 
    	// Matrix represents a relationship among elements
    	ArrayList<Cell> nz = domino.getMat().getNonZeroData();
    	MatrixDescriptor desc = domino.getMat().getMatrixDescriptor();
    	float min = domino.getMat().findMinValue();
    	float max = domino.getMat().findMaxValue();
    	float dist = max - min;
    	
    	List<Cell> _cells = new ArrayList<>();
    	
    	if (domino.getType() == Dominoes.TYPE_SUPPORT){
    		for (Cell cell : nz){
    			
    			boolean toAdd = true;
    			
    			for (Cell _c : _cells){
    				if (_c.row == cell.col && _c.col == cell.row){
    					toAdd = false;
    					break;
    				}
    			}
    			
    			if (toAdd){
    				_cells.add(cell);
    			}
    		}
    	} else {
    		_cells = nz;
    	}
    	
    	
    	for (Cell cell : _cells){	
    		NodeInfo n1 = null;
    		NodeInfo n2 = null;
    		String id1 = null;
    		String id2 = null;
    		
    		if (domino.isSquare() && domino.getIdRow().equals(domino.getIdCol())){
    			
    			if (cell.row == cell.col){
    				continue;
    			}
    		}
			
    		if (domino.isSquare()){
    			id1 = Integer.toString(cell.row);
    			id2 = Integer.toString(cell.col);
    		} else {
        		id1 = "R" + cell.row;
        		id2 = "" + cell.col;   			
    		}

    		
    		
    		if (nodes.containsKey(id1)){  
				n1 = nodes.get(id1); 
			}
			else {
				n1 = new NodeInfo(id1);
				n1.setColor(Color.BLUE);
				n1.setUserData(desc.getRowAt(cell.row));
				n1.setThreshold(Float.POSITIVE_INFINITY);
				nodes.put(n1.toString(), n1);
				graph.addVertex(n1.toString());
			}
				
			if (nodes.containsKey(id2)){
				n2 = nodes.get(id2); 
				n2.setThreshold(Math.max(n2.getThreshold(), cell.value));
			}
			else {
				
				n2 = new NodeInfo(id2);
				if (domino.getType() == Dominoes.TYPE_SUPPORT) 
					n2.setColor(Color.BLUE);
				else
					n2.setColor(Color.GREEN);
				
				n2.setUserData(desc.getColumnAt(cell.col));
				n2.setThreshold(cell.value);
				nodes.put(n2.toString(), n2);
				graph.addVertex(n2.toString());
			}
			float perc = cell.value / dist;
			float intensityColor = Math.max(0.1f, perc);
			
			NodeLink edge = new NodeLink("E" + n1.toString() + n2.toString(), cell.value);
			edge.setColor(new Color(1.0f - intensityColor, 1.0f - intensityColor, 1.0f - intensityColor));
			
			edges.put(edge.getId(), edge);
			if (domino.getType() == Dominoes.TYPE_SUPPORT) 
				graph.addEdge(edge.getId(), n1.toString(), n2.toString(), EdgeType.UNDIRECTED);
			else
				graph.addEdge(edge.getId(), n1.toString(), n2.toString(), EdgeType.DIRECTED);
			
    	}
    	
    	vertexPredicate = new VertexDisplayPredicate(nodes);
    	edgePredicate = new DirectionDisplayPredicate(nodes, edges);
    	
    	
    }

    private boolean isAValidDomino(Dominoes domino) {
        return ((domino.isSquare())
                && (domino.getIdRow().equals(domino.getIdCol())));
    }

    private final static class VertexDisplayPredicate 
		implements Predicate<Context<Graph<String, String>,String>> {
    	
    	private float threshold = 0;
    	private Map<String, NodeInfo> nodes;
    	
		public VertexDisplayPredicate(Map<String, NodeInfo> nodes2) {
			this.setNodes(nodes2);
		}

		@Override
		public boolean evaluate(Context<Graph<String, String>, String> context) {
			Graph<String,String> g = context.graph;
			
			return nodes.get(context.element).getThreshold() >= threshold
					&& (g.inDegree(context.element) > 0 || 
					g.outDegree(context.element) > 0);		
		}

		public void setThreshold(float threshold) {
			this.threshold = threshold;
		}
		
		public void setNodes(Map<String, NodeInfo> nodes) {
			this.nodes = nodes;
		}
    	
    }
    
    private final static class DirectionDisplayPredicate 
    	implements Predicate<Context<Graph<String, String>,String>> {
    	
    	private float threshold = 0;
    	private Map<String, NodeInfo> nodes;
    	private Map<String, NodeLink> links;
    	
    	public DirectionDisplayPredicate(Map<String,NodeInfo> nodes, 
    			Map<String,NodeLink> links) {
			this.nodes = nodes;
			this.links = links;
		}

		@Override
		public boolean evaluate(Context<Graph<String, String>, String> context) {
			
			Graph<String, String> g = context.graph;
			
			String e = context.element;
			
			return links.get(e).getWediht() >= threshold;
		}

		public float getThreshold() {
			return threshold;
		}

		public void setThreshold(float threshold) {
			this.threshold = threshold;
		}
    	
    }
       
    
}
