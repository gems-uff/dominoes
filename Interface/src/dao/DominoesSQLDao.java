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

import arch.Matrix2D;

public class DominoesSQLDao implements DominoesDao{
	
	
	public static String repository_name = "Math-Game";
	public static String databaseName = "db/gitdataminer.sqlite";
	private static Connection conn = null;
	
	public static final int Developer_Commit = 1;
	public static final int Commit_File = 2;
	
	
	
	public static void openDatabase() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
	}

    @Override
    public ArrayList<Dominoes> loadAllMatrices() throws IOException, SQLException, Exception {
    	
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
				
				Matrix2D _mat = loadMatrixFromDatabase(_id, _row_name, _col_name);
				
				Dominoes _dom = new Dominoes(_row_ab, _col_ab, _mat);
				_dominoesList.add(_dom);
			}
							
			rs.close();
			smt.close();
		}
    	
    	return _dominoesList;
    }

    private Matrix2D loadMatrixFromDatabase(int id, String row_name, String col_name) throws Exception{

    	switch (id){
    	case Developer_Commit:
    		return loadDeveloperCommit(row_name, col_name);
    		
    	case Commit_File:
    		return loadCommitFile(row_name, col_name);
    	}
    	
    	return null;
    }
    
    private Matrix2D loadCommitFile(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository_name + "' "
				+ "ORDER BY TC.Date;";
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("HashCode"));
		}
		
		// Get all elements
		sql = "SELECT Distinct(TF.NewName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' " +					
				"AND TR.name = '" + repository_name + "';";					
					
		rs = smt.executeQuery(sql);
			
		while (rs.next())
			descriptor.AddColDesc(rs.getString("NewName"));
						
		// Build Matrix
		Matrix2D mat = new Matrix2D(descriptor);

		sql = "SELECT TC.HashCode, TF.NewName FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' " +
				"AND TR.name = '" + repository_name + "' " +
				"ORDER BY TC.Date, TF.NewName;";
					
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		float [] matrix = new float[descriptor.getNumRows() * descriptor.getNumCols()];
		
		while (rs.next()){	
			int row_idx = descriptor.getRowElementIndex(rs.getString("HashCode"));
			int col_idx = descriptor.getColElementIndex(rs.getString("NewName"));
			
			matrix[row_idx * descriptor.getNumCols() + col_idx] = 1;
		}
		
		mat.setData(matrix);
		
		rs.close();
		smt.close();
				
		return mat;
    }
    
    private Matrix2D loadDeveloperCommit(String row, String col) throws Exception{
    	
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(
				row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository_name + "' "
				+ "ORDER BY TC.Date;";
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddColDesc(rs.getString("HashCode"));
		}
		
		// Get all users
		sql = "SELECT Distinct(TU.name) FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "';";
					
		rs = smt.executeQuery(sql);
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("name"));
		}
		
		// Build Matrix
		Matrix2D mat = new Matrix2D(descriptor);
		
		// Get all commits
		sql = "SELECT TU.name, TC.HashCode FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "';";

		rs = smt.executeQuery(sql);
		
		// temp matrix
		float [] matrix = new float[descriptor.getNumRows() * descriptor.getNumCols()];
		
		while (rs.next()){	
			int row_idx = descriptor.getRowElementIndex(rs.getString("name"));
			int col_idx = descriptor.getColElementIndex(rs.getString("HashCode"));
			
			matrix[row_idx * descriptor.getNumCols() + col_idx] = 1;
		}
		rs.close();
		smt.close();
		
		mat.setData(matrix);
		
		return mat;
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
