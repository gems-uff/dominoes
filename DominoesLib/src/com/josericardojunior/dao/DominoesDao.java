package dao;

import domain.Dominoes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface DominoesDao {
    
    /**This function has begin when the user want using all matrices in Dominoes
     * database.
     *
     * @return Dominoes List
     * @throws IOException, SQLException
     */
    public ArrayList<Dominoes> loadAllMatrices(String _database, String _device) throws IOException, SQLException, Exception;
    
    /**This function has begin when the user want using a matrix in Dominoes 
     * database.
     *
     * @param dominoes - the row and the column this dominoes will be used in 
     * the search to load
     * @return The domino, in database, which contains the row and the 
     * column equal to row and column of the domino passed how parameters 
     * to this function
     * @throws IOException
     */
    public Dominoes loadMatrix(Dominoes dominoes)throws IOException;
    
    /**This Fuction remove a domino which base in the idetifier row and column
     *
     * @param dominoes contain the identifier row and column this matriz
     * @return true, in case affirmative
     * @throws IOException
     */
    public boolean removeMatrix(Dominoes dominoes)throws IOException;
    
    /**This function has begin when the user want to save a matrix
     *
     * @param dominoes information to be saved
     * @return true, in case affirmative.
     * @throws IOException
     */
    public boolean saveMatrix(Dominoes dominoes)throws IOException;
}
