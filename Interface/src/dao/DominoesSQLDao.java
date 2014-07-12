package dao;

import domain.Dominoes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DominoesSQLDao implements DominoesDao{
	
	
	public static String databaseName = "db/gitdataminer.sqlite";
	private static Connection conn = null;
	
	
	
	public static void openDatabase() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
	}

    @Override
    public ArrayList<Dominoes> loadAllMatrices() throws IOException, SQLException {
    	
    	ArrayList<Dominoes> _dominoesList = new ArrayList<Dominoes>();
    	
    	if (conn != null){
			Statement smt = conn.createStatement();
			ResultSet rs = null;
			
			String sql = "SELECT * FROM TMATDESC;";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next()){
				int _id = rs.getInt("mat_id");
				String _row_name = rs.getString("row_name");
				String _col_name = rs.getString("column_name");
				String _row_ab = rs.getString("row_abbreviate");
				String _col_ab = rs.getString("column_abbreviate");
				
				Dominoes _dom = new Dominoes(Dominoes.TYPE_BASIC, _row_ab, _col_ab,
						_id, _row_name, _col_name);
				_dominoesList.add(_dom);
			}
							
			rs.close();
			smt.close();
		}
    	
    	return _dominoesList;
    }

    @Override
    public Dominoes loadMatrix(Dominoes dominoes) throws IOException {
        return null;
    }

    @Override
    public boolean removeMatrix(Dominoes dominoes) throws IOException {
        return true;
    }

    @Override
    public boolean saveMatrix(Dominoes dominoes) throws IOException {
        return true;
    }
    
}
