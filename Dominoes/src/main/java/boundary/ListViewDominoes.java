package boundary;

import domain.Configuration;
import com.josericardojunior.domain.Dominoes;

import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

@SuppressWarnings("restriction")
public class ListViewDominoes extends ListView<Group> {

    private ObservableList<Group> pieces;
    private ArrayList<Dominoes> dominoes;

    private double padding = 20;

    private boolean visibilityHistoric;

    /**
     * This class builder initialize this list and your arrays with values
     * defined in the parameter Array.
     *
     * @param array Values to initialize this list and your array
     */
    public ListViewDominoes(ArrayList<Dominoes> array) {
        this.visibilityHistoric = true;

        this.pieces = FXCollections.observableList(new ArrayList<Group>());
        this.dominoes = new ArrayList<>(); 
        
        this.Configure(array);
    }
    
    public void Configure(ArrayList<Dominoes> array){
    	this.clear();
    	
    	if (array != null) {
            for (Dominoes dom : array) {
                this.add(dom);
            }

        }
        

        this.setItems(this.pieces);
    }

    /**
     * This function adds a Dominoes in the list
     *
     * @param domino The dominoes to resultMultiplication
     * @return true in affirmative case
     * @throws IllegalArgumentException
     */
    public boolean add(Dominoes domino) throws IllegalArgumentException {

        boolean result = false;

        if (domino == null) {
            return result;
        }

        if (isAdded(domino)) {
            return result;
        }

        ContextMenu minimenu = new ContextMenu();
        MenuItem menuItemToAreaMove = new MenuItem("Copy To Area Move");
        MenuItem menuItemRemove = new MenuItem("Remove");

        Group group = domino.drawDominoes();
        group.getChildren().get(Dominoes.GRAPH_HISTORIC).setVisible(visibilityHistoric);

        Tooltip tooltip = new Tooltip(domino.getMat().getMatrixDescriptor().getRowType()
        		+ " x "
        		+ domino.getMat().getMatrixDescriptor().getColType()
        		+ " : "
        		+ domino.getMat().getMatrixDescriptor().getNumRows()
        		+ " x "
        		+ domino.getMat().getMatrixDescriptor().getNumCols());
        Tooltip.install(group, tooltip);

        group.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                cursorProperty().set(Cursor.OPEN_HAND);
            }
        });
        group.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                cursorProperty().set(Cursor.CLOSED_HAND);
            }
        });
        group.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if (cursorProperty().get() == Cursor.CLOSED_HAND) {

                    int indexTargetRelative = (event.getY() < 0) ? (int) ((event.getY() + (-1) * (Dominoes.GRAPH_HEIGHT + 6)) / (Dominoes.GRAPH_HEIGHT + 6)) : (int) (event.getY() / (Dominoes.GRAPH_HEIGHT + 6));

                    if (/*this.*/pieces == null) {
                        return;
                    }

                    int indexSource = getSelectionModel().getSelectedIndex();

                    moveItems(indexSource, indexTargetRelative);
                }
                cursorProperty().set(Cursor.OPEN_HAND);
            }
        });
        group.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if (cursorProperty().get() == Cursor.CLOSED_HAND) {

                } else {
                    cursorProperty().set(Cursor.DEFAULT);
                }
            }
        });

        group.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        //System.out.println("copy to area move");
                        copyFromListToAreaMove(group);
                    }
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
                // choise menu item multiply
                if (((MenuItem) event.getTarget()).getText().equals(menuItemToAreaMove.getText())) {
                   // System.out.println("copy to area move");
                    copyFromListToAreaMove(group);
                } else if (((MenuItem) event.getTarget()).getText().equals(menuItemRemove.getText())) {
                    System.out.println("removing");
                    try {
                        removeFromListAndArea(group);
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
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

        minimenu.getItems().addAll(menuItemToAreaMove, menuItemRemove);

        this.dominoes.add(domino);

        this.pieces.add(group);

        result = true;
        return result;
    }

    /**
     * This function checks if a domino parameters is added in the list
     *
     * @param domino The domino to check
     * @return true, in affirmative case
     * @throws IllegalArgumentException
     */
    private boolean isAdded(Dominoes domino) throws IllegalArgumentException {
        if (domino == null) {
            throw new IllegalArgumentException("list not initialized");
        }
        for (Dominoes d : this.dominoes) {
            if (d.getIdRow().equals(domino.getIdRow())
                    && d.getIdCol().equals(domino.getIdCol())
                    && domino.getHistoric().toString().equals(d.getHistoric().toString())) {
                return true;
            }
        }
        return false;
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
        for (int i = 0; i < this.pieces.size(); i++) {
            this.pieces.get(i).setVisible(false);
        }
        this.pieces.removeAll(this.pieces);
        this.dominoes.removeAll(this.dominoes);
    }

    /**
     * This Function copy from this list, to area move, a domino.
     *
     * @param group domino to copy
     */
    private void copyFromListToAreaMove(Group group) {

        Dominoes auxDomino = this.dominoes.get(this.pieces.indexOf(group));

        // adding in area move
        App.copyToArea(auxDomino.cloneNoMatrix());
    }

    /**
     * This function is used to move a selected domino in the list.
     *
     * @param indexSource The selected index. The dominoes in this position will
     * suffer a change in their position
     * @param indexTargetRelative The position target.
     */
    public void moveItems(int indexSource, int indexTargetRelative) {
        int indexTarget = indexSource + indexTargetRelative;
//        int indexTarget = indexTargetRelative;

        // catch index selected
        if (this.pieces == null || this.dominoes == null) {
            return;
        }

        if ((indexTarget < 0 || indexTarget >= this.pieces.size())
                || (indexSource < 0 || indexSource >= this.pieces.size())
                || (indexSource == indexTarget)) {
            return;
        }

        if (indexTarget > indexSource) {
            Group sourceGroup = new Group();
            sourceGroup = this.pieces.get(indexSource);
            Dominoes sourceDominoes = new Dominoes(Configuration.processingUnit);
            sourceDominoes = this.dominoes.get(indexSource);

            for (int i = indexSource; i < indexTarget; i++) {
                this.pieces.set(i, this.pieces.get(i + 1));
                this.dominoes.set(i, this.dominoes.get(i + 1));
            }

            this.pieces.set(indexTarget, sourceGroup);
            this.dominoes.set(indexTarget, sourceDominoes);

        } else if (indexTarget < indexSource) {
            Group sourceGroup = new Group();
            sourceGroup = this.pieces.get(indexSource);
            Dominoes sourceDominoes = new Dominoes(Configuration.processingUnit);
            sourceDominoes = this.dominoes.get(indexSource);

            for (int i = indexSource; i > indexTarget; i--) {
                this.pieces.set(i, this.pieces.get(i - 1));
                this.dominoes.set(i, this.dominoes.get(i - 1));
            }

            this.pieces.set(indexTarget, sourceGroup);
            this.dominoes.set(indexTarget, sourceDominoes);
        }
    }

    /**
     * This Functions is used to define the moving area size
     *
     * @param width
     * @param height
     */
    void setSize(double width, double height) {
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
    	boolean visibility = this.visibilityHistoric;
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
     * This function remove only a element this list.
     *
     * @param group element to remove
     * @return true in affimative case
     */
    public boolean remove(Group group) {
        int index = this.pieces.indexOf(group);
        if (index > -1) {
            group.setVisible(false);
            this.dominoes.remove(index);
            this.pieces.remove(index);
            return true;
        }
        return false;
    }

    /**
     * This function remove the element of the list and of the move area
     *
     * @param group Element to remove
     * @return true, in affirmative case
     * @throws IOException
     */
    private boolean removeFromListAndArea(Group group) throws IOException {
        return App.removeMatrix(this.dominoes.get(pieces.indexOf(group)), group);
    }

}
