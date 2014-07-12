package control;

import util.ConfigurationFile;
import dao.DominoesDao;
import dao.FactoryDao;
import domain.Configuration;
import domain.Dominoes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Controller {

    /**
     * This function has begin when the user want using all matrices in Dominoes
     * database.
     *
     * @return Dominoes List
     * @throws IOException
     */
    public static ArrayList<Dominoes> loadAllMatrices() {
        DominoesDao result = FactoryDao.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        try {
            return result.loadAllMatrices();
        } catch (IOException ex) {
            return null;
        } catch (SQLException ex){
        	return null;
        }
    }
    
    /**
     * This function is used to initialize the Configuration class
     * @throws IOException
     * @throws Exception 
     */
    public static void loadConfiguration() throws IOException, Exception{
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
        DominoesDao result = FactoryDao.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.loadMatrix(dominoes);
    }
    
    /**This function has begin when the user want to multiply two matrices. 
     *
     * @param dom1 First operator in multiplication
     * @param dom2 Second operator in multiplication
     * @return The result of multiplication
     */
    public static Dominoes MultiplyMatrices(Dominoes dom1, Dominoes dom2) {
        // call dominoes
        
        // test
        byte[][] resultMat = new byte[dom1.getMat().length][dom2.getMat()[0].length];
        
        for (int i = 0; i < dom1.getMat()[0].length; i++) {
            for (int j = 0; j < dom2.getMat().length; j++) {
                for (int k = 0; k < resultMat.length; k++) {
                    resultMat[i][j] += dom1.getMat()[i][k] * dom2.getMat()[k][j];
                }
                
            }
        }
        // end test

        Dominoes result = new Dominoes(dom1, dom2, resultMat);
        result.multiply(dom1.getIdRow(), dom2.getIdCol());
        return result;
    }
    
    /**
     * This functions is called when user want remove a matrix of database
     * @param dominoes The dominoes corresponding to matrix
     * @return True, in affirmative case
     * @throws IOException 
     */
    public static boolean removeMatrix(Dominoes dominoes) throws IOException {
        DominoesDao result = FactoryDao.getDominoesDao(Configuration.accessMode);

        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        
        return result.removeMatrix(dominoes);
    }
    
    /**
     * This function has begin when the user want to save a matrix
     *
     * @param dominoes information to be saved
     * @return true, in case afirmative.
     * @throws IOException
     */
    public static boolean saveMatrix(Dominoes dominoes) throws IOException {
        DominoesDao result = FactoryDao.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.saveMatrix(dominoes);
    }

    /**This function has begin when the user want to transpose a matrix.
     * 
     * @param domino Matrix to be transposed
     * @return Return the transpose of the matrix in the parameter
     */
    public static Dominoes tranposeDominoes(Dominoes domino) {
        
        // call dominoes
        
        // test
        byte swap;
        byte[][] resultMat = new byte[domino.getWidth()][domino.getHeight()];
        for (int i = 0; i < domino.getHeight(); i++) {
            for (int j = 0; j < domino.getWidth(); j++) {
                resultMat[j][i] = domino.getMat()[i][j];
            }
        }
        // end test
        
        domino.setMat(resultMat);
        
        domino.transpose();

        return domino;
    }
    
}
