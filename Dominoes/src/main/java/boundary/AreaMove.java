package boundary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.text.Font;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;
import domain.Configuration;
import com.josericardojunior.domain.Dominoes;

@SuppressWarnings("restriction")
public class AreaMove extends Pane {

    private ArrayList<Dominoes> dominoes;
    private ArrayList<Group> pieces;
    private final Rectangle background;

    private int indexFirstOperatorMultiplication = -1;
    private int indexSecondOperatorMultiplication = -1;    

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;

    private double padding = Configuration.width;
    
    private boolean transposing = false;

    // Controller Variables
    private List<MenuItem> menuItemAggregateRow = new ArrayList<>();
    private List<MenuItem> menuItemAggregateCol = new ArrayList<>();
    /**
     * Class builder with the dimension defined in parameters. here, will create
     * too a background with white color
     *
     */
    public AreaMove() {
        super();
        this.background = new Rectangle();
        this.background.setFill(new Color(1, 1, 1, 1));

        this.getChildren().addAll(background);

        this.dominoes = new ArrayList<>();
        this.pieces = new ArrayList<>();
    }

    /**
     * Add a new Domino in Area Move in position x = 0, y = 0
     *
     * @param domino The Domino information
     */
    public void add(Dominoes domino) {
        this.add(domino, 0, 0);

    }

