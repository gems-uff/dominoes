package control;

import java.awt.Frame;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;

import boundary.App;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import util.ConfigurationFile;
import com.josericardojunior.dao.DominoesDao;
import com.josericardojunior.dao.DominoesSQLDao;
import domain.Configuration;
import com.josericardojunior.domain.Dominoes;

public class Controller {
	
	public static String[] args = null;
	
	public static final String MESSAGE_STARTED = "started";
	public static final String MESSAGE_FINISHED = "finished";
	
	public static final int amout_tiles = 6;
	
	public static String message = "";
	
	public static ArrayList<Dominoes> resultLoadMatrices;
	
	public static int indexTileSelected = -1;
	
	
    /**
     * This function has begin when the user want using all matrices in Dominoes
     * database.
     *
     * @return Dominoes List
     * @throws IOException
     */
    public static void loadAllMatrices(Date _beginDate, Date _endDate, String _projectName) {
    	resultLoadMatrices = new ArrayList<Dominoes>();
    	
        try {
            Controller.resultLoadMatrices = DominoesSQLDao.loadAllMatrices(
            		Configuration.database, _projectName, Configuration.processingUnit, _beginDate, _endDate);
        } catch (IOException ex) {
        	ex.printStackTrace();
            
        } catch (SQLException ex){
        	ex.printStackTrace();
        	
        } catch (Exception ex){
        	ex.printStackTrace();
        	
        }
    }
    
    /**
     * This function is used to initialize the Configuration class
     * @throws IOException
     * @throws Exception 
     */
    public static void loadConfiguration() throws IOException, Exception{
    	new Configuration();
        new ConfigurationFile().loadConfigurationFile();
    }

    /**
     * This function has begin when the user want using a matrix in Dominoes
     * database.
     *
     * @param dominoes the row and the column this dominoes will be used in the
     * search to load
     * @return The domino, in database, which contains the row and the column
     * equal to row and column of the domino passed how parameters to this
     * function
     * @throws IOException
     */
    public static Dominoes loadMatrix(Dominoes dominoes) throws IOException {
        /*DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.loadMatrix(dominoes);*/
    	
    	return null;
    }
    
    /**This function has begin when the user want to multiply two matrices. 
     *
     * @param dom1 First operator in multiplication
     * @param dom2 Second operator in multiplication
     * @return The result of multiplication
     */
    public static Dominoes MultiplyMatrices(Dominoes dom1, Dominoes dom2) {
        // call dominoes
        Dominoes result = dom1.multiply(dom2);
        return result;
    }
    
    /**
     * This functions is called when user want remove a matrix of database
     * @param dominoes The dominoes corresponding to matrix
     * @return True, in affirmative case
     * @throws IOException 
     */
    public static boolean removeMatrix(Dominoes dominoes) throws IOException {
        /*DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);

        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        
        return result.removeMatrix(dominoes);*/
    	return true;
    }
    
    /**
     * This function has begin when the user want to save a matrix
     *
     * @param dominoes information to be saved
     * @return true, in case afirmative.
     * @throws IOException
     */
    public static boolean saveMatrix(Dominoes dominoes) throws IOException {
       /* DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.saveMatrix(dominoes);*/
    	return true;
    }

    /**This function has begin when the user want to transpose a matrix.
     * 
     * @param domino Matrix to be transposed
     * @return Return the transpose of the matrix in the parameter
     */
    public static Dominoes tranposeDominoes(Dominoes domino) {
        
    	domino.transpose();
    	
        return domino;

    }
    
    /**This function has begin when the user want calculate the confidence
     * 
     * @param support domino Matrix to be calculated
     * @return Return the confidence of the support matrix
     */
    public static Dominoes confidence(Dominoes domino) {
        
    	domino.confidence();
    	
        return domino;

    }
    
    /**This function has begin when the user want to reduce a matrix.
     * 
     * @param domino Matrix to be reduced
     * @return Return the reduced matrix in the parameter
     */
    public static Dominoes reduceDominoes(Dominoes domino) {
        
    	if(domino.reduceRows()){
    		return domino;
    	}
    	return null;
        
    }

    public static double opposite(double size, double index){
    	if(size < 0 || index < 0 || index > size){
    		throw new IllegalArgumentException("Invalid parameter."
    				+ "\nController.opposite(...) parameter is invalid");
    	}
    	double result = Math.abs(index - size);
    	return result;
   }

	public static String changeFormat(SimpleDateFormat source,
			SimpleDateFormat target, String format) throws ParseException {		
		Date date = source.parse(format);
		String result = target.format(date);
		return result;
	}

	public static JFrame FXToJFrame(Scene scene) {
		JFrame jFrame = new JFrame();
		JFXPanel panel = new JFXPanel();
		panel.setScene(scene);
		jFrame.add(panel);
		
		return jFrame;
	}
	
	public static void printPrompt(String string) {
    	Controller.message = string; 
    	System.out.println(string);
		
	}
	
//	public static void changeMatrixHeaderColor(Color color){
//		App.getVisual().getCh
//		
//	}
}
