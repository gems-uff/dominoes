package domain;

import java.util.ArrayList;

import arch.IMatrix2D;
import arch.Matrix2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class Dominoes {
    public final static double GRAPH_WIDTH = 100;
    public final static double GRAPH_HEIGHT = 50;

    public static Color COLOR_FILL = new Color(1, 1, 1, 1);
    public static Color COLOR_LINE = new Color(0.86, 0.86, 0.86, 1);
    public static Color COLOR_NORMAL_FONT = new Color(0, 0, 0, 1);
    public static Color COLOR_NO_OPERATION_FONT = new Color(1, 0, 0, 1);
    public static Color COLOR_OPERATE_FONT = new Color(0, 1, 0, 1);
    public static Color COLOR_HISTORIC = new Color(0.86, 0.86, 0.86, 1);
    public static Color COLOR_INIVISIBLE = new Color(0, 0, 0, 0);
    public static Color COLOR_TYPE = COLOR_LINE;

    /*
     This variables are used to know the sequence of the matrix information 
     in the hour of to save/load in the .TXT format
     */
    public final static int INDEX_TYPE = 0;
    public final static int INDEX_ID_ROW = 1;
    public final static int INDEX_ID_COL = 2;
    public final static int INDEX_HEIGHT = 3;
    public final static int INDEX_WIDTH = 4;
    public final static int INDEX_HIST = 5;
    public final static int INDEX_MATRIX = 6;

    public final static int INDEX_SIZE = 7;

    /*
     This variables are used to know the sequence of the elements,
     the Group (Graphicaly) relative to this Domino, in time of the insert
     */
    private static double GRAPH_ARC = 10;
    public final static int GRAPH_BORDER = 0;
    public final static int GRAPH_FILL = 1;
    public final static int GRAPH_LINE = 2;
    public final static int GRAPH_HISTORIC = 3;
    public final static int GRAPH_TYPE = 4;
    public final static int GRAPH_TRANSPOSE_ID_ROW = 6;
    public final static int GRAPH_TRANSPOSE_ID_COL = 5;
    public final static int GRAPH_ID_ROW = 5;
    public final static int GRAPH_ID_COL = 6;
    

    public final static int GRAPH_SIZE = 7;

    /*
     This variables are used to know the type of matrix
     */
    public final static int TYPE_BASIC = 0;
    public final static int TYPE_DERIVED = 1;
    public final static int TYPE_SUPPORT = 2;
    public final static int TYPE_CONFIDENCE = 3;
    public final static int TYPE_LIFT = 4;
    public final static String TYPE_BASIC_CODE = "B";
    public final static String TYPE_DERIVED_CODE = "D";
    public final static String TYPE_SUPPORT_CODE = "S";
    public final static String TYPE_CONFIDENCE_CODE = "C";
    public final static String TYPE_LIFT_CODE = "L";

    private String idRow;
    private String idCol;
    private ArrayList<String> historic;
    private int type;
    private IMatrix2D mat = null;

    public Dominoes() {
    }

    /**
     * Class build. The type, for default, is Basic.
     *
     * @param idRow - identifier row of the Dominos matrix
     * @param idCol - identifier row of the Dominos matrix
     * @param mat - matrix2D
     * @throws IllegalArgumentException - in case of invalid parameters
     */
    public Dominoes(String idRow, String idCol, IMatrix2D mat) throws IllegalArgumentException {
        this.setIdRow(idRow);
        this.setIdCol(idCol);

        this.setMat(mat);

        this.historic = new ArrayList<>();
        this.historic.add(idRow);
        this.historic.add(idCol);

        this.type = Dominoes.TYPE_BASIC;
    }

    /**
     * Class build. The type, for default, is Derived
     *
     * @param type
     * @param idRow - identifier row of the Dominos matrix
     * @param idCol - identifier row of the Dominos matrix
     * @param historic - The dominoes historic derivated
     * @param mat - matrix2D
     * @throws IllegalArgumentException - in case of invalid parameters
     */
    public Dominoes(int type, String idRow, String idCol, ArrayList<String> historic, Matrix2D mat) throws IllegalArgumentException {
        this.setIdRow(idRow);
        this.setIdCol(idCol);

        this.setMat(mat);

        this.setHistoric(historic);
        if (type == Dominoes.TYPE_BASIC
                || (type != Dominoes.TYPE_DERIVED
                && type != Dominoes.TYPE_CONFIDENCE
                && type != Dominoes.TYPE_SUPPORT
                && type != Dominoes.TYPE_LIFT)) {
            throw new IllegalArgumentException("Invalid argument.\nThe Type attribute not is defined or not is valid");
        }
        this.type = type;
    }
    
//    /**
//     * Class build. The type, for default, is Derived
//     *
//     * @param type
//     * @param idRow - identifier row of the Dominos matrix
//     * @param idCol - identifier row of the Dominos matrix
//     * @throws IllegalArgumentException - in case of invalid parameters
//     */
//    public Dominoes(int type, String idRow, String idCol) throws IllegalArgumentException {
//    	this.setIdRow(idRow);
//        this.setIdCol(idCol);
//
//        this.historic = new ArrayList<>();
//        this.historic.add(idRow);
//        this.historic.add(idCol);
//
//        this.type = Dominoes.TYPE_BASIC;
//        
//        this.mat = null;
//    }

    /**
     * Class Builder This function is used when the user to do a multiplication,
     * this will return a new matrix with data according with a real
     * multiplication. The type, for default, is the type of the first parameter
     *
     * @param firstOperator The first matrix in this operation
     * @param secondOperator The second matrix in this operation
     * @param mat
     * @return A new matrix
     */
   /* public Dominoes(Dominoes firstOperator, Dominoes secondOperator, byte[][] mat) throws IllegalArgumentException {
        this.historic = new ArrayList<>();

        this.historic.addAll(firstOperator.getHistoric());
        this.historic.addAll(this.historic.size(), secondOperator.getHistoric());

        this.setIdRow(firstOperator.getIdRow());
        this.setIdCol(secondOperator.getIdCol());

        this.setMat(mat);
        this.type = firstOperator.getType();
    }*/

    /**
     * From this Dominoes, this function will build a piece (graphically)
     * respective to this dominoes
     *
     * @return - A javafx.scene.Group (Graphic) to draw in scene
     */
    public Group drawDominoes() {

        double padding = 1;
        
        Rectangle border = new Rectangle(GRAPH_WIDTH, GRAPH_HEIGHT);
        border.setFill(Dominoes.COLOR_LINE);
        border.setArcHeight(Dominoes.GRAPH_ARC);
        border.setArcWidth(Dominoes.GRAPH_ARC);
        border.setX(0);
        border.setY(0);

        Rectangle back = new Rectangle(GRAPH_WIDTH - 2, GRAPH_HEIGHT - 2);
        back.setFill(Dominoes.COLOR_FILL);
        back.setArcHeight(Dominoes.GRAPH_ARC);
        back.setArcWidth(Dominoes.GRAPH_ARC);
        back.setX(1);
        back.setY(1);

        Rectangle line = new Rectangle(GRAPH_WIDTH / 2 - 1, 10, 2, 30);
        line.setFill(Dominoes.COLOR_LINE);
        line.setArcHeight(Dominoes.GRAPH_ARC);
        line.setArcWidth(Dominoes.GRAPH_ARC);

        Text idRow = new Text(this.getIdRow());

        idRow.setFont(new Font("Arial", 15));
        idRow.setFill(Dominoes.COLOR_NORMAL_FONT);
        idRow.setX(5);
        idRow.setY(2 * Dominoes.GRAPH_HEIGHT / 5);
        idRow.toFront();

        Text idCol = new Text(this.getIdCol());
        idCol.setFont(new Font("Arial", 15));
        idCol.setFill(Dominoes.COLOR_NORMAL_FONT);
        idCol.setX(Dominoes.GRAPH_WIDTH / 2 + 5);
        idCol.setY(2 * Dominoes.GRAPH_HEIGHT / 5);
        idCol.toFront();

        String auxHistoric = "";
        for (int i = 0; i < this.historic.size(); i++) {
            auxHistoric += this.historic.get(i);

        }
        Text historic = new Text(auxHistoric);
        historic.setFont(new Font("Arial", 10));
        historic.setFill(Dominoes.COLOR_HISTORIC);
        if(this.type == Dominoes.TYPE_BASIC){
        	historic.setFill(Dominoes.COLOR_INIVISIBLE);
        }
        historic.setX(2);
        historic.setY(3 * Dominoes.GRAPH_HEIGHT / 5);
        historic.setWrappingWidth(Dominoes.GRAPH_WIDTH - 2);
        historic.toFront();

        Circle circle = new Circle(back.getX() + back.getWidth() / 2, back.getY() + back.getHeight() / 2, 5, Dominoes.COLOR_TYPE);

        Text type = new Text();
        type.setFill(Dominoes.COLOR_NORMAL_FONT);
        switch (this.getType()) {
            case Dominoes.TYPE_BASIC:
                type.setText(Dominoes.TYPE_BASIC_CODE);
                
                circle.setFill(COLOR_INIVISIBLE);
            	type.setFill(COLOR_INIVISIBLE);
                break;
            case Dominoes.TYPE_DERIVED:
                type.setText(Dominoes.TYPE_DERIVED_CODE);
                
                circle.setFill(COLOR_INIVISIBLE);
                type.setStroke(COLOR_INIVISIBLE);
                break;
            case Dominoes.TYPE_SUPPORT:
                type.setText(Dominoes.TYPE_SUPPORT_CODE);
                break;
            case Dominoes.TYPE_CONFIDENCE:
                type.setText(Dominoes.TYPE_CONFIDENCE_CODE);
                break;
            case Dominoes.TYPE_LIFT:
                type.setText(Dominoes.TYPE_LIFT_CODE);
                break;
        }
        type.setX(circle.getCenterX() - circle.getRadius()/2 - padding);
        type.setY(circle.getCenterY() + circle.getRadius()/2 + padding);
        
        circle.toFront();
        type.toFront();

        return new Group(border, back, line, historic, new Group(circle, type),idRow, idCol);
    }


    /**
     * User to obtain the complete historic this
     *
     * @return this Historic
     */
    public ArrayList<String> getHistoric() {
        return this.historic;
    }

    /**
     * Used to obtain the Id Column this Domino
     *
     * @return Return the Id Column value
     */
    public String getIdCol() {
        return this.idCol;
    }

    /**
     * Used to obtain the Id Column this Domino
     *
     * @return Return the Id Row value
     */
    public String getIdRow() {
        return this.idRow;
    }

    /**
     * Used to obtain the Matrix this Domino
     *
     * @return Return the Matrix value
     */
    public IMatrix2D getMat() {
        return this.mat;
    }

    /**
     * Used to obtain the Type of Matrix
     *
     * @return Return the Type value
     */
    public int getType() {
        return type;
    }



    /**
     * Used to change the Historic of this Domino
     *
     * @param historic The Historic value
     * @throws IllegalArgumentException
     */
    private void setHistoric(ArrayList<String> historic) {
        if (historic == null || historic.size() == 0) {
            historic = new ArrayList<>();
            historic.add(this.idRow);
            historic.add(this.idCol);
        }
        this.historic = historic;
    }

    /**
     * Used to change the Id Column this Domino
     *
     * @param idCol The Id Column value
     * @throws IllegalArgumentException
     */
    private void setIdCol(String idCol) throws IllegalArgumentException {
        if (idCol == null || idCol.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe IdCol attribute is null or void");
        }
        this.idCol = idCol;
    }

    /**
     * Used to change the Id Row this Domino
     *
     * @param idRow The Id Row value
     * @throws IllegalArgumentException
     */
    private void setIdRow(String idRow) throws IllegalArgumentException {
        if (idRow == null || idRow.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe IdRow attribute is null or void");
        }
        this.idRow = idRow;
    }

    /**
     * Used to change the Matrix this Domino
     *
     * @param mat The Matrix value
     * @throws IllegalArgumentException
     */
    public void setMat(IMatrix2D mat){
        if (mat == null) {
            throw new IllegalArgumentException("Invalid argument.\nThe Mat attribute is null");
        }
        
        this.mat = mat;
    }

    /**
     * This function just invert the Historic
     *
     * @return the historic invert
     */
    public void transpose() {
        
        // transpose the row with column
        Integer swapSide = new Integer(0);
        String swapId = new String("");

        swapId = this.getIdRow();
        this.setIdRow(this.getIdCol());
        this.setIdCol(swapId);

        this.type = Dominoes.TYPE_DERIVED;
        if(this.getIdRow().equals(this.getIdCol())){
            this.type = Dominoes.TYPE_SUPPORT;
        }
        
        // transpose this historic
        while (!(this.getIdRow().equals(this.getHistoric().get(0))
                && this.getIdCol().equals(this.getHistoric().get(this.historic.size() - 1)))) {
            ArrayList<String> newHistoric = new ArrayList<>();
            for (int i = this.historic.size() - 1; i >= 0; i--) {
                newHistoric.add(this.historic.get(i));
            }
            this.historic = newHistoric;
        }
        
        IMatrix2D _newMat = mat.transpose();
        setMat(_newMat);
    }

    public Dominoes multiply(Dominoes dom2) {
    	
    	Dominoes domResult = new Dominoes();
    	
    	domResult.type = Dominoes.TYPE_DERIVED;

        if (idCol.equals(dom2.getIdRow())) {
            domResult.type = Dominoes.TYPE_SUPPORT;
        }
        
        try {
			domResult.setMat(
					getMat().multiply(
							dom2.getMat(), false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        domResult.historic = new ArrayList<>();
        domResult.historic.addAll(getHistoric());
        domResult.historic.addAll(domResult.historic.size(), dom2.getHistoric());
        
        domResult.setIdRow(getIdRow());
        domResult.setIdCol(dom2.getIdCol());
        
        return domResult;
    }
    
    public Dominoes cloneNoMatrix(){
    	Dominoes cloned = new Dominoes(getIdRow(), getIdCol(), getMat());
    	
    	return cloned;
    }
    
    
}