    /**
     * Add a new Domino in Area Move in position defined for parameters
     *
     * @param domino The Domino information
     * @param x The coordinate X of this new Domino
     * @param y The coordinate Y of this new Domino
     */
    public void add(Dominoes domino, double x, double y) {
        double thisTranslateX = x;
        double thisTranslateY = y;

        ContextMenu minimenu = new ContextMenu();
        
        MenuItem menuItemTranspose = new MenuItem("Transpose");

		menuItemAggregateRow.add(new MenuItem("Aggregate by " + domino.getMat().getMatrixDescriptor().getRowType()));
        menuItemAggregateCol.add(new MenuItem("Aggregate by " + domino.getMat().getMatrixDescriptor().getColType()));
        MenuItem menuItemConfidence = new MenuItem("Confidence");
		
		MenuItem menuItemZScore = new MenuItem("Z-Score");
        MenuItem menuItemSaveInList = new MenuItem("Save");
        MenuItem menuItemViewGraph = new MenuItem("Graph");
        MenuItem menuItemViewMatrix = new MenuItem("Matrix");
        MenuItem menuItemViewChart = new MenuItem("Bar Chart");
        MenuItem menuItemViewLineChart = new MenuItem("Line Chart");
        MenuItem menuItemViewTree = new MenuItem("Tree");
        MenuItem menuItemClose = new MenuItem("Close");
        
        
        Menu menuOperate = new Menu("Operations");
        Menu menuView = new Menu("Views");

        Group group = domino.drawDominoes();
        group.getChildren().get(Dominoes.GRAPH_HISTORIC).setVisible(Configuration.visibilityHistoric);

        group.setTranslateX(thisTranslateX);
        group.setTranslateY(thisTranslateY);
        if (!this.pieces.isEmpty()) {
            for (Group g : this.pieces) {

                if (g.getTranslateY() + Dominoes.GRAPH_HEIGHT >= background.prefHeight(-1)) {
                    thisTranslateX += Dominoes.GRAPH_WIDTH;
                    thisTranslateY = background.getY();
                    continue;
                }

                if ((thisTranslateX >= g.getTranslateX()
                        && thisTranslateX < g.getTranslateX() + Dominoes.GRAPH_WIDTH
                        && thisTranslateY >= g.getTranslateY()
                        && thisTranslateY < g.getTranslateY() + Dominoes.GRAPH_HEIGHT)
                        || (thisTranslateX + Dominoes.GRAPH_WIDTH >= g.getTranslateX()
                        && thisTranslateX + Dominoes.GRAPH_WIDTH < g.getTranslateX() + Dominoes.GRAPH_WIDTH
                        && thisTranslateY >= g.getTranslateY()
                        && thisTranslateY < g.getTranslateY() + Dominoes.GRAPH_HEIGHT)
                        || (thisTranslateX >= g.getTranslateX()
                        && thisTranslateX < g.getTranslateX() + Dominoes.GRAPH_WIDTH
                        && thisTranslateY + Dominoes.GRAPH_HEIGHT >= g.getTranslateY()
                        && thisTranslateY + Dominoes.GRAPH_HEIGHT < g.getTranslateY() + Dominoes.GRAPH_HEIGHT)
                        || (thisTranslateX + Dominoes.GRAPH_WIDTH >= g.getTranslateX()
                        && thisTranslateX + Dominoes.GRAPH_WIDTH < g.getTranslateX() + Dominoes.GRAPH_WIDTH
                        && thisTranslateY + Dominoes.GRAPH_HEIGHT >= g.getTranslateY()
                        && thisTranslateY + Dominoes.GRAPH_HEIGHT < g.getTranslateY() + Dominoes.GRAPH_HEIGHT)) {

                    thisTranslateY = g.getTranslateY() + Dominoes.GRAPH_HEIGHT;
                }
            }
            group.setTranslateY(thisTranslateY);
            group.setTranslateX(thisTranslateX);
        }

        this.pieces.add(group);
        this.dominoes.add(domino);
        this.getChildren().add(group);

        //if (!domino.getIdRow().equals(domino.getIdCol())) {
          //  menuItemViewGraph.setDisable(true);
        //}

        group.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                cursorProperty().set(Cursor.OPEN_HAND);
            }
        });
        group.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - srcSceneX;
                double offsetY = event.getSceneY() - srcSceneY;
                double newTranslateX = srcTranslateX + offsetX;
                double newTranslateY = srcTranslateY + offsetY;

                // detect move out
                boolean detecMoveOutX = false;
                boolean detecMoveOutY = false;
                if (newTranslateX < background.getX()) {
                    ((Group) (event.getSource())).setTranslateX(background.getX());

                    detecMoveOutX = true;
                }
                if (newTranslateY < background.getY()) {
                    ((Group) (event.getSource())).setTranslateY(background.getY());
                    detecMoveOutY = true;
                }
                if (newTranslateX + ((Group) (event.getSource())).prefWidth(-1) > background.getX() + background.getWidth()) {
                    ((Group) (event.getSource())).setTranslateX(background.getX() + background.getWidth() - ((Group) (event.getSource())).prefWidth(-1));
                    detecMoveOutX = true;
                }
                if (newTranslateY + ((Group) (event.getSource())).prefHeight(-1) > background.getY() + background.getHeight()) {
                    ((Group) (event.getSource())).setTranslateY(background.getY() + background.getHeight() - ((Group) (event.getSource())).prefHeight(-1));
                    detecMoveOutY = true;
                }

                if (!detecMoveOutX) {
                    ((Group) (event.getSource())).setTranslateX(newTranslateX);
                }
                if (!detecMoveOutY) {
                    ((Group) (event.getSource())).setTranslateY(newTranslateY);
                }

                // detect multiplication
                int index = pieces.indexOf(group);

                for (int j = 0; j < pieces.size(); j++) {

                    if (index != j && detectMultiplication(index, j)) {
                        //menuItemReduceLines.setDisable(false);

                        break;
                    } else {
                        //menuItemReduceLines.setDisable(true);
                    }
                }
            }
        });
        group.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                srcSceneX = event.getSceneX();
                srcSceneY = event.getSceneY();
                srcTranslateX = ((Group) (event.getSource())).getTranslateX();
                srcTranslateY = ((Group) (event.getSource())).getTranslateY();

                group.toFront();
                cursorProperty().set(Cursor.CLOSED_HAND);
            }
        });
        group.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                cursorProperty().set(Cursor.OPEN_HAND);
                try {
                    multiply();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });
        group.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                cursorProperty().set(Cursor.DEFAULT);
            }
        });
        group.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

                    if (mouseEvent.getClickCount() == 2) {
                        try {
                        	if(!transposing){
                        		transpose(group);
                        	}
                        } catch (IOException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                }
            }
        });
        group.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    minimenu.show(group, e.getScreenX(), e.getScreenY());
                } else {
                    minimenu.hide();
                }
            }
        });
        minimenu.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    event.consume();
                }
            }
        });
        minimenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (((MenuItem) event.getTarget()).getText().equals(menuItemSaveInList.getText())) {
                    System.out.println("saving");
                    try {
                        saveAndSendToList(group);
                        close(group);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemClose.getText())) {
                    System.out.println("closing");
                    closePiece(group);
                }
            }
        });
        int index = dominoes.indexOf(domino);
        menuOperate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (((MenuItem) event.getTarget()).getText().equals(menuItemTranspose.getText())) {
                    try {
                    	if(!transposing){
                    		System.out.println("transposing");
                        	transpose(group);
                    	}
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemAggregateRow.get(index).getText())) {
                    try {
                        reduceColumns(group);
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemAggregateCol.get(index).getText())) {
                    try {
                        reduceLines(group);
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemConfidence.getText())) {
                	try {
						confidence(group);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
            }
        });
        menuView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (((MenuItem) event.getTarget()).getText().equals(menuItemViewGraph.getText())) {
                    drawGraph(domino);
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemViewMatrix.getText())) {
                    drawMatrix(domino);
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemViewChart.getText())) {
                    drawChart(domino);
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemViewTree.getText())) {
                    drawTree(domino);
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemViewLineChart.getText())) {
        			drawLineChart(domino);
        		}
            }
        });
        

        menuOperate.getItems().addAll(menuItemTranspose, menuItemAggregateRow.get(index),
        		menuItemAggregateCol.get(index), menuItemConfidence, menuItemZScore);
        menuView.getItems().addAll(menuItemViewChart, /*menuItemViewLineChart,*/ 
        		menuItemViewGraph, menuItemViewMatrix/*, menuItemViewTree*/);
        minimenu.getItems().addAll(menuOperate, menuView, menuItemSaveInList, menuItemClose);
    }

    /**
     * This function is called to change the parts color
     */
    void changeColor() {
        for (Group group : this.pieces) {
            ((Shape) group.getChildren().get(Dominoes.GRAPH_FILL)).setFill(Dominoes.COLOR_BACK);
            ((Shape) group.getChildren().get(Dominoes.GRAPH_LINE)).setFill(Dominoes.COLOR_BORDER);
            ((Shape) group.getChildren().get(Dominoes.GRAPH_BORDER)).setFill(Dominoes.COLOR_BORDER);
            ((Shape) group.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NORMAL_FONT);
            ((Shape) group.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NORMAL_FONT);
        }
    }

    /**
     * This function remove all parts in this area move
     */
    public void clear() {
    	if(this.pieces == null || this.dominoes == null) return;
        for (int i = 0; i < this.pieces.size(); i++) {
            this.pieces.get(i).setVisible(false);
        }
        this.pieces.removeAll(this.pieces);
        this.dominoes.removeAll(this.dominoes);

        //this.pieces = null;
        //this.dominoes = null;
    }

    /**
     * This function is used to remove a element of this Area Move
     *
     * @param group A specified element
     */
    private void close(Group group) {
        // removing in area move
        this.remove(group);
    }

    /**
     * Just close the piece defined in the parameter
     *
     * @param group The piece to will be removed
     */
    private void closePiece(Group group) {
        remove(group);
    }

    /**
     * To detect a multiplication will be used the interception between the
     * pieces, detecting by left or detecting by right has different
     * significates. All detectiong are ever in relation to index1 (left ou
     * right)
     *
     * @param index1 - piece index one
     * @param index2 - piece index two
     */
    private boolean detectMultiplication(int index1, int index2) {

        Group g1 = this.pieces.get(index1);
        Group g2 = this.pieces.get(index2);
        Dominoes d1 = this.dominoes.get(index1);
        Dominoes d2 = this.dominoes.get(index2);

        int paddingToCoupling = 1;

        boolean detect = false;

        if ((g1.getTranslateX() >= g2.getTranslateX() + Dominoes.GRAPH_WIDTH / 2
                && g1.getTranslateX() <= g2.getTranslateX() + Dominoes.GRAPH_WIDTH)
                && (g1.getTranslateY() >= g2.getTranslateY()
                && g1.getTranslateY() <= g2.getTranslateY() + Dominoes.GRAPH_HEIGHT)) {

            if (d1.getIdRow().equals(d2.getIdCol())
            		&& !d1.getIdRow().contains(Dominoes.AGGREG_TEXT)
                    && d1.getMat().getMatrixDescriptor().getNumRows() == d2.getMat().getMatrixDescriptor().getNumCols() ) {

                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_OPERATE_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_OPERATE_FONT);

                g1.setTranslateX(g2.getTranslateX() + Dominoes.GRAPH_WIDTH - paddingToCoupling);
                g1.setTranslateY(g2.getTranslateY());

                this.indexFirstOperatorMultiplication = index2;
                this.indexSecondOperatorMultiplication = index1;

                return true;
            } else {
                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                detect = true;
            }

        } else if ((g1.getTranslateX() >= g2.getTranslateX() + Dominoes.GRAPH_WIDTH / 2
                && g1.getTranslateX() <= g2.getTranslateX() + Dominoes.GRAPH_WIDTH)
                && (g1.getTranslateY() + Dominoes.GRAPH_HEIGHT >= g2.getTranslateY()
                && g1.getTranslateY() + Dominoes.GRAPH_HEIGHT <= g2.getTranslateY()
                + Dominoes.GRAPH_HEIGHT)) {

            if (d1.getIdRow().equals(d2.getIdCol())
            		&& !d1.getIdRow().contains(Dominoes.AGGREG_TEXT)
                    && d1.getMat().getMatrixDescriptor().getNumRows() == d2.getMat().getMatrixDescriptor().getNumCols()) {

                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_OPERATE_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_OPERATE_FONT);

                g1.setTranslateX(g2.getTranslateX() + Dominoes.GRAPH_WIDTH - paddingToCoupling);
                g1.setTranslateY(g2.getTranslateY());

                this.indexFirstOperatorMultiplication = index2;
                this.indexSecondOperatorMultiplication = index1;

                return true;

            } else {
                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                detect = true;
            }

        } else if ((g1.getTranslateX() + Dominoes.GRAPH_WIDTH >= g2.getTranslateX()
                && g1.getTranslateX() + Dominoes.GRAPH_WIDTH <= g2.getTranslateX() + Dominoes.GRAPH_WIDTH / 2)
                && (g1.getTranslateY() >= g2.getTranslateY()
                && g1.getTranslateY() <= g2.getTranslateY() + Dominoes.GRAPH_HEIGHT)) {

            if (d1.getIdCol().equals(d2.getIdRow())
            		&& !d1.getIdCol().contains(Dominoes.AGGREG_TEXT)
                    && d1.getMat().getMatrixDescriptor().getNumCols() == d2.getMat().getMatrixDescriptor().getNumRows()) {

                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_OPERATE_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_OPERATE_FONT);

                g1.setTranslateX(g2.getTranslateX() - Dominoes.GRAPH_WIDTH + paddingToCoupling);
                g1.setTranslateY(g2.getTranslateY());

                this.indexFirstOperatorMultiplication = index1;
                this.indexSecondOperatorMultiplication = index2;

                return true;
            } else {
                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                detect = true;
            }

        } else if ((g1.getTranslateX() + Dominoes.GRAPH_WIDTH >= g2.getTranslateX()
                && g1.getTranslateX() + Dominoes.GRAPH_WIDTH <= g2.getTranslateX() + Dominoes.GRAPH_WIDTH / 2)
                && (g1.getTranslateY() + Dominoes.GRAPH_HEIGHT >= g2.getTranslateY()
                && g1.getTranslateY() + Dominoes.GRAPH_HEIGHT <= g2.getTranslateY() + Dominoes.GRAPH_HEIGHT)) {

            if (d1.getIdCol().equals(d2.getIdRow())
            		&& !d1.getIdCol().contains(Dominoes.AGGREG_TEXT)
                    && d1.getMat().getMatrixDescriptor().getNumCols() == d2.getMat().getMatrixDescriptor().getNumRows()) {

                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_OPERATE_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_OPERATE_FONT);

                g1.setTranslateX(g2.getTranslateX() - Dominoes.GRAPH_WIDTH + paddingToCoupling);
                g1.setTranslateY(g2.getTranslateY());

                this.indexFirstOperatorMultiplication = index1;
                this.indexSecondOperatorMultiplication = index2;

                return true;
            } else {
                ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);
                ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NO_OPERATION_FONT);

                detect = true;
            }
        }

        if (!detect) {
            ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NORMAL_FONT);
            ((Text) g1.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NORMAL_FONT);
            ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFill(Dominoes.COLOR_NORMAL_FONT);
            ((Text) g2.getChildren().get(Dominoes.GRAPH_ID_COL)).setFill(Dominoes.COLOR_NORMAL_FONT);

            this.indexFirstOperatorMultiplication = -1;
            this.indexSecondOperatorMultiplication = -1;
        }

        return false;
    }

    /**
     * This will make a multiplication
     */
    private void multiply() throws IOException {

        if (this.indexFirstOperatorMultiplication != -1 && this.indexSecondOperatorMultiplication != -1) {

            Dominoes d1 = this.dominoes.get(this.indexFirstOperatorMultiplication);
            Dominoes d2 = this.dominoes.get(this.indexSecondOperatorMultiplication);

            if (d1.getIdCol().equals(d2.getIdRow())) {

                Dominoes resultOperation = control.Controller.MultiplyMatrices(d1, d2);

                double x = (this.pieces.get(this.dominoes.indexOf(d1)).getTranslateX()
                        + this.pieces.get(this.dominoes.indexOf(d2)).getTranslateX()) / 2;

                double y = (this.pieces.get(this.dominoes.indexOf(d1)).getTranslateY()
                        + this.pieces.get(this.dominoes.indexOf(d2)).getTranslateY()) / 2;

                if (this.remove(this.indexFirstOperatorMultiplication)
                        && this.indexSecondOperatorMultiplication > this.indexFirstOperatorMultiplication) {
                    this.remove(this.indexSecondOperatorMultiplication - 1);
                } else {
                    this.remove(this.indexSecondOperatorMultiplication);
                }
                
                this.add(resultOperation, x, y);
                if (Configuration.autoSave) {
                    this.saveAndSendToList(pieces.get(dominoes.indexOf(resultOperation)));
                }
            }
            this.indexFirstOperatorMultiplication = -1;
            this.indexSecondOperatorMultiplication = -1;
        }

    }

    /**
     * This function remove the matrix, in the piece and dominoes array, by the
     * element
     *
     * @param group the element to remove
     * @return True in affirmative case
     */
    public boolean remove(Group group) {
        int index = -1;
        index = this.pieces.indexOf(group);
        return remove(index);

    }

    /**
     * This function remove the matrix, in the piece and dominoes array, by the
     * index
     *
     * @param index the index to remove
     * @return True in affirmative case
     */
    public boolean remove(int index) {
        if (index > -1) {
            this.pieces.get(index).setVisible(false);
            this.dominoes.remove(index);
            this.pieces.remove(index);
            this.menuItemAggregateRow.remove(index);
            this.menuItemAggregateCol.remove(index);
        }
        return true;
    }

    /**
     * This function save all piece in AreaMove, remove and create a new matrix
     * in the List
     *
     * @throws IOException
     */
    public void saveAllAndSendToList() throws IOException {
        for (int i = 0; i < this.dominoes.size(); i++) {

            control.Controller.saveMatrix(this.dominoes.get(i));

            // adding in list
            App.CopyToList(this.dominoes.get(i));

            this.pieces.get(i).setVisible(false);
        }
        this.dominoes.removeAll(this.dominoes);
        this.pieces.removeAll(this.pieces);

    }

    /**
     * This function save, remove and create a new matrix in the List
     *
     * @param group The matrix which will suffer with this operation
     * @throws IOException
     */
    private void saveAndSendToList(Group group) throws IOException {
        control.Controller.saveMatrix(this.dominoes.get(this.pieces.indexOf(group)));

        // adding in list
        App.CopyToList(this.dominoes.get(this.pieces.indexOf(group)));
    }

    /**
     * This Functions is used to define the moving area size
     *
     * @param width
     * @param height
     */
    public void setSize(double width, double height) {

        this.background.setWidth(width + padding);
        this.background.setHeight(height);

        this.setMinWidth(width - padding);
        this.setPrefWidth(width);
        this.setMaxWidth(width + padding);
        this.setPrefHeight(height);
    }

    /**
     * This function is used to define the visibility of historic
     *
     * @param visibility True to define visible the historic
     */
    void setVisibleHistoric() {
    	boolean visibility = Configuration.visibilityHistoric;
        for (Group group : pieces) {
            group.getChildren().get(Dominoes.GRAPH_HISTORIC).setVisible(visibility);
        }
        
    }
    
    /**
     * This function is used to define the visibility of type
     *
     * @param visibility True to define visible the type
     */
    void setVisibleType() {
    	boolean visibility = Configuration.visibilityType;
        for (Group group : pieces) {
            group.getChildren().get(Dominoes.GRAPH_TYPE).setVisible(visibility);
        }
        
    }

    /**
     * This function only maked a simple animation to tranpose a matrix
     *
     * @param piece The piece to animate
     */
    private void transpose(Group piece) throws IOException {
    	transposing = true;
    	
        int duration = 500;
        
        double startAngle = piece.getRotate();
        
        int index = pieces.indexOf(piece); 
        MenuItem swapMenu = menuItemAggregateRow.get(index);
        menuItemAggregateRow.set(index, menuItemAggregateCol.get(index));
        menuItemAggregateCol.set(index, swapMenu);
        
        Dominoes domino = control.Controller.tranposeDominoes(this.dominoes.get(this.pieces.indexOf(piece)));
        Group swap = domino.drawDominoes();
        
        double swapFontSize = ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).getFont().getSize();
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).setFont(((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).getFont());
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFont(new Font(swapFontSize));
        
        double translateX = ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).getX();
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).setX(((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).getX());
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).setX(translateX);       
        
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).setText(((Text)swap.getChildren().get(Dominoes.GRAPH_ID_ROW)).getText());
        ((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).setText(((Text)swap.getChildren().get(Dominoes.GRAPH_ID_COL)).getText());
        
        RotateTransition rtPiece = new RotateTransition(Duration.millis(duration));
        rtPiece.setFromAngle(startAngle);
        rtPiece.setToAngle(startAngle + 180);

        RotateTransition rtPieceRow = new RotateTransition(Duration.millis(duration));
        rtPieceRow.setFromAngle(rtPiece.getFromAngle());
        rtPieceRow.setToAngle(startAngle - 180);

        RotateTransition rtPieceCol = new RotateTransition(Duration.millis(duration));
        rtPieceCol.setFromAngle(rtPiece.getFromAngle());
        rtPieceCol.setToAngle(rtPieceRow.getToAngle());

        RotateTransition rtType = new RotateTransition(Duration.millis(duration));
        rtType.setFromAngle(rtPiece.getFromAngle());
        rtType.setToAngle(rtPiece.getToAngle());
        
        Color colorHistoric = (Color)((Text)piece.getChildren().get(Dominoes.GRAPH_HISTORIC)).getFill();
        FillTransition ftHistoric1 = new FillTransition(Duration.millis(duration));
        ftHistoric1.setFromValue(colorHistoric);
        ftHistoric1.setToValue(Dominoes.COLOR_INIVISIBLE);
        
        FillTransition ftHistoric2 = new FillTransition(Duration.millis(duration));
        ftHistoric2.setFromValue(ftHistoric1.getToValue());
        ftHistoric2.setToValue(ftHistoric1.getFromValue());
        
        Group groupType = (Group) piece.getChildren().get(Dominoes.GRAPH_TYPE);
        Color colorType = (Color) ((Shape) groupType.getChildren().get(0)).getFill();
        FillTransition ftType1 = new FillTransition(Duration.millis(duration));
        ftType1.setFromValue(colorType);
        ftType1.setToValue(Dominoes.COLOR_INIVISIBLE);
        
        Color colorFontType = (Color) ((Text)groupType.getChildren().get(1)).getFill();
        FillTransition ftType2 = new FillTransition(Duration.millis(duration));
        ftType2.setFromValue(colorFontType);
        ftType2.setToValue(Dominoes.COLOR_INIVISIBLE);
        
        FillTransition ftType3 = new FillTransition(Duration.millis(duration));
        ftType3.setFromValue(ftType1.getToValue());
        ftType3.setToValue(ftType1.getFromValue());
        
        FillTransition ftType4 = new FillTransition(Duration.millis(duration));
        ftType4.setFromValue(ftType2.getToValue());
        ftType4.setToValue(ftType2.getFromValue());

        ParallelTransition transition1_1 = new ParallelTransition(new SequentialTransition(groupType.getChildren().get(0), ftType1));
        ParallelTransition transition1_2 = new ParallelTransition(new SequentialTransition(groupType.getChildren().get(1), ftType2));
        ParallelTransition transition1_3 = new ParallelTransition(piece.getChildren().get(Dominoes.GRAPH_HISTORIC), ftHistoric1);
        
        transition1_1.play();
        transition1_2.play();
        transition1_3.play();
        
        ParallelTransition transition2_1 = new ParallelTransition(piece, rtPiece);
        ParallelTransition transition2_2 = new ParallelTransition(piece.getChildren().get(Dominoes.GRAPH_ID_ROW), rtPieceRow);
        ParallelTransition transition2_3 = new ParallelTransition(piece.getChildren().get(Dominoes.GRAPH_ID_COL), rtPieceCol);
        
        if(!colorFontType.equals(Dominoes.COLOR_INIVISIBLE)
        		|| !colorHistoric.equals(Dominoes.COLOR_INIVISIBLE)){
        	transition2_1.setDelay(Duration.millis(duration));
        	transition2_2.setDelay(Duration.millis(duration));
        	transition2_3.setDelay(Duration.millis(duration));
        }
        
        transition2_1.play();
        transition2_2.play();
        transition2_3.play();
        
        ParallelTransition transition3_1 = new ParallelTransition(piece.getChildren().get(Dominoes.GRAPH_HISTORIC), ftHistoric2);
        ParallelTransition transition3_2 = new ParallelTransition(groupType.getChildren().get(0), ftType3);
        ParallelTransition transition3_3 = new ParallelTransition(groupType.getChildren().get(1), ftType4);
        
        if(!colorFontType.equals(Dominoes.COLOR_INIVISIBLE)
        		|| !colorHistoric.equals(Dominoes.COLOR_INIVISIBLE)){
        	transition3_1.setDelay(Duration.millis(2 * duration));
        	transition3_2.setDelay(Duration.millis(2 * duration));
        	transition3_3.setDelay(Duration.millis(2 * duration));
        }
        
        transition3_1.play();
        transition3_2.play();
        transition3_3.play();
        
        transition1_1.setOnFinished(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				
				double x = ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).getTranslateX();
	            double y = ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).getTranslateY();
	            x = Math.abs(Dominoes.GRAPH_WIDTH - x);
	            y = Math.abs(Dominoes.GRAPH_HEIGHT - y);
	            ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).setTranslateX(x);
	            ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).setTranslateY(y);
	            
	            ((Text)piece.getChildren().get(Dominoes.GRAPH_HISTORIC)).setRotate(startAngle - 180);
	            ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).setRotate(startAngle - 180);
	            
	            ((Text)piece.getChildren().get(Dominoes.GRAPH_HISTORIC)).setText(((Text)swap.getChildren().get(Dominoes.GRAPH_HISTORIC)).getText());
	            ((Text) ((Group) piece.getChildren().get(Dominoes.GRAPH_TYPE)).getChildren().get(1)).setText(((Text) ((Group) swap.getChildren().get(Dominoes.GRAPH_TYPE)).getChildren().get(1)).getText());
				
			}
		});
        
        transition3_3.setOnFinished(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {
				transposing = false;
				
			}
		});

    }
    
    /**
     * This function is responsible for summing up all lines in a column
     *
     * @param piece The piece to animate
     */
    private void reduceLines(Group piece) throws IOException {
        
    	int index = this.pieces.indexOf(piece);
    	Dominoes toReduce = this.dominoes.get(index);
    	if(!toReduce.isRowAggregatable()){
    		Dominoes domino = control.Controller.reduceDominoes(toReduce);
    		this.dominoes.set(index, domino);
    		
    		((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).setText(domino.getIdRow());
    		((Text)piece.getChildren().get(Dominoes.GRAPH_ID_ROW)).setFont(new Font(Dominoes.GRAPH_AGGREG_FONT_SIZE));

    		if (Configuration.autoSave) {
    			this.saveAndSendToList(piece);
    		}
    		
    		this.menuItemAggregateRow.get(index).setDisable(true);
    		
    	}else{
    		System.err.println("this domino is already aggregate by " + toReduce.getMat().getMatrixDescriptor().getRowType());
    	}

    }
    
    /**
     * This function is responsible for summing up all columns in a line
     *
     * @param piece The piece to animate
     */
    private void reduceColumns(Group piece) throws IOException {
    	
        int index = this.pieces.indexOf(piece);
        Dominoes toReduce = this.dominoes.get(index);

        if(!toReduce.isColAggregatable()){
        	toReduce.transpose();
        	Dominoes domino = control.Controller.reduceDominoes(toReduce);
        	domino.transpose();
        	this.dominoes.set(index, domino);
        	
        	Group swap = domino.drawDominoes();
        	
        	((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).setText(domino.getIdCol());
        	((Text)piece.getChildren().get(Dominoes.GRAPH_ID_COL)).setFont(new Font(Dominoes.GRAPH_AGGREG_FONT_SIZE));
        	
        	if (Configuration.autoSave) {
        		this.saveAndSendToList(piece);
        	}
        	
        	this.menuItemAggregateCol.get(index).setDisable(true);
    	}else{
    		System.err.println("this domino is already aggregate by " + toReduce.getMat().getMatrixDescriptor().getColType());
    	}

    }
    
    /**
     * This function is responsible for calculating the confidence on a matrix
     *
     * @param piece The piece to animate
     * @throws IOException 
     */
    private void confidence(Group piece) throws IOException {

        Color colorHistoric;
        int index = this.pieces.indexOf(piece);
        Dominoes toConfidence = this.dominoes.get(index);
        Dominoes domino = control.Controller.confidence(toConfidence);
          
        remove(index);
        add(domino, piece.getTranslateX(), piece.getTranslateY() );
        

        if (Configuration.autoSave) {
            this.saveAndSendToList(piece);
        }

    }

    private void drawGraph(Dominoes domino) {
        App.drawGraph(domino);
    }

    private void drawMatrix(Dominoes domino) {
        App.drawMatrix(domino);
    }
    
    private void drawChart(Dominoes domino) {
        App.drawChart(domino);
    }
    
    private void drawLineChart(Dominoes domino) {
        App.drawLineChart(domino);
    }
    
    private void drawTree(Dominoes domino) {
        App.drawTree(domino);
    }
}
